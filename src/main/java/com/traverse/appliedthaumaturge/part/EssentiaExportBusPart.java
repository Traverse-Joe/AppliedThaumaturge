package com.traverse.appliedthaumaturge.part;

import appeng.api.networking.IGrid;
import appeng.api.config.Actionable;
import appeng.api.config.RedstoneMode;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.RegisterPartCapabilitiesEvent;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageHelper;
import appeng.core.settings.TickRates;
import appeng.parts.automation.IOBusPart;
import appeng.util.prioritylist.DefaultPriorityList;
import com.traverse.appliedthaumaturge.me.key.EssentiaKeyType;
import com.traverse.appliedthaumaturge.me.key.EssentiaKey;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.traverse.appliedthaumaturge.me.strategy.EssentiaExportStrategy;
import com.traverse.appliedthaumaturge.registry.ATMenus;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class EssentiaExportBusPart extends IOBusPart implements IEssentiaTransport {
    @Nullable
    private EssentiaExportStrategy strategy;
    private int nextSlot;
    private long pullBudget;
    private RedstoneMode pullMode = RedstoneMode.IGNORE;
    private boolean pullDidWork;
    private boolean transferring;

    public static void registerCapabilities(RegisterPartCapabilitiesEvent event) {
        event.register(EssentiaCapabilities.TRANSPORT, (part, side) -> part.isConnectable(side) ? part : null,
                EssentiaExportBusPart.class);
    }

    public EssentiaExportBusPart(IPartItem<?> partItem) {
        super(TickRates.ExportBus, Set.of(EssentiaKeyType.TYPE), partItem);
    }

    @Override
    protected boolean doBusWork(IGrid grid) {
        if (strategy == null) {
            BlockPos target = getHost().getBlockEntity().getBlockPos().relative(getSide());
            strategy = new EssentiaExportStrategy((ServerLevel) getLevel(), target, getSide().getOpposite());
        }
        EssentiaTransferContext context = new EssentiaTransferContext(
                grid.getStorageService(), grid.getEnergyService(), source, getOperationsPerTick(), DefaultPriorityList.INSTANCE);
        int slots = availableSlots();
        int x;
        for (x = 0; x < slots && context.hasOperationsLeft(); x++) {
            AEKey what = getConfig().getKey((nextSlot + x) % slots);
            if (what == null) {
                continue;
            }
            int perOp = what.getAmountPerOperation();
            long moved = strategy.transfer(context, what, (long) context.getOperationsRemaining() * perOp);
            if (moved > 0) {
                context.reduceOperationsRemaining(Math.max(1, moved / perOp));
            }
        }
        if (context.hasDoneWork() && slots > 0) {
            nextSlot = (nextSlot + x) % slots;
        }
        pullBudget = context.getOperationsRemaining();
        pullMode = getRSMode();
        boolean didWork = context.hasDoneWork() || pullDidWork;
        pullDidWork = false;
        return didWork;
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face != null && face == getSide();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return false;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return isConnectable(face) && pullBudget > 0 && pullMode == getRSMode() && canDoBusWork()
                && (getRSMode() == RedstoneMode.SIGNAL_PULSE || !isSleeping());
    }

    @Override
    public void setSuction(Holder<IAspect> aspect, int amount) {
    }

    @Nullable
    @Override
    public Holder<IAspect> getSuctionType(Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return 0;
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return takeEssentia(aspect, amount, face, false);
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face, boolean simulate) {
        EssentiaKey key = EssentiaKey.of(aspect);
        if (amount <= 0 || key == null || transferring || !canOutputTo(face)) {
            return 0;
        }
        boolean configured = false;
        for (int slot = 0; slot < availableSlots(); slot++) {
            if (key.equals(getConfig().getKey(slot))) {
                configured = true;
                break;
            }
        }
        IGrid grid = getMainNode().getGrid();
        if (!configured || grid == null) {
            return 0;
        }
        transferring = true;
        try {
            long limit = Math.min(amount, pullBudget * key.getAmountPerOperation());
            int extracted = (int) StorageHelper.poweredExtraction(grid.getEnergyService(),
                    grid.getStorageService().getInventory(), key, limit, source,
                    simulate ? Actionable.SIMULATE : Actionable.MODULATE);
            if (!simulate && extracted > 0) {
                pullBudget -= Math.max(1, (extracted + key.getAmountPerOperation() - 1L) / key.getAmountPerOperation());
                pullDidWork = true;
            }
            return extracted;
        } finally {
            transferring = false;
        }
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face, boolean simulate) {
        return 0;
    }

    @Nullable
    @Override
    public Holder<IAspect> getEssentiaType(Direction face) {
        if (!canOutputTo(face)) {
            return null;
        }
        for (int slot = 0; slot < availableSlots(); slot++) {
            if (getConfig().getKey(slot) instanceof EssentiaKey key) {
                Holder<IAspect> aspect = key.holderOrNull(getLevel().registryAccess());
                if (aspect != null && takeEssentia(aspect, 1, face, true) > 0) {
                    return aspect;
                }
            }
        }
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        Holder<IAspect> aspect = getEssentiaType(face);
        return aspect == null ? 0 : takeEssentia(aspect, Integer.MAX_VALUE, face, true);
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public void readFromNBT(ValueInput extra) {
        super.readFromNBT(extra);
        nextSlot = extra.getIntOr("nextSlot", 0);
    }

    @Override
    public void writeToNBT(ValueOutput extra) {
        super.writeToNBT(extra);
        extra.putInt("nextSlot", nextSlot);
    }

    @Override
    protected MenuType<?> getMenuType() {
        return ATMenus.ESSENTIA_EXPORT_BUS;
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(4, 4, 12, 12, 12, 14);
        bch.addBox(5, 5, 14, 11, 11, 15);
        bch.addBox(6, 6, 15, 10, 10, 16);
        bch.addBox(6, 6, 11, 10, 10, 12);
    }
}
