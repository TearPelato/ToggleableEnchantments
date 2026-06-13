package net.liukrast.toggleable_enchantments.packet;

import net.liukrast.toggleable_enchantments.TEConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;

public record ToggleEnchantmentPacket(List<Identifier> enchantment, EquipmentSlot slot) implements CustomPacketPayload {
    public static final Type<ToggleEnchantmentPacket> PACKET_TYPE = new Type<>(TEConstants.id("toggle_enchantment"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleEnchantmentPacket> CODEC = StreamCodec.ofMember(ToggleEnchantmentPacket::write, ToggleEnchantmentPacket::new);

    public ToggleEnchantmentPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readCollection(ArrayList::new, Identifier.STREAM_CODEC), fromInt(buf.readInt()));
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeCollection(enchantment, Identifier.STREAM_CODEC);
        buf.writeInt(toInt(slot));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    private static int toInt(EquipmentSlot slot) {
        if(slot.getType() == EquipmentSlot.Type.HAND) return slot.getIndex();
        return slot.getIndex() + 2;
    }

    public static EquipmentSlot fromInt(int value) {
        return switch (value) {
            case 1 -> EquipmentSlot.OFFHAND;
            case 2 -> EquipmentSlot.FEET;
            case 3 -> EquipmentSlot.LEGS;
            case 4 -> EquipmentSlot.CHEST;
            case 5 -> EquipmentSlot.HEAD;
            default -> EquipmentSlot.MAINHAND;
        };
    }

    public static void handle(ToggleEnchantmentPacket packet, Player ctx) {
        var enchantRegistry = ctx.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var list = packet.enchantment.stream().map(e -> ((Holder<Enchantment>)enchantRegistry.getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, e)))).filter(holder -> holder.is(TEConstants.WHITELIST) || !holder.is(TEConstants.BLACKLIST)).toList();
        ItemStack stack = ctx.getItemBySlot(packet.slot);
        if(!list.isEmpty()) TEConstants.toggleEnchantments(stack, list, ctx);
    }
}
