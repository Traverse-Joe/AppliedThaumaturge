package com.traverse.appliedthaumics.mixin;

import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.KeyCounter;
import appeng.parts.automation.StorageLevelEmitterPart;
import appeng.util.ConfigInventory;
import com.traverse.appliedthaumics.me.key.EssentiaKey;
import com.traverse.appliedthaumics.me.key.EssentiaKeyType;
import com.traverse.appliedthaumics.part.EssentiaLevelEmitterPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = StorageLevelEmitterPart.class, remap = false)
public abstract class StorageLevelEmitterPartMixin {
    @Shadow
    public abstract ConfigInventory getConfig();

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lappeng/util/ConfigInventory$Builder;build()Lappeng/util/ConfigInventory;"))
    private ConfigInventory appliedthaumics$restrictFilters(ConfigInventory.Builder builder) {
        if ((Object) this instanceof EssentiaLevelEmitterPart) {
            return builder.supportedType(EssentiaKeyType.TYPE).build();
        }
        return builder.slotFilter((slot, key) -> !(key instanceof EssentiaKey)).build();
    }

    @Inject(method = "configureWatchers", at = @At("HEAD"))
    private void appliedthaumics$clearLegacyAspect(CallbackInfo ci) {
        if (!((Object) this instanceof EssentiaLevelEmitterPart) && getConfig().getKey(0) instanceof EssentiaKey) {
            getConfig().setStack(0, null);
        }
    }

    @Redirect(method = "updateReportingValue", at = @At(value = "INVOKE", target = "Lappeng/api/networking/storage/IStorageService;getCachedInventory()Lappeng/api/stacks/KeyCounter;"))
    private KeyCounter appliedthaumics$filterTotals(IStorageService storage) {
        boolean essentiaEmitter = (Object) this instanceof EssentiaLevelEmitterPart;
        KeyCounter filtered = new KeyCounter();
        for (var entry : storage.getCachedInventory()) {
            if ((entry.getKey() instanceof EssentiaKey) == essentiaEmitter) {
                filtered.add(entry.getKey(), entry.getLongValue());
            }
        }
        return filtered;
    }
}
