package net.liukrast.toggleable_enchantments.screen;

import net.liukrast.toggleable_enchantments.TEConstants;
import net.liukrast.toggleable_enchantments.packet.ChangeGroupPacket;
import net.liukrast.toggleable_enchantments.packet.ToggleEnchantmentPacket;
import net.liukrast.toggleable_enchantments.platform.TEServices;
import net.liukrast.toggleable_enchantments.registry.RegisterDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;

import java.awt.*;
import java.util.*;
import java.util.List;

public class TEScreen extends Screen {
    private static final Identifier TEXTURE = TEConstants.id("textures/gui/toggleable_enchantments.png");
    private static final Identifier BUTTON = TEConstants.id("toggle_button");
    private static final Identifier SCROLLER = TEConstants.id("scroller");
    public static final Component TITLE = Component.translatable("container.toggleable_enchantments");

    private static final List<Component> TOOLTIP = Arrays.asList(new Component[]{
            Component.translatable("container.toggleable_enchantments.hotkey_group"),
            Component.translatable("container.toggleable_enchantments.hotkey_group.help")
    });

    private static final int IMAGE_W = 176, IMAGE_H = 144;
    private static final int TOP_OFFSET = 7;
    private static final int BUTTON_W = 16, BUTTON_OFFSET = 24;

