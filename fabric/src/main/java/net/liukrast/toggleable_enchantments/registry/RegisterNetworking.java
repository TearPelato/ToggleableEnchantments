package net.liukrast.toggleable_enchantments.registry;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.liukrast.toggleable_enchantments.packet.ChangeGroupPacket;
import net.liukrast.toggleable_enchantments.packet.ToggleEnchantmentPacket;

public class RegisterNetworking {
    public static void register() {
        PayloadTypeRegistry.playC2S().register(ToggleEnchantmentPacket.PACKET_TYPE, ToggleEnchantmentPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ChangeGroupPacket.PACKET_TYPE, ChangeGroupPacket.CODEC);
    }

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(ToggleEnchantmentPacket.PACKET_TYPE, (packet, ctx) -> ToggleEnchantmentPacket.handle(packet, ctx.player()));
        ServerPlayNetworking.registerGlobalReceiver(ChangeGroupPacket.PACKET_TYPE, (packet, ctx) -> ChangeGroupPacket.handle(packet, ctx.player()));
    }
}
