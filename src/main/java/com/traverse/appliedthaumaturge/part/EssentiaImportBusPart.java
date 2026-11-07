package com.traverse.appliedthaumaturge.part;

import appeng.api.networking.IGrid;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import com.traverse.appliedthaumaturge.AppliedThaumaturge;
import net.minecraft.resources.ResourceLocation;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.parts.automation.IOBusPart;
import com.traverse.appliedthaumaturge.me.key.EssentiaKeyType;
import com.traverse.appliedthaumaturge.me.strategy.EssentiaImportStrategy;
import com.traverse.appliedthaumaturge.registry.ATMenus;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

public class EssentiaImportBusPart extends IOBusPart {
    @PartModels
    public static final ResourceLocation MODEL_BASE = AppliedThaumaturge.id("part/essentia_import_bus_base");
    @PartModels
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, AppEng.makeId("part/import_bus_off"));
    @PartModels
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, AppEng.makeId("part/import_bus_on"));
    @PartModels
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, AppEng.makeId("part/import_bus_has_channel"));

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
    public IPartModel getStaticModels() {
        if (isActive() && isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (isPowered()) {
            return MODELS_ON;
        }
        return MODELS_OFF;
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