    private List<Map.Entry<Entry<Holder<Enchantment>>, Boolean>> list = Collections.emptyList();
    private int scrollOffs = 0;
    public TEScreen() {
        super(TITLE);
    }


    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        int leftPos = (this.width- IMAGE_W)>>1;
        int topPos = (this.height- IMAGE_H)>>1;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, IMAGE_W, IMAGE_H, 256, 256);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        int leftPos = (this.width-IMAGE_W)>>1;
        int topPos = (this.height-IMAGE_H)>>1;
        int j = 0;
        for(int i = Math.max(0, scrollOffs); i < list.size(); i++) {
            if(j > 9) break;
            j++;
            var entry = list.get(i);
            var subEntry = entry.getKey();
            var holder = subEntry.getKey();
            boolean enabled = entry.getValue();
            int level = subEntry.getIntValue();
            assert Minecraft.getInstance().player != null;
            int group = Minecraft.getInstance().player.getMainHandItem().getOrDefault(RegisterDataComponents.ENCHANTMENT_GROUPS, ItemEnchantments.EMPTY).getLevel(holder);
            if(level <= 0) continue;
            var comp = Enchantment.getFullname(holder, level).plainCopy().withStyle(ChatFormatting.WHITE);
            boolean hovered = mouseX >= leftPos + IMAGE_W - BUTTON_W - BUTTON_OFFSET && mouseX < leftPos + IMAGE_W - BUTTON_OFFSET && mouseY >= topPos + j*12 + TOP_OFFSET && mouseY < topPos + j*12 + 8 + TOP_OFFSET;
            graphics.text(this.font, comp, leftPos + 9, topPos + j*12 + TOP_OFFSET, -1);
            graphics.text(this.font, group == 0 ? "-" : String.valueOf(group), leftPos + 120, topPos + j*12 + TOP_OFFSET, -1);
            if(!holder.is(TEConstants.WHITELIST) && holder.is(TEConstants.BLACKLIST)) continue;
            graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    BUTTON,
                    32, 16,
                    (hovered ? BUTTON_W : 0),
                    enabled ? 0 : 8,
                    leftPos + IMAGE_W - BUTTON_W - BUTTON_OFFSET,
                    topPos + j*12 + TOP_OFFSET,
                    BUTTON_W, 8
            );
        }


        int k = (int)(((float)scrollOffs / Math.max(list.size() - 10, 1)) * 105);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER, leftPos + 156, topPos + 18 + k, 12, 13);

        if(mouseX >= leftPos + 112 && mouseX < leftPos + 134 && mouseY >= topPos + 17 && mouseY < topPos + 137) {
            graphics.tooltip(
                    this.font,
                    TOOLTIP.stream()
                            .map(c -> ClientTooltipComponent.create(c.getVisualOrderText()))
                            .toList(),
                    mouseX,
                    mouseY,
                    DefaultTooltipPositioner.INSTANCE,
                    null
            );
        }

        renderLabels(graphics);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();

        int leftPos = (this.width - IMAGE_W) >> 1;
        int topPos = (this.height - IMAGE_H) >> 1;
        int j = 0;
        for (int i = Math.max(0, scrollOffs); i < list.size(); i++) {
            if (j > 9) break;
            j++;
            var entry = list.get(i);
            var subEntry = entry.getKey();
            var holder = subEntry.getKey();
            int level = subEntry.getIntValue();
            if (level <= 0) continue;

            boolean hovered = mouseX >= leftPos + IMAGE_W - BUTTON_W - BUTTON_OFFSET
                    && mouseX < leftPos + 176 - BUTTON_OFFSET
                    && mouseY >= topPos + j * 12 + TOP_OFFSET
                    && mouseY < topPos + j * 12 + 8 + TOP_OFFSET;

            if (!holder.is(TEConstants.WHITELIST) && holder.is(TEConstants.BLACKLIST)) continue;
            if (!hovered) continue;
            var level1 = Minecraft.getInstance().level;
            if (level1 == null) continue;
            var access = level1.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            Identifier id = access.getKey(holder.value());
            if (id == null) continue;
            TEServices.PLATFORM.send2S(new ToggleEnchantmentPacket(List.of(id), EquipmentSlot.MAINHAND));
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if(super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        int leftPos = (this.width-IMAGE_W)>>1;
        int topPos = (this.height-IMAGE_H)>>1;

        int j = 0;
        for(int i = Math.max(0, scrollOffs); i < list.size(); i++) {
            if(j > 9) break;
            j++;
            var entry = list.get(i);
            var subEntry = entry.getKey();
            var holder = subEntry.getKey();
            int level = subEntry.getIntValue();
            assert Minecraft.getInstance().player != null;
            int group = Minecraft.getInstance().player.getMainHandItem().getOrDefault(RegisterDataComponents.ENCHANTMENT_GROUPS, ItemEnchantments.EMPTY).getLevel(holder);
            if(level <= 0) continue;
            boolean hovered = mouseX >= leftPos + 112 && mouseX < leftPos + 134 && mouseY >= topPos + j*12 + TOP_OFFSET -2 && mouseY < topPos + j*12 + TOP_OFFSET + 10;
            if(!hovered) continue;
            var level1 = Minecraft.getInstance().level;
            if(level1 == null) continue;
            var access = level1.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            Identifier id = access.getKey(holder.value());
            if(id == null) continue;
            TEServices.PLATFORM.send2S(new ChangeGroupPacket(id, (int) Math.clamp(group + scrollY, 0, 9)));
            return true;
        }
        scrollOffs = (int) Mth.clamp(scrollOffs-scrollY, 0, Math.max(0, list.size()-10));
        return true;
    }

    @Override
    public void tick() {
        reload();
        super.tick();
    }

    private void reload() {
        var player = Minecraft.getInstance().player;
        if(player == null) {
            this.onClose();
            list = Collections.emptyList();
            return;
        }
        var stack = player.getMainHandItem();
        if(stack.isEmpty()) this.onClose();
        var enabled = stack.getEnchantments();
        var disabled = stack.getOrDefault(RegisterDataComponents.DISABLED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        final Map<Entry<Holder<Enchantment>>, Boolean> map = new HashMap<>();
        enabled.entrySet().forEach(e -> map.put(e, true));
        disabled.entrySet().forEach(e -> map.put(e, false));
        this.list = map.entrySet()
                .stream()
                .sorted(Comparator.comparing(e -> e.getKey().getKey().getRegisteredName()))
                .toList();
    }

    /**
     * @author Tier1234
     * Beacuse of {@link Screen} don't have any renderLabel method this one is inspired by the one inside
     * {@link net.minecraft.client.gui.screens.inventory.AbstractContainerScreen}
     * */
    private void renderLabels(GuiGraphicsExtractor graphics) {
        int leftPos = (this.width - IMAGE_W)>>1;
        int topPos = (this.height - IMAGE_H)>>1;
        graphics.text(this.font,TITLE,leftPos + 7,topPos + 7, Color.DARK_GRAY.getRGB(), false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
