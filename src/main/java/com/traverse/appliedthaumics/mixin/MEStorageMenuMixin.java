package com.traverse.appliedthaumics.mixin;

import appeng.api.stacks.AEKey;
import appeng.api.storage.ITerminalHost;
import appeng.helpers.WirelessTerminalMenuHost;
import appeng.menu.me.common.MEStorageMenu;
import com.traverse.appliedthaumics.item.WirelessEssentiaTerminalItem;
import com.traverse.appliedthaumics.me.key.EssentiaKey;
import com.traverse.appliedthaumics.part.ArcaneInscriberPart;
import com.traverse.appliedthaumics.part.ArcaneTerminalPart;
import com.traverse.appliedthaumics.part.EssentiaTerminalPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MEStorageMenu.class, remap = false)
public abstract class MEStorageMenuMixin {
    @Shadow
    public abstract ITerminalHost getHost();

    @Inject(method = "isKeyVisible", at = @At("HEAD"), cancellable = true)
    private void appliedthaumics$hideEssentia(AEKey key, CallbackInfoReturnable<Boolean> cir) {
        if (key instanceof EssentiaKey) {
            ITerminalHost host = getHost();
            if (!(host instanceof EssentiaTerminalPart)
                    && !(host instanceof ArcaneTerminalPart)
                    && !(host instanceof ArcaneInscriberPart)
                    && !(host instanceof WirelessTerminalMenuHost<?> wireless && wireless.getItem() instanceof WirelessEssentiaTerminalItem)) {
                cir.setReturnValue(false);
            }
        }
    }
}
