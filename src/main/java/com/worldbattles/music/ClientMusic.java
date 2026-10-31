package com.worldbattles.music;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = WorldBattlesMusic.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
final class ClientMusic {
    private static final Map<String, MatchLoopSound> ACTIVE = new ConcurrentHashMap<>();

    private ClientMusic() {}

    static boolean isPlayingMatchMusic() {
        return !ACTIVE.isEmpty();
    }

    static void handle(String track, boolean start) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!start) {
            MatchLoopSound old = ACTIVE.remove(track);
            if (old != null) minecraft.getSoundManager().stop(old);
            return;
        }

        MatchLoopSound old = ACTIVE.remove(track);
        if (old != null) minecraft.getSoundManager().stop(old);

        // Stop vanilla background music only when a match soundtrack starts.
        minecraft.getSoundManager().stop(null, SoundSource.MUSIC);
        startTrack(track);
    }

    private static void startTrack(String track) {
        ResourceLocation id = new ResourceLocation(WorldBattlesMusic.MODID, track);
        SoundEvent event = BuiltInRegistries.SOUND_EVENT.get(id);
        if (event == null) {
            System.err.println("[WorldBattlesMusic] Missing sound event: " + id);
            return;
        }
        MatchLoopSound sound = new MatchLoopSound(event, track);
        ACTIVE.put(track, sound);
        Minecraft.getInstance().getSoundManager().play(sound);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ACTIVE.isEmpty()) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            for (MatchLoopSound sound : ACTIVE.values()) minecraft.getSoundManager().stop(sound);
            ACTIVE.clear();
            return;
        }

        // Some streamed OGG tracks can terminate after a small number of native
        // loops. Keep the match soundtrack alive until the server sends STOP.
        for (Map.Entry<String, MatchLoopSound> entry : ACTIVE.entrySet()) {
            if (!minecraft.getSoundManager().isActive(entry.getValue())) {
                startTrackIfCurrent(entry.getKey(), entry.getValue());
            }
        }
    }

    private static void startTrackIfCurrent(String track, MatchLoopSound ended) {
        if (ACTIVE.remove(track, ended)) {
            ResourceLocation id = new ResourceLocation(WorldBattlesMusic.MODID, track);
            SoundEvent event = BuiltInRegistries.SOUND_EVENT.get(id);
            if (event != null) {
                MatchLoopSound replacement = new MatchLoopSound(event, track);
                ACTIVE.put(track, replacement);
                Minecraft.getInstance().getSoundManager().play(replacement);
            }
        }
    }

    private static final class MatchLoopSound extends AbstractTickableSoundInstance {
        MatchLoopSound(SoundEvent event, String track) {
            super(event, SoundSource.MUSIC, RandomSource.create());
            this.looping = true;
            this.delay = 0;
            this.volume = "worldbattles".equals(track) ? 1.6F : 1.0F;
            this.pitch = 1.0F;
            this.relative = true;
        }

        @Override
        public void tick() {
            if (Minecraft.getInstance().level == null) this.stop();
        }
    }
}
