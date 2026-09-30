package com.traverse.appliedthaumics.mixin;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.security.IActionSource;
import appeng.core.network.clientbound.CraftConfirmPlanPacket;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.me.crafting.CraftingPlanSummary;
import com.traverse.appliedthaumics.assembler.ArcanePattern;
import com.traverse.appliedthaumics.assembler.ArcanePlanCheck;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CraftConfirmMenu.class, remap = false)
public abstract class CraftConfirmMenuMixin {
    @Shadow
    private ICraftingPlan result;

    @Shadow
    private IGrid getGrid() {
        throw new AssertionError();
    }

    @Shadow
    private IActionSource getActionSrc() {
        throw new AssertionError();
    }

    @Unique
    private int appliedthaumics$refresh;

    @Inject(method = "broadcastChanges", at = @At("TAIL"))
    private void appliedthaumics$refreshPlan(CallbackInfo ci) {
        var menu = (CraftConfirmMenu) (Object) this;
        if (!menu.isClientSide() && result != null && appliedthaumics$refresh-- <= 0
                && result.patternTimes().keySet().stream().anyMatch(ArcanePattern.class::isInstance)) {
            appliedthaumics$refresh = 10;
            appliedthaumics$sendPlan(menu);
        }
    }

    @Inject(method = "startJob", at = @At("HEAD"), cancellable = true)
    private void appliedthaumics$validateStart(CallbackInfo ci) {
        var menu = (CraftConfirmMenu) (Object) this;
        if (!menu.isClientSide() && ArcanePlanCheck.entries(getGrid(), result).stream()
                .anyMatch(entry -> entry.getMissingAmount() > 0)) {
            appliedthaumics$sendPlan(menu);
            ci.cancel();
        }
    }

    @Unique
    private void appliedthaumics$sendPlan(CraftConfirmMenu menu) {
        IGrid grid = getGrid();
        if (grid != null && result != null && menu.getPlayer() instanceof ServerPlayer player) {
            CraftingPlanSummary summary = CraftingPlanSummary.fromJob(grid, getActionSrc(), result);
            menu.setPlan(summary);
            PacketDistributor.sendToPlayer(player, new CraftConfirmPlanPacket(summary));
        }
    }
}
