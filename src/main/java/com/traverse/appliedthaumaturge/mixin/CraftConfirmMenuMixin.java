package com.traverse.appliedthaumaturge.mixin;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.security.IActionSource;
import appeng.core.network.clientbound.CraftConfirmPlanPacket;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.me.crafting.CraftingPlanSummary;
import com.traverse.appliedthaumaturge.assembler.ArcanePattern;
import com.traverse.appliedthaumaturge.assembler.ArcanePlanCheck;
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
    private int appliedthaumaturge$refresh;

    @Inject(method = "broadcastChanges", at = @At("TAIL"))
    private void appliedthaumaturge$refreshPlan(CallbackInfo ci) {
        var menu = (CraftConfirmMenu) (Object) this;
        if (!menu.isClientSide() && result != null && appliedthaumaturge$refresh-- <= 0
                && result.patternTimes().keySet().stream().anyMatch(ArcanePattern.class::isInstance)) {
            appliedthaumaturge$refresh = 10;
            appliedthaumaturge$sendPlan(menu);
        }
    }

    @Inject(method = "startJob", at = @At("HEAD"), cancellable = true)
    private void appliedthaumaturge$validateStart(CallbackInfo ci) {
        var menu = (CraftConfirmMenu) (Object) this;
        if (!menu.isClientSide() && ArcanePlanCheck.entries(getGrid(), result).stream()
                .anyMatch(entry -> entry.getMissingAmount() > 0)) {
            appliedthaumaturge$sendPlan(menu);
            ci.cancel();
        }
    }

    @Unique
    private void appliedthaumaturge$sendPlan(CraftConfirmMenu menu) {
        IGrid grid = getGrid();
        if (grid != null && result != null && menu.getPlayer() instanceof ServerPlayer player) {
            CraftingPlanSummary summary = CraftingPlanSummary.fromJob(grid, getActionSrc(), result);
            menu.setPlan(summary);
            PacketDistributor.sendToPlayer(player, new CraftConfirmPlanPacket(summary));
        }
    }
}
