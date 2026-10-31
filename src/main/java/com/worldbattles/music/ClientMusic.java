package com.worldbattles.music;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        // Cut off an already-playing vanilla background track before starting ours.
        minecraft.getSoundManager().stop(null, SoundSource.MUSIC);
        ResourceLocation id = new ResourceLocation(WorldBattlesMusic.MODID, track);
        SoundEvent event = BuiltInRegistries.SOUND_EVENT.get(id);
        MatchLoopSound sound = new MatchLoopSound(event, track);
        ACTIVE.put(track, sound);
        minecraft.getSoundManager().play(sound);
    }

    private static final class MatchLoopSound extends AbstractTickableSoundInstance {
        MatchLoopSound(SoundEvent event, String track) {
            super(event, SoundSource.MUSIC, RandomSource.create());
            this.looping = true;
            this.delay = 0;
            this.volume = 1.0F;
            this.pitch = 1.0F;
            this.relative = true;
        }
        @Override public void tick() {
            if (Minecraft.getInstance().level == null) this.stop();
        }
    }
}
