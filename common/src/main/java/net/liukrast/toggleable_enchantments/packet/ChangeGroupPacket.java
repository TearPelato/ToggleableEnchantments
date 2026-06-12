package net.liukrast.toggleable_enchantments.packet;

import net.liukrast.toggleable_enchantments.TEConstants;
import net.liukrast.toggleable_enchantments.registry.RegisterDataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public record ChangeGroupPacket(ResourceLocation enchantment, int group) implements CustomPacketPayload {
    public static final Type<ChangeGroupPacket> PACKET_TYPE = new Type<>(TEConstants.id("change_group"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChangeGroupPacket> CODEC = StreamCodec.ofMember(ChangeGroupPacket::write, ChangeGroupPacket::new);

    public ChangeGroupPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readResourceLocation(), buf.readInt());
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeResourceLocation(enchantment);
        buf.writeInt(group);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    public static void handle(ChangeGroupPacket packet, Player ctx) {
        var enchantRegistry = ctx.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        var enchantment = enchantRegistry.getHolderOrThrow(ResourceKey.create(Registries.ENCHANTMENT, packet.enchantment));
        ItemStack stack = ctx.getMainHandItem();
        ItemEnchantments groups = stack.getOrDefault(RegisterDataComponents.ENCHANTMENT_GROUPS, ItemEnchantments.EMPTY);
        var mutable = new ItemEnchantments.Mutable(groups);
        mutable.set(enchantment, packet.group);
        stack.set(RegisterDataComponents.ENCHANTMENT_GROUPS, mutable.toImmutable());
    }
}
