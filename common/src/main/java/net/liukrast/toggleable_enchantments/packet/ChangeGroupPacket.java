package net.liukrast.toggleable_enchantments.packet;

import net.liukrast.toggleable_enchantments.TEConstants;
import net.liukrast.toggleable_enchantments.registry.RegisterDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public record ChangeGroupPacket(Identifier enchantment, int group) implements CustomPacketPayload {
    public static final Type<ChangeGroupPacket> PACKET_TYPE = new Type<>(TEConstants.id("change_group"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChangeGroupPacket> CODEC = StreamCodec.ofMember(ChangeGroupPacket::write, ChangeGroupPacket::new);

    public ChangeGroupPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readIdentifier(), buf.readInt());
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeIdentifier(enchantment);
        buf.writeInt(group);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    public static void handle(ChangeGroupPacket packet, Player ctx) {
        Registry<Enchantment> enchantRegistry = ctx.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        Holder.Reference<Enchantment> enchantment = enchantRegistry.getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, packet.enchantment));
        ItemStack stack = ctx.getMainHandItem();
        ItemEnchantments groups = stack.getOrDefault(RegisterDataComponents.ENCHANTMENT_GROUPS, ItemEnchantments.EMPTY);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(groups);
        mutable.set(enchantment, packet.group);
        stack.set(RegisterDataComponents.ENCHANTMENT_GROUPS, mutable.toImmutable());
    }
}
