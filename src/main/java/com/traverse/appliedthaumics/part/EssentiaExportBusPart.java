package com.traverse.appliedthaumics.part;

import appeng.api.networking.IGrid;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.stacks.AEKey;
import appeng.core.settings.TickRates;
import appeng.parts.automation.IOBusPart;
import appeng.util.prioritylist.DefaultPriorityList;
import com.traverse.appliedthaumics.me.key.EssentiaKeyType;
import com.traverse.appliedthaumics.me.strategy.EssentiaExportStrategy;
import com.traverse.appliedthaumics.registry.ATMenus;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class EssentiaExportBusPart extends IOBusPart {
    @Nullable
    private EssentiaExportStrategy strategy;
    private int nextSlot;

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
        return context.hasDoneWork();
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
