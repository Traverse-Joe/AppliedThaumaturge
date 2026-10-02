package com.traverse.appliedthaumaturge.part;

import appeng.api.parts.IPartItem;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.parts.automation.StorageLevelEmitterPart;
import appeng.util.ConfigInventory;
import com.traverse.appliedthaumaturge.me.key.EssentiaKey;
import com.traverse.appliedthaumaturge.registry.ATMenus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class EssentiaLevelEmitterPart extends StorageLevelEmitterPart {
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
    public boolean onUseWithoutItem(Player player, Vec3 pos) {
        if (!isClientSide()) {
            MenuOpener.open(ATMenus.ESSENTIA_LEVEL_EMITTER, player, MenuLocators.forPart(this));
        }
        return true;
    }
}
