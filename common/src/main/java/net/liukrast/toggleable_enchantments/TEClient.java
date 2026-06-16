package net.liukrast.toggleable_enchantments;

import net.liukrast.toggleable_enchantments.packet.ToggleEnchantmentPacket;
import net.liukrast.toggleable_enchantments.platform.TEServices;
import net.liukrast.toggleable_enchantments.registry.RegisterDataComponents;
import net.liukrast.toggleable_enchantments.registry.RegisterKeyMappings;
import net.liukrast.toggleable_enchantments.screen.TEScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.List;
import java.util.Objects;

public class TEClient {
        public static void onClientTick() {
        final var mc = Minecraft.getInstance();
        if(mc.player == null) return;
        final var stack = mc.player.getMainHandItem();
        while(RegisterKeyMappings.TOGGLEABLE_MENU.consumeClick()) {
            if(!(mc.gui.screen() instanceof TEScreen)) {
                if(!stack.isEmpty()) mc.gui.setScreen(new TEScreen());
            }
        }
        for(int i = 0; i < RegisterKeyMappings.GROUP_KEYS.size(); i++) {
            var mapping = RegisterKeyMappings.GROUP_KEYS.get(i);
            while(mapping.consumeClick()) {
                int finalI = i+1;
                var access = mc.player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
                for(int k = 0; k < 6; k++) {
                    var slot = ToggleEnchantmentPacket.fromInt(k);
                    var stack1 = mc.player.getItemBySlot(slot);
                    if(stack1.isEmpty()) continue;
                    ItemEnchantments groups = stack1.getOrDefault(RegisterDataComponents.ENCHANTMENT_GROUPS, ItemEnchantments.EMPTY);
                    List<Identifier > list = groups.entrySet().stream().filter(entry -> entry.getIntValue() == finalI).map(holder -> access.getKey(holder.getKey().value())).filter(
                            Objects::nonNull).toList();
                    if(!list.isEmpty()) TEServices.PLATFORM.send2S(new ToggleEnchantmentPacket(list, slot));
                }
            }
        }
    }
}
