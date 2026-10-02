package com.traverse.appliedthaumaturge.mixin;

import appeng.api.stacks.AEItemKey;
import appeng.client.gui.me.common.RepoSlot;
import com.leclowndu93150.thaumaturge.client.item.InventoryScanHandler;
import com.traverse.appliedthaumaturge.client.ArcaneTermScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = InventoryScanHandler.class, remap = false)
public abstract class InventoryScanHandlerMixin {
    @Shadow
    private static int target;

    @Shadow
    private static int ticks;

    @Unique
    private static long appliedthaumaturge$hoveredSerial = -1;

    @Inject(method = "targetUnderMouse", at = @At("HEAD"), cancellable = true)
    private static void appliedthaumaturge$targetStoredItem(AbstractContainerScreen<?> screen, LocalPlayer player, int mouseX, int mouseY,
                                                       CallbackInfoReturnable<Integer> cir) {
        if (screen instanceof ArcaneTermScreen && screen.getSlotUnderMouse() instanceof RepoSlot slot) {
            var entry = slot.getEntry();
            if (entry != null && entry.getWhat() instanceof AEItemKey && entry.getStoredAmount() > 0) {
                if (appliedthaumaturge$hoveredSerial != entry.getSerial()) {
                    ticks = 0;
                    appliedthaumaturge$hoveredSerial = entry.getSerial();
                }
                int slotPosition = screen.getMenu().slots.indexOf(slot);
                cir.setReturnValue(slotPosition >= 0 ? slotPosition : Integer.MIN_VALUE);
                return;
            }
            cir.setReturnValue(Integer.MIN_VALUE);
        }
        appliedthaumaturge$hoveredSerial = -1;
    }

    @Inject(method = "onClientTick", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/network/ClientPacketDistributor;sendToServer(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;[Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V"), cancellable = true)
    private static void appliedthaumaturge$scanStoredItem(ClientTickEvent.Post event, CallbackInfo ci) {
        if (Minecraft.getInstance().screen instanceof ArcaneTermScreen screen) {
            var menu = screen.getMenu();
            if (target >= 0 && target < menu.slots.size()) {
                Slot slot = menu.getSlot(target);
                if (slot instanceof RepoSlot repoSlot) {
                    var entry = repoSlot.getEntry();
                    if (entry != null && entry.getWhat() instanceof AEItemKey && entry.getStoredAmount() > 0) {
                        menu.scanStoredItem(entry.getSerial());
                    }
                    ticks = 0;
                    ci.cancel();
                }
            }
        }
    }
}
