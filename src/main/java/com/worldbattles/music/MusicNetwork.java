package com.worldbattles.music;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Supplier;

public final class MusicNetwork {
    private static final String VERSION = "1";
    private static SimpleChannel CHANNEL;
    private static int packetId = 0;
    private MusicNetwork() {}

    public static void register() {
        CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(WorldBattlesMusic.MODID, "main"),
                () -> VERSION, VERSION::equals, VERSION::equals);
        CHANNEL.messageBuilder(MusicPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(MusicPacket::encode).decoder(MusicPacket::decode)
                .consumerMainThread(MusicPacket::handle).add();
    }

    public static void send(ServerPlayer player, String track, boolean start) {
        if (CHANNEL != null) CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new MusicPacket(track, start));
    }

    public record MusicPacket(String track, boolean start) {
        static void encode(MusicPacket packet, FriendlyByteBuf buffer) {
            buffer.writeUtf(packet.track, 32);
            buffer.writeBoolean(packet.start);
        }
        static MusicPacket decode(FriendlyByteBuf buffer) {
            return new MusicPacket(buffer.readUtf(32), buffer.readBoolean());
        }
        static void handle(MusicPacket packet, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(
                    net.minecraftforge.api.distmarker.Dist.CLIENT,
                    () -> () -> ClientMusic.handle(packet.track, packet.start)));
            context.setPacketHandled(true);
        }
    }
}
