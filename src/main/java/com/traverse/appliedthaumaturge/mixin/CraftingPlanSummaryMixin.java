package com.traverse.appliedthaumaturge.mixin;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.security.IActionSource;
import appeng.menu.me.crafting.CraftingPlanSummary;
import com.traverse.appliedthaumaturge.assembler.ArcanePlanCheck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CraftingPlanSummary.class, remap = false)
public abstract class CraftingPlanSummaryMixin {
    @Inject(method = "fromJob", at = @At("RETURN"), cancellable = true)
    private static void appliedthaumaturge$addVis(IGrid grid, IActionSource source, ICraftingPlan plan,
                                              CallbackInfoReturnable<CraftingPlanSummary> cir) {
        cir.setReturnValue(ArcanePlanCheck.augment(grid, plan, cir.getReturnValue()));
    }
}
