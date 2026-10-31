package com.worldbattles.music;

import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Blocks Minecraft's background soundtrack while a WorldBattles match soundtrack is active. */
@Mod.EventBusSubscriber(modid = WorldBattlesMusic.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientMusicEvents {
    private ClientMusicEvents() {}

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        if (event.getOriginalSound().getSource() == SoundSource.MUSIC
                && "minecraft".equals(event.getOriginalSound().getLocation().getNamespace())
                && ClientMusic.isPlayingMatchMusic()) {
            event.setSound(null);
        }
    }
}
