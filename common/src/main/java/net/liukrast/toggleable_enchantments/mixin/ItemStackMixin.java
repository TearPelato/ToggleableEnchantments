package net.liukrast.toggleable_enchantments.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.liukrast.toggleable_enchantments.mixin_helpers.IFlagEnchantment;
import net.liukrast.toggleable_enchantments.registry.RegisterDataComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow public abstract <T extends TooltipProvider> void addToTooltip(DataComponentType<T> component, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag);

    @Inject(
            method = "addDetailsToTooltip",
            at = @At("HEAD")
    )
    private void addDetailsToTooltip(Item.TooltipContext context, TooltipDisplay tooltipDisplay, Player player, TooltipFlag tooltipFlag, Consumer<Component> tooltipAdder, CallbackInfo ci) {
        addToTooltip(RegisterDataComponents.DISABLED_ENCHANTMENTS, context, tooltipDisplay, tooltipAdder, tooltipFlag);
    }

    @ModifyExpressionValue(
            method = "addToTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private Object addToTooltip(Object original, @Local(argsOnly = true) DataComponentType<?> componentType) {
        if(componentType == RegisterDataComponents.DISABLED_ENCHANTMENTS && original != null) ((IFlagEnchantment)original).toggleable_enchantments$setDisabled(true);
        return original;
    }
}
