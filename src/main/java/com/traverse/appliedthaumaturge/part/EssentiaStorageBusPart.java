package com.traverse.appliedthaumaturge.part;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import com.traverse.appliedthaumaturge.AppliedThaumaturge;
import net.minecraft.resources.ResourceLocation;
import appeng.parts.storagebus.StorageBusPart;

public class EssentiaStorageBusPart extends StorageBusPart {
    @PartModels
    public static final ResourceLocation MODEL_BASE = AppliedThaumaturge.id("part/essentia_storage_bus_base");
    @PartModels
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, AppEng.makeId("part/storage_bus_off"));
    @PartModels
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, AppEng.makeId("part/storage_bus_on"));
    @PartModels
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, AppEng.makeId("part/storage_bus_has_channel"));

    public EssentiaStorageBusPart(IPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    public IPartModel getStaticModels() {
        if (isActive() && isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (isPowered()) {
            return MODELS_ON;
        }
        return MODELS_OFF;
    }
}
