package com.traverse.appliedthaumics.me.strategy;

import appeng.api.behaviors.StackImportStrategy;
import appeng.api.behaviors.StackTransferContext;
import appeng.api.config.Actionable;
import appeng.api.storage.MEStorage;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.traverse.appliedthaumics.me.key.EssentiaKey;
import com.traverse.appliedthaumics.me.key.EssentiaKeyType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class EssentiaImportStrategy implements StackImportStrategy {
    private final EssentiaEndpoint.Lookup lookup;

    public EssentiaImportStrategy(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        this.lookup = EssentiaEndpoint.lookup(level, fromPos, fromSide);
    }

    @Override
    public boolean transfer(StackTransferContext context) {
        if (!context.isKeyTypeEnabled(EssentiaKeyType.TYPE)) {
            return false;
        }
        EssentiaEndpoint endpoint = lookup.find();
        if (endpoint == null) {
            return false;
        }
        MEStorage inv = context.getInternalStorage().getInventory();
        int perOp = EssentiaKeyType.TYPE.getAmountPerOperation();
        for (AspectInstance entry : endpoint.contents().entries()) {
            if (!context.hasOperationsLeft()) {
                break;
            }
            EssentiaKey key = EssentiaKey.of(entry.aspect());
            if (key == null || context.isInFilter(key) == context.isInverted()) {
                continue;
            }
            long budget = (long) context.getOperationsRemaining() * perOp;
            int available = endpoint.extract(entry.aspect(), EssentiaExternalStorageStrategy.clamp(budget), true);
            if (available <= 0) {
                continue;
            }
            long accepted = inv.insert(key, available, Actionable.SIMULATE, context.getActionSource());
            if (accepted <= 0) {
                continue;
            }
            int taken = endpoint.extract(entry.aspect(), (int) accepted, false);
            if (taken <= 0) {
                continue;
            }
            long inserted = inv.insert(key, taken, Actionable.MODULATE, context.getActionSource());
            if (inserted < taken) {
                endpoint.insert(entry.aspect(), (int) (taken - inserted), false);
            }
            context.reduceOperationsRemaining(Math.max(1, (inserted + perOp - 1) / perOp));
        }
        return false;
    }
}
