package com.traverse.appliedthaumaturge.mixin;

import appeng.api.stacks.AEKeyType;
import appeng.parts.reporting.AbstractTerminalPart;
import com.traverse.appliedthaumaturge.me.key.EssentiaKeyType;
import com.traverse.appliedthaumaturge.me.key.VisKeyType;
import com.traverse.appliedthaumaturge.part.ArcaneInscriberPart;
import com.traverse.appliedthaumaturge.part.ArcaneTerminalPart;
import java.util.function.Predicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = AbstractTerminalPart.class, remap = false)
public abstract class AbstractTerminalPartMixin {
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lappeng/api/util/KeyTypeSelection;<init>(Ljava/lang/Runnable;Ljava/util/function/Predicate;)V"), index = 1)
    private Predicate<AEKeyType> appliedthaumaturge$filterTerminalTypes(Predicate<AEKeyType> supportedTypes) {
        boolean showEssentia = (Object) this instanceof ArcaneTerminalPart || (Object) this instanceof ArcaneInscriberPart;
        return type -> type != VisKeyType.TYPE
                && (showEssentia || type != EssentiaKeyType.TYPE)
                && supportedTypes.test(type);
    }
}
