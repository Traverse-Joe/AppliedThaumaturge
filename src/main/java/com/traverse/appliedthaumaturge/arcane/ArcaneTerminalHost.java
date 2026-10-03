package com.traverse.appliedthaumaturge.arcane;

import appeng.api.storage.ITerminalHost;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneWorkbenchContext;
import appeng.util.inv.AppEngInternalInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public interface ArcaneTerminalHost extends ITerminalHost {
    AppEngInternalInventory getArcaneInventory();

    ArcaneWorkbenchContext getArcaneContext(ServerPlayer player);

    BlockPos getAuraPosition();

    boolean hasChargingCard();

    void saveArcaneInventory();
}
