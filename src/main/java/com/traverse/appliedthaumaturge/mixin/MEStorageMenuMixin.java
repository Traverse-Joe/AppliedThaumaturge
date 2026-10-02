package com.traverse.appliedthaumaturge.mixin;

import appeng.api.stacks.AEKey;
import appeng.api.storage.ITerminalHost;
import appeng.helpers.WirelessTerminalMenuHost;
import appeng.menu.me.common.MEStorageMenu;
import com.traverse.appliedthaumaturge.item.WirelessEssentiaTerminalItem;
import com.traverse.appliedthaumaturge.item.PortableEssentiaCellItem;
import appeng.items.contents.PortableCellMenuHost;
import com.traverse.appliedthaumaturge.me.key.EssentiaKey;
import com.traverse.appliedthaumaturge.part.ArcaneInscriberPart;
import com.traverse.appliedthaumaturge.part.ArcaneTerminalPart;
import com.traverse.appliedthaumaturge.part.EssentiaTerminalPart;
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
    private void appliedthaumaturge$hideEssentia(AEKey key, CallbackInfoReturnable<Boolean> cir) {
        if (key instanceof EssentiaKey) {
            ITerminalHost host = getHost();
            if (!(host instanceof EssentiaTerminalPart)
                    && !(host instanceof ArcaneTerminalPart)
                    && !(host instanceof ArcaneInscriberPart)
                    && !(host instanceof PortableCellMenuHost<?> portable && portable.getItem() instanceof PortableEssentiaCellItem)
                    && !(host instanceof WirelessTerminalMenuHost<?> wireless && wireless.getItem() instanceof WirelessEssentiaTerminalItem)) {
                cir.setReturnValue(false);
            }
        }
    }
}
