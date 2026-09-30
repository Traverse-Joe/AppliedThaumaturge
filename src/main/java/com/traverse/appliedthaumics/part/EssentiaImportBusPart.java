package com.traverse.appliedthaumics.part;

import appeng.api.networking.IGrid;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.parts.automation.IOBusPart;
import com.traverse.appliedthaumics.me.key.EssentiaKeyType;
import com.traverse.appliedthaumics.me.strategy.EssentiaImportStrategy;
import com.traverse.appliedthaumics.registry.ATMenus;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

public class EssentiaImportBusPart extends IOBusPart {
    @Nullable
    private EssentiaImportStrategy strategy;

    public EssentiaImportBusPart(IPartItem<?> partItem) {
        super(TickRates.ImportBus, Set.of(EssentiaKeyType.TYPE), partItem);
    }

    @Override
    protected boolean doBusWork(IGrid grid) {
        if (strategy == null) {
            BlockPos target = getHost().getBlockEntity().getBlockPos().relative(getSide());
            strategy = new EssentiaImportStrategy((ServerLevel) getLevel(), target, getSide().getOpposite());
        }
        EssentiaTransferContext context = new EssentiaTransferContext(
                grid.getStorageService(), grid.getEnergyService(), source, getOperationsPerTick(), getFilter());
        context.setInverted(isUpgradedWith(AEItems.INVERTER_CARD));
        strategy.transfer(context);
        return context.hasDoneWork();
    }

    @Override
    protected MenuType<?> getMenuType() {
        return ATMenus.ESSENTIA_IMPORT_BUS;
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(6, 6, 11, 10, 10, 13);
        bch.addBox(5, 5, 13, 11, 11, 14);
        bch.addBox(4, 4, 14, 12, 12, 16);
    }
}
