package com.worldbattles.music;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.sounds.SoundEvent;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod(WorldBattlesMusic.MODID)
public final class WorldBattlesMusic {
    public static final String MODID = "worldbattlesmusic";
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
    public static final RegistryObject<SoundEvent> WORLDBATTLES = sound("worldbattles");
    public static final RegistryObject<SoundEvent> SANDWARS = sound("sandwars");
    public static final RegistryObject<SoundEvent> ZOMBIES = sound("zombies");
    private static final Map<UUID, String> PLAYING = new ConcurrentHashMap<>();

    private static RegistryObject<SoundEvent> sound(String path) {
        ResourceLocation id = new ResourceLocation(MODID, path);
        return SOUNDS.register(path, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public WorldBattlesMusic() {
        SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        MusicNetwork.register();
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = event.getServer();
        Map<UUID, String> desired = new HashMap<>();
        collect("com.worldbattles.arenas.WorldBattlesMode", "MATCHES", "teams", "worldbattles", desired);
        collect("com.worldbattles.arenas.SandWarsMode", "MATCHES", "teams", "sandwars", desired);
        collect("com.worldbattles.arenas.WorldBattlesArenas", "ARENAS", "players", "zombies", desired);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID id = player.getUUID();
            String next = desired.get(id);
            String current = PLAYING.get(id);
            if (Objects.equals(current, next)) continue;
            if (current != null) MusicNetwork.send(player, current, false);
            if (next != null) MusicNetwork.send(player, next, true);
            if (next == null) PLAYING.remove(id); else PLAYING.put(id, next);
        }
        PLAYING.keySet().removeIf(id -> server.getPlayerList().getPlayer(id) == null);
    }

    private static void collect(String className, String arrayField, String memberField,
                                String track, Map<UUID, String> desired) {
        try {
            Class<?> owner = Class.forName(className, false, WorldBattlesMusic.class.getClassLoader());
            Field matchesField = owner.getDeclaredField(arrayField);
            matchesField.setAccessible(true);
            Object value = matchesField.get(null);
            if (!(value instanceof Object[] matches)) return;
            for (Object match : matches) {
                if (match == null) continue;
                Field phaseField = match.getClass().getDeclaredField("phase");
                phaseField.setAccessible(true);
                Object phase = phaseField.get(match);
                if (phase == null || !"ACTIVE".equals(phase.toString())) continue;
                Field membersField = match.getClass().getDeclaredField(memberField);
                membersField.setAccessible(true);
                Object members = membersField.get(match);
                if (members instanceof Map<?, ?> map) {
                    for (Object key : map.keySet()) if (key instanceof UUID uuid) desired.putIfAbsent(uuid, track);
                } else if (members instanceof Collection<?> collection) {
                    for (Object key : collection) if (key instanceof UUID uuid) desired.putIfAbsent(uuid, track);
                }
            }
        } catch (ClassNotFoundException ignored) {
            // Optional mode class is absent in this WB Arenas build.
        } catch (ReflectiveOperationException | RuntimeException exception) {
            System.err.println("[WorldBattlesMusic] Could not inspect " + className + ": " + exception.getMessage());
        }
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            String current = PLAYING.remove(player.getUUID());
            if (current != null) MusicNetwork.send(player, current, false);
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        PLAYING.clear();
    }
}
