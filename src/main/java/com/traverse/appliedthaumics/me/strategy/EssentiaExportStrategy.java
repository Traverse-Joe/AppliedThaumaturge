package com.traverse.appliedthaumics.me.strategy;

import appeng.api.behaviors.StackExportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.traverse.appliedthaumics.me.key.EssentiaKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;

public class EssentiaExportStrategy implements StackExportStrategy {
    private final EssentiaEndpoint.Lookup lookup;
    private final ServerLevel level;

    public EssentiaExportStrategy(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        this.lookup = EssentiaEndpoint.lookup(level, fromPos, fromSide);
        this.level = level;
    }

    @Override
    public long transfer(StackTransferContext context, AEKey what, long amount) {
        if (!(what instanceof EssentiaKey key)) {
            return 0;
        }
        EssentiaEndpoint endpoint = lookup.find();
        Holder<IAspect> aspect = key.holderOrNull(level.registryAccess());
        if (endpoint == null || aspect == null) {
            return 0;
        }
        MEStorage inv = context.getInternalStorage().getInventory();
        long available = StorageHelper.poweredExtraction(context.getEnergySource(), inv, what, amount, context.getActionSource(), Actionable.SIMULATE);
        int accepted = endpoint.insert(aspect, EssentiaExternalStorageStrategy.clamp(available), true);
        if (accepted <= 0) {
            return 0;
        }
        long extracted = StorageHelper.poweredExtraction(context.getEnergySource(), inv, what, accepted, context.getActionSource(), Actionable.MODULATE);
        if (extracted <= 0) {
            return 0;
        }
        int inserted = endpoint.insert(aspect, (int) extracted, false);
        if (inserted < extracted) {
            inv.insert(what, extracted - inserted, Actionable.MODULATE, context.getActionSource());
        }
        return inserted;
    }

    @Override
    public long push(AEKey what, long amount, Actionable mode) {
        if (!(what instanceof EssentiaKey key)) {
            return 0;
        }
        EssentiaEndpoint endpoint = lookup.find();
        Holder<IAspect> aspect = key.holderOrNull(level.registryAccess());
        if (endpoint == null || aspect == null) {
            return 0;
        }
        return endpoint.insert(aspect, EssentiaExternalStorageStrategy.clamp(amount), mode.isSimulate());
    }
}
