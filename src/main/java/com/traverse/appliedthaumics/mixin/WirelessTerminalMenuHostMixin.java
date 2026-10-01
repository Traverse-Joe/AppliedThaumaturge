package com.traverse.appliedthaumics.mixin;

import appeng.api.stacks.AEKeyType;
import appeng.helpers.WirelessTerminalMenuHost;
import com.traverse.appliedthaumics.me.key.EssentiaKeyType;
import com.traverse.appliedthaumics.me.key.VisKeyType;
import java.util.function.Predicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = WirelessTerminalMenuHost.class, remap = false)
public abstract class WirelessTerminalMenuHostMixin {
    @ModifyArg(method = "getKeyTypeSelection", at = @At(value = "INVOKE", target = "Lappeng/api/util/KeyTypeSelection;forStack(Lnet/minecraft/world/item/ItemStack;Ljava/util/function/Predicate;)Lappeng/api/util/KeyTypeSelection;"), index = 1)
    private Predicate<AEKeyType> appliedthaumics$filterTerminalTypes(Predicate<AEKeyType> supportedTypes) {
        return type -> type != VisKeyType.TYPE && type != EssentiaKeyType.TYPE && supportedTypes.test(type);
    }
}
