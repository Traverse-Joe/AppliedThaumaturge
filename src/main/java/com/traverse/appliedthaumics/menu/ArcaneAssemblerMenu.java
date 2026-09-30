package com.traverse.appliedthaumics.menu;

import appeng.api.inventories.InternalInventory;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.interfaces.IProgressProvider;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.OutputSlot;
import com.leclowndu93150.thaumaturge.content.workbench.SlotWorkbenchWand;
import com.traverse.appliedthaumics.assembler.ArcaneAssemblerBlockEntity;
import com.traverse.appliedthaumics.knowledge.KnowledgeCores;
import com.traverse.appliedthaumics.wand.EntangledWands;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class ArcaneAssemblerMenu extends UpgradeableMenu<ArcaneAssemblerBlockEntity> implements IProgressProvider {
    @GuiSync(60)
    public int craftProgress;
    @GuiSync(61)
    public int availableVis;

    private int auraRefresh;

    public ArcaneAssemblerMenu(MenuType<?> type, int id, Inventory ip, ArcaneAssemblerBlockEntity host) {
        super(type, id, ip, host);
    }

    @Override
    protected void setupConfig() {
        InternalInventory inv = getHost().getInventory();
        for (int i = 0; i < 9; i++) {
            addSlot(new DisplaySlot(inv, ArcaneAssemblerBlockEntity.GRID_START + i), SlotSemantics.MACHINE_CRAFTING_GRID);
        }
        addSlot(new WandSlot(inv, ArcaneAssemblerBlockEntity.WAND_SLOT), ATSlotSemantics.ARCANE_WAND);
        addSlot(new CoreSlot(inv, ArcaneAssemblerBlockEntity.CORE_SLOT), SlotSemantics.ENCODED_PATTERN);
        addSlot(new OutputSlot(inv, ArcaneAssemblerBlockEntity.OUTPUT_SLOT, null), SlotSemantics.MACHINE_OUTPUT);
    }

    @Override
    public void broadcastChanges() {
        craftProgress = getHost().getProgress();
        if (!isClientSide()) {
            if (EntangledWands.mirror(getHost().getInventory().getStackInSlot(ArcaneAssemblerBlockEntity.WAND_SLOT))) {
                getHost().saveChanges();
            }
            if (auraRefresh-- <= 0) {
                auraRefresh = 10;
                availableVis = getHost().getAvailableVis();
            }
        }
        super.broadcastChanges();
    }

    @Override
    public int getCurrentProgress() {
        return craftProgress;
    }

    @Override
    public int getMaxProgress() {
        return 100;
    }

    private static final class DisplaySlot extends AppEngSlot {
        DisplaySlot(InternalInventory inv, int slot) {
            super(inv, slot);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }

    private static final class WandSlot extends AppEngSlot {
        WandSlot(InternalInventory inv, int slot) {
            super(inv, slot);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return SlotWorkbenchWand.isUsableWand(stack) && super.mayPlace(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    private static final class CoreSlot extends AppEngSlot {
        CoreSlot(InternalInventory inv, int slot) {
            super(inv, slot);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return KnowledgeCores.isCore(stack) && !KnowledgeCores.isBlank(stack) && super.mayPlace(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
