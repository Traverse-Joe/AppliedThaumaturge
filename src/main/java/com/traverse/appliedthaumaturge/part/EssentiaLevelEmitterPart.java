package com.traverse.appliedthaumaturge.part;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import com.traverse.appliedthaumaturge.AppliedThaumaturge;
import net.minecraft.resources.ResourceLocation;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.parts.automation.StorageLevelEmitterPart;
import appeng.util.ConfigInventory;
import com.traverse.appliedthaumaturge.me.key.EssentiaKey;
import com.traverse.appliedthaumaturge.registry.ATMenus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class EssentiaLevelEmitterPart extends StorageLevelEmitterPart {
    @PartModels
    public static final ResourceLocation ESSENTIA_BASE_OFF = AppliedThaumaturge.id("part/essentia_level_emitter_base_off");
    @PartModels
    public static final ResourceLocation ESSENTIA_BASE_ON = AppliedThaumaturge.id("part/essentia_level_emitter_base_on");
    private static final PartModel ESSENTIA_OFF_OFF = new PartModel(ESSENTIA_BASE_OFF, MODEL_STATUS_OFF);
    private static final PartModel ESSENTIA_OFF_ON = new PartModel(ESSENTIA_BASE_OFF, MODEL_STATUS_ON);
    private static final PartModel ESSENTIA_OFF_HAS_CHANNEL = new PartModel(ESSENTIA_BASE_OFF, MODEL_STATUS_HAS_CHANNEL);
    private static final PartModel ESSENTIA_ON_OFF = new PartModel(ESSENTIA_BASE_ON, MODEL_STATUS_OFF);
    private static final PartModel ESSENTIA_ON_ON = new PartModel(ESSENTIA_BASE_ON, MODEL_STATUS_ON);
    private static final PartModel ESSENTIA_ON_HAS_CHANNEL = new PartModel(ESSENTIA_BASE_ON, MODEL_STATUS_HAS_CHANNEL);

    public EssentiaLevelEmitterPart(IPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    protected void onReportingValueChanged() {
        super.onReportingValueChanged();
        getHost().markForSave();
    }

    @Override
    protected void configureWatchers() {
        ConfigInventory config = getConfig();
        if (config.getKey(0) != null && !(config.getKey(0) instanceof EssentiaKey)) {
            config.setStack(0, null);
        }
        super.configureWatchers();
    }

    @Override
    public IPartModel getStaticModels() {
        if (isActive() && isPowered()) {
            return isLevelEmitterOn() ? ESSENTIA_ON_HAS_CHANNEL : ESSENTIA_OFF_HAS_CHANNEL;
        } else if (isPowered()) {
            return isLevelEmitterOn() ? ESSENTIA_ON_ON : ESSENTIA_OFF_ON;
        }
        return isLevelEmitterOn() ? ESSENTIA_ON_OFF : ESSENTIA_OFF_OFF;
    }

    @Override
    public boolean onUseWithoutItem(Player player, Vec3 pos) {
        if (!isClientSide()) {
            MenuOpener.open(ATMenus.ESSENTIA_LEVEL_EMITTER, player, MenuLocators.forPart(this));
        }
        return true;
    }
}
