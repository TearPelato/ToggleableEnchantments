package net.liukrast.toggleable_enchantments;

import net.liukrast.toggleable_enchantments.registry.RegisterDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TEConstants {
    public static final String MOD_ID = "toggleable_enchantments";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String PACKET_VERSION = "1.0.1";

    public static final TagKey<Enchantment> BLACKLIST = TagKey.create(Registries.ENCHANTMENT, id("blacklist"));
    public static final TagKey<Enchantment> WHITELIST = TagKey.create(Registries.ENCHANTMENT, id("whitelist"));

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void toggleEnchantments(ItemStack stack, List<Holder<Enchantment>> enchantments, @Nullable Player player) {
        ItemEnchantments.Mutable enabled = new ItemEnchantments.Mutable(stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY));
        ItemEnchantments.Mutable disabled = new ItemEnchantments.Mutable(stack.getOrDefault(RegisterDataComponents.DISABLED_ENCHANTMENTS, ItemEnchantments.EMPTY));
        List<String> enabled1 = new ArrayList<>();
        List<String> disabled1 = new ArrayList<>();
        for(Holder<Enchantment> enchantment : enchantments) {
            if (!stack.isEmpty()) {
                boolean enable = enabled.getLevel(enchantment) == 0;
                int level = enabled.getLevel(enchantment) == 0 ? disabled.getLevel(enchantment) : enabled.getLevel(enchantment);
                if (level > 0) {
                    if (enable) {
                        disabled.removeIf(holder -> holder.equals(enchantment));
                        enabled.upgrade(enchantment, level);
                        enabled1.add(enchantment.value().description().copy().getString());
                    } else {
                        enabled.removeIf(holder -> holder.equals(enchantment));
                        disabled.upgrade(enchantment, level);
                        disabled1.add(enchantment.value().description().copy().getString());
                    }
                } else LOGGER.error("Enchantment toggle failed. ItemStack {} does not have {}", stack, enchantment.getRegisteredName());
            } else LOGGER.error("Enchantment toggle failed. ItemStack {} is empty", stack);
        }
        var fEnabled = enabled.toImmutable();
        var fDisabled = disabled.toImmutable();
        if(fEnabled.isEmpty()) stack.remove(DataComponents.ENCHANTMENTS);
        else stack.set(DataComponents.ENCHANTMENTS, fEnabled);
        if(fDisabled.isEmpty()) stack.remove(RegisterDataComponents.DISABLED_ENCHANTMENTS);
        else stack.set(RegisterDataComponents.DISABLED_ENCHANTMENTS, fDisabled);
        if(player != null) {
            if(!enabled1.isEmpty()) player.sendSystemMessage(Component.translatable("toggleable_enchantments.enable", String.join(", ", enabled1), stack.getDisplayName()));
            if(!disabled1.isEmpty()) player.sendSystemMessage(Component.translatable("toggleable_enchantments.disable", String.join(", ", disabled1), stack.getDisplayName()));
        }
    }
}
