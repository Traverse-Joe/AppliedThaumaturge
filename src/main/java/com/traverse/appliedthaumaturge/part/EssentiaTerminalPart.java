package com.traverse.appliedthaumaturge.part;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.util.KeyTypeSelection;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractTerminalPart;
import com.traverse.appliedthaumaturge.AppliedThaumaturge;
import com.traverse.appliedthaumaturge.me.key.EssentiaKeyType;
import net.minecraft.resources.ResourceLocation;

public class EssentiaTerminalPart extends AbstractTerminalPart {
    @PartModels
    public static final ResourceLocation MODEL_OFF = AppliedThaumaturge.id("part/essentia_terminal_off");
    @PartModels
    public static final ResourceLocation MODEL_ON = AppliedThaumaturge.id("part/essentia_terminal_on");
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

    private final KeyTypeSelection essentiaOnly = new KeyTypeSelection(() -> {
    }, type -> type == EssentiaKeyType.TYPE);

    public EssentiaTerminalPart(IPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    public KeyTypeSelection getKeyTypeSelection() {
        return essentiaOnly;
    }

    @Override
    public IPartModel getStaticModels() {
        return selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }
}
