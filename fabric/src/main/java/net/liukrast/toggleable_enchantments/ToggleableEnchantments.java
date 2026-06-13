package net.liukrast.toggleable_enchantments;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.liukrast.toggleable_enchantments.registry.RegisterDataComponents;
import net.liukrast.toggleable_enchantments.registry.RegisterKeyMappings;
import net.liukrast.toggleable_enchantments.registry.RegisterNetworking;
import net.minecraft.client.KeyMapping;

import java.util.HashSet;
import java.util.Set;

public class ToggleableEnchantments implements ModInitializer, ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Set<KeyMapping> keyMappings = new HashSet<>();
        RegisterKeyMappings.register(keyMappings);
        keyMappings.forEach(KeyBindingHelper::registerKeyBinding);
        ClientTickEvents.END_CLIENT_TICK.register(client -> TEClient.onClientTick());
    }

    @Override
    public void onInitialize() {
        RegisterDataComponents.register();
        RegisterNetworking.register();
        RegisterNetworking.registerServer();
    }
}
