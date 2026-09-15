package net.liukrast.toggleable_enchantments.registry;

import com.mojang.blaze3d.platform.InputConstants;
import net.liukrast.toggleable_enchantments.TEConstants;
import net.minecraft.client.KeyMapping;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RegisterKeyMappings {

    public static final KeyMapping.Category MAIN_CATEGORY = new KeyMapping.Category(TEConstants.id("main"));


    public static final KeyMapping TOGGLEABLE_MENU = new KeyMapping(
            "key.toggleable_enchantments.open_gui",
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_J,
            MAIN_CATEGORY
    );

    public static final List<KeyMapping> GROUP_KEYS = IntStream.range(1, 10).mapToObj(i -> new KeyMapping(
            "key.toggleable_enchantments.group_" + i,
            InputConstants.Type.KEYBOARD,
            320 + i,
            MAIN_CATEGORY
    )).collect(Collectors.toCollection(ArrayList::new));

    public static void register(Set<KeyMapping> keyMappings) {
        keyMappings.add(TOGGLEABLE_MENU);
        keyMappings.addAll(GROUP_KEYS);
    }

}
