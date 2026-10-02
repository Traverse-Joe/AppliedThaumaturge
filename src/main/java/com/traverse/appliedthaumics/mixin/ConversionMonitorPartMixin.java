package com.traverse.appliedthaumics.mixin;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.parts.IPartItem;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.parts.reporting.AbstractMonitorPart;
import appeng.parts.reporting.ConversionMonitorPart;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.traverse.appliedthaumics.me.key.EssentiaKey;
import com.traverse.appliedthaumics.part.EssentiaMonitorInteraction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ConversionMonitorPart.class, remap = false)
public abstract class ConversionMonitorPartMixin extends AbstractMonitorPart {
    protected ConversionMonitorPartMixin(IPartItem<?> partItem) {
        super(partItem, true);
    }

    @Inject(method = {"onClicked", "onShiftClicked"}, at = @At("HEAD"), cancellable = true)
    private void appliedthaumics$extractEssentiaToContainer(Player player, Vec3 hit,
            CallbackInfoReturnable<Boolean> cir) {
        if (!(getDisplayed() instanceof EssentiaKey key)) {
            return;
        }

        if (isClientSide()) {
            cir.setReturnValue(true);
            return;
        }
        if (!getMainNode().isActive() || !Platform.hasPermissions(getHost().getLocation(), player)) {
            cir.setReturnValue(false);
            return;
        }
        if (player.getMainHandItem().getCapability(EssentiaCapabilities.ITEM_STORAGE) != null) {
            getMainNode().ifPresent(grid -> EssentiaMonitorInteraction.fill(
                    grid, this, player, InteractionHand.MAIN_HAND, key));
        }
        cir.setReturnValue(true);
    }

    @Inject(method = "onUseItemOn", at = @At("HEAD"), cancellable = true)
    private void appliedthaumics$useEssentiaContainer(ItemStack stack, Player player, InteractionHand hand,
            Vec3 hit, CallbackInfoReturnable<Boolean> cir) {
        if (InteractionUtil.isInAlternateUseMode(player)) {
            return;
        }

        if (isLocked() && stack.copyWithCount(1).getCapability(EssentiaCapabilities.ITEM_STORAGE) != null) {
            if (isClientSide()) {
                cir.setReturnValue(true);
                return;
            }
            if (!getMainNode().isActive() || !Platform.hasPermissions(getHost().getLocation(), player)) {
                cir.setReturnValue(false);
                return;
            }
            getMainNode().ifPresent(grid -> EssentiaMonitorInteraction.empty(grid, this, player, hand));
            cir.setReturnValue(true);
        } else if (!isLocked() && AEItemKey.matches(getDisplayed(), stack)) {
            GenericStack contents = ContainerItemStrategies.getContainedStack(stack);
            if (contents != null && contents.what() instanceof EssentiaKey) {
                cir.setReturnValue(super.onUseItemOn(stack, player, hand, hit));
            }
        }
    }
}
