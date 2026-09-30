package com.traverse.appliedthaumics.block.entity;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaStorage;
import com.traverse.appliedthaumics.me.key.EssentiaKey;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class EssentiaNetworkBlockEntity extends AENetworkedBlockEntity {
    private final IActionSource source = IActionSource.ofMachine(this);
    private final NetworkEssentiaStorage storage = new NetworkEssentiaStorage(this);
    private boolean busy;

    public IEssentiaStorage storage() {
        return storage;
    }

    long networkRevision() {
        return level == null ? 0 : level.getGameTime();
    }

    protected EssentiaNetworkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL).setIdlePowerUsage(1.0);
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        markForUpdate();
    }

    @Nullable
    protected IGrid activeGrid() {
        if (level == null || level.isClientSide() || !getMainNode().isActive()) {
            return null;
        }
        return getMainNode().getGrid();
    }

    public boolean isNetworkActive() {
        return activeGrid() != null;
    }

    protected long networkAmount(Holder<IAspect> aspect) {
        IGrid grid = activeGrid();
        EssentiaKey key = EssentiaKey.of(aspect);
        if (grid == null || key == null) {
            return 0;
        }
        return grid.getStorageService().getCachedInventory().get(key);
    }

    protected AspectList networkContents() {
        IGrid grid = activeGrid();
        if (grid == null || level == null) {
            return AspectList.EMPTY;
        }
        AspectList out = AspectList.EMPTY;
        KeyCounter stored = grid.getStorageService().getCachedInventory();
        for (Object2LongMap.Entry<AEKey> entry : stored) {
            if (entry.getKey() instanceof EssentiaKey key && entry.getLongValue() > 0) {
                Holder<IAspect> aspect = key.holderOrNull(level.registryAccess());
                if (aspect != null) {
                    out = out.add(aspect, (int) Math.min(Integer.MAX_VALUE, entry.getLongValue()));
                }
            }
        }
        return out;
    }

    protected int extractFromNetwork(Holder<IAspect> aspect, int amount, Actionable mode) {
        IGrid grid = activeGrid();
        EssentiaKey key = EssentiaKey.of(aspect);
        if (grid == null || key == null || amount <= 0 || busy) {
            return 0;
        }
        busy = true;
        try {
            MEStorage inv = grid.getStorageService().getInventory();
            return (int) StorageHelper.poweredExtraction(grid.getEnergyService(), inv, key, amount, source, mode);
        } finally {
            busy = false;
        }
    }

    protected int insertIntoNetwork(Holder<IAspect> aspect, int amount, Actionable mode) {
        IGrid grid = activeGrid();
        EssentiaKey key = EssentiaKey.of(aspect);
        if (grid == null || key == null || amount <= 0 || busy) {
            return 0;
        }
        busy = true;
        try {
            MEStorage inv = grid.getStorageService().getInventory();
            return (int) StorageHelper.poweredInsert(grid.getEnergyService(), inv, key, amount, source, mode);
        } finally {
            busy = false;
        }
    }
}
