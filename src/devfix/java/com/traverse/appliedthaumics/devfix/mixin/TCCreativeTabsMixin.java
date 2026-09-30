package com.traverse.appliedthaumics.devfix.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.leclowndu93150.thaumaturge.registry.TCCreativeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TCCreativeTabs.class)
public class TCCreativeTabsMixin {
    @WrapOperation(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab$Output;accept(Lnet/minecraft/world/item/ItemStack;)V"))
    private static void appliedthaumics$skipDuplicateStack(CreativeModeTab.Output output, ItemStack stack, Operation<Void> original) {
        try {
            original.call(output, stack);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @WrapOperation(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab$Output;accept(Lnet/minecraft/world/level/ItemLike;)V"))
    private static void appliedthaumics$skipDuplicateItem(CreativeModeTab.Output output, ItemLike item, Operation<Void> original) {
        try {
            original.call(output, item);
        } catch (IllegalArgumentException ignored) {
        }
    }
}
