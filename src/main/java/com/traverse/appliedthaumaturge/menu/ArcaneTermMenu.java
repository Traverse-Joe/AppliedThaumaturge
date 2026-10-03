package com.traverse.appliedthaumaturge.menu;

import appeng.api.config.Actionable;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.helpers.ICraftingGridMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.guisync.ClientActionKey;
import appeng.menu.me.common.MEStorageMenu;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.PlayerInternalInventory;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftingTransaction;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumaturge.content.item.ThaumometerItem;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.workbench.MenuArcaneWorkbench;
import com.leclowndu93150.thaumaturge.content.workbench.SlotCrystalEssentia;
import com.leclowndu93150.thaumaturge.content.workbench.SlotWorkbenchWand;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.codec.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.traverse.appliedthaumaturge.arcane.ArcaneCrafter;
import com.traverse.appliedthaumaturge.arcane.ArcaneTerminalHost;
import com.traverse.appliedthaumaturge.arcane.ArcaneTerminalAuraSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import com.traverse.appliedthaumaturge.part.ArcaneTerminalPart;
import com.traverse.appliedthaumaturge.wand.EntangledWands;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class ArcaneTermMenu extends MEStorageMenu implements ICraftingGridMenu, ArcaneRecipeTarget {
    private static final ClientActionKey<Void> ACTION_STORE_GRID = new ClientActionKey<>("storeCraftingGrid");
    private static final ClientActionKey<Void> ACTION_TAKE_GRID = new ClientActionKey<>("clearToPlayer");
    private static final ClientActionKey<Long> ACTION_SCAN_ITEM = new ClientActionKey<>("scanStoredItem");
    private final ArcaneTerminalHost arcaneHost;
    private final AppEngInternalInventory arcaneInventory;
    private final ArcaneResultSlot outputSlot;

    @GuiSync(40)
    public int visCost;
    @GuiSync(41)
    public boolean paymentAvailable;
    @GuiSync(42)
    public int baseVis;
    @GuiSync(43)
    public boolean crudeCost;
    @GuiSync(44)
    public int availableVis;

    private int auraRefresh;

    public ArcaneTermMenu(MenuType<?> type, int id, Inventory ip, ArcaneTerminalHost host) {
        super(type, id, ip, host);
        this.arcaneHost = host;
        this.arcaneInventory = host.getArcaneInventory();

        for (int i = 0; i < ArcaneTerminalPart.GRID_SLOTS; i++) {
            addSlot(new ArcaneSlot(this, arcaneInventory, i, stack -> true), SlotSemantics.CRAFTING_GRID);
        }
        for (int i = 0; i < ArcaneTerminalPart.CRYSTAL_SLOTS; i++) {
            ResourceKey<IAspect> primal = MenuArcaneWorkbench.PRIMAL_ORDER.get(i);
            addSlot(new ArcaneSlot(this, arcaneInventory, ArcaneTerminalPart.CRYSTAL_START + i, stack -> SlotCrystalEssentia.isValidCrystal(stack, primal)),
                    ATSlotSemantics.ARCANE_CRYSTALS);
        }
        addSlot(new ArcaneSlot(this, arcaneInventory, ArcaneTerminalPart.WAND_SLOT, SlotWorkbenchWand::isUsableWand).maxStack(1), ATSlotSemantics.ARCANE_WAND);


        InternalInventory grid = arcaneInventory.getSubInventory(0, ArcaneTerminalPart.GRID_SLOTS);
        this.outputSlot = new ArcaneResultSlot(getPlayerInventory().player, getActionSource(), energySource, host.getInventory(), grid, this);
        addSlot(outputSlot, SlotSemantics.CRAFTING_RESULT);

        registerClientAction(ACTION_STORE_GRID, this::clearCraftingGrid);
        registerClientAction(ACTION_TAKE_GRID, this::clearToPlayerInventory);
        registerClientAction(ACTION_SCAN_ITEM, ByteBufCodecs.VAR_LONG, this::scanStoredItem);

        updateOutput();
    }

    public ArcaneTerminalHost getArcaneHost() {
        return arcaneHost;
    }

    public boolean canCraft() {
        return isValidMenu() && stillValid(getPlayer()) && canInteractWithGrid();
    }

    public void scanStoredItem(long serial) {
        if (isClientSide()) {
            sendClientAction(ACTION_SCAN_ITEM, serial);
            return;
        }
        if (!isValidMenu() || !stillValid(getPlayer()) || !canInteractWithGrid()
                || !(getCarried().getItem() instanceof ThaumometerItem)) {
            return;
        }
        AEKey key = getStackBySerial(serial);
        if (!(key instanceof AEItemKey item) || !isKeyVisible(item)
                || arcaneHost.getInventory().extract(item, 1, Actionable.SIMULATE, getActionSource()) < 1) {
            return;
        }
        ScanningManager.scanTheThing(getPlayer(), item.toStack(1));
    }

    public void clearCraftingGrid() {
        if (isClientSide()) {
            sendClientAction(ACTION_STORE_GRID);
            return;
        }
        if (!canCraft()) {
            return;
        }
        InternalInventory grid = getCraftingMatrix();
        for (int slot = 0; slot < grid.size(); slot++) {
            ItemStack stack = grid.getStackInSlot(slot);
            AEItemKey key = AEItemKey.of(stack);
            if (key != null) {
                long inserted = StorageHelper.poweredInsert(energySource, arcaneHost.getInventory(), key, stack.getCount(), getActionSource(), Actionable.MODULATE);
                grid.setItemDirect(slot, stack.copyWithCount(stack.getCount() - (int) inserted));
            }
        }
        updateOutput();
        broadcastChanges();
    }

    public void clearToPlayerInventory() {
        if (isClientSide()) {
            sendClientAction(ACTION_TAKE_GRID);
            return;
        }
        if (!isValidMenu() || !stillValid(getPlayer())) {
            return;
        }
        InternalInventory grid = getCraftingMatrix();
        PlayerInternalInventory playerInventory = new PlayerInternalInventory(getPlayerInventory());
        for (int slot = 0; slot < grid.size(); slot++) {
            for (int pass = 0; pass < 2; pass++) {
                for (int destination = 0; destination < 36; destination++) {
                    int playerSlot = destination < 9 ? 8 - destination : destination;
                    if (playerInventory.getStackInSlot(playerSlot).isEmpty() == (pass == 1)) {
                        grid.setItemDirect(slot, playerInventory.getSlotInv(playerSlot).addItems(grid.getStackInSlot(slot)));
                    }
                }
            }
        }
        updateOutput();
        broadcastChanges();
    }

    public AppEngInternalInventory getArcaneInventory() {
        return arcaneInventory;
    }

    @Override
    public void slotsChanged(Container inventory) {
        updateOutput();
    }

    void updateOutput() {
        if (isClientSide() || !(getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        ArcaneCraftingTransaction.Inspection inspection = ArcaneCrafter.inspect(player, arcaneHost, arcaneInventory);
        boolean valid = inspection != null && inspection.successful();
        outputSlot.set(valid ? inspection.output() : ItemStack.EMPTY);
        baseVis = valid && inspection.requirements() != null ? inspection.requirements().baseVis() : 0;
        ArcaneCraftingTransaction.Result cost = valid ? ArcaneCrafter.previewCost(player, arcaneHost, arcaneInventory) : null;
        visCost = cost == null || cost.cost() == null ? 0 : cost.cost().auraVis();
        crudeCost = cost != null && cost.cost() != null && !cost.cost().crystalsNeeded().isEmpty();
        paymentAvailable = cost != null && cost.successful();
    }

    @Override
    public void fillFromRecipe(List<List<ItemStack>> wanted) {
        if (isClientSide() || !canCraft()) {
            return;
        }
        MEStorage storage = arcaneHost.getInventory();
        for (int i = 0; i < wanted.size() && i < ArcaneTerminalPart.GRID_SLOTS + ArcaneTerminalPart.CRYSTAL_SLOTS; i++) {
            int slot = i < ArcaneTerminalPart.GRID_SLOTS ? i : ArcaneTerminalPart.CRYSTAL_START + (i - ArcaneTerminalPart.GRID_SLOTS);
            List<ItemStack> candidates = wanted.get(i).stream().filter(stack -> !stack.isEmpty()).toList();
            ItemStack current = arcaneInventory.getStackInSlot(slot);
            if (!current.isEmpty() && candidates.stream().anyMatch(c -> ItemStack.isSameItemSameComponents(c, current))) {
                continue;
            }
            if (!current.isEmpty()) {
                arcaneInventory.setItemDirect(slot, ItemStack.EMPTY);
                returnToPlayerOrNetwork(current, storage);
            }
            for (ItemStack candidate : candidates) {
                int amount = slot < ArcaneTerminalPart.GRID_SLOTS ? 1 : Math.max(1, candidate.getCount());
                ItemStack taken = takeFromNetwork(candidate, amount, storage);
                if (taken.isEmpty()) {
                    taken = takeFromPlayer(candidate, amount);
                }
                if (!taken.isEmpty()) {
                    arcaneInventory.setItemDirect(slot, taken);
                    break;
                }
            }
        }
        updateOutput();
        broadcastChanges();
    }

    private ItemStack takeFromNetwork(ItemStack template, int amount, MEStorage storage) {
        AEItemKey key = AEItemKey.of(template);
        if (key == null) {
            return ItemStack.EMPTY;
        }
        long extracted = StorageHelper.poweredExtraction(energySource, storage, key, amount, getActionSource(), Actionable.MODULATE);
        return extracted <= 0 ? ItemStack.EMPTY : key.toStack((int) extracted);
    }

    private ItemStack takeFromPlayer(ItemStack template, int amount) {
        Inventory inventory = getPlayerInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, template)) {
                return stack.split(Math.min(amount, stack.getCount()));
            }
        }
        return ItemStack.EMPTY;
    }

    private void returnToPlayerOrNetwork(ItemStack stack, MEStorage storage) {
        AEItemKey key = AEItemKey.of(stack);
        long inserted = key == null ? 0 : StorageHelper.poweredInsert(energySource, storage, key, stack.getCount(), getActionSource(), Actionable.MODULATE);
        ItemStack left = stack.copyWithCount(stack.getCount() - (int) inserted);
        if (!left.isEmpty() && !getPlayerInventory().add(left)) {
            getPlayer().drop(left, false);
        }
    }

    @Override
    public void broadcastChanges() {
        if (!isClientSide()) {
            boolean wandChanged = EntangledWands.mirror(arcaneInventory.getStackInSlot(ArcaneTerminalPart.WAND_SLOT));
            if (wandChanged) {
                arcaneHost.saveArcaneInventory();
            }
            boolean refreshAura = auraRefresh-- <= 0;
            if (refreshAura) {
                auraRefresh = 10;
                availableVis = measureAura();
            }
            if (wandChanged || refreshAura) {
                updateOutput();
            }
        }
        super.broadcastChanges();
    }

    private int measureAura() {
        Level level = getPlayer().level();
        BlockPos pos = arcaneHost.getAuraPosition();
        float total = 0;
        for (BlockPos anchor : ArcaneTerminalAuraSource.anchors(pos, arcaneHost.hasChargingCard())) {
            total += AuraHelper.getVis(level, anchor);
        }
        return (int) total;
    }

    @Override
    public InternalInventory getCraftingMatrix() {
        return arcaneInventory.getSubInventory(0, ArcaneTerminalPart.GRID_SLOTS);
    }

    public static final class ArcaneSlot extends appeng.menu.slot.AppEngSlot {
        private final ArcaneTermMenu menu;
        private final Predicate<ItemStack> filter;
        private int max = 64;

        ArcaneSlot(ArcaneTermMenu menu, InternalInventory inv, int slot, Predicate<ItemStack> filter) {
            super(inv, slot);
            this.menu = menu;
            this.filter = filter;
        }

        ArcaneSlot maxStack(int max) {
            this.max = max;
            return this;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return filter.test(stack) && super.mayPlace(stack);
        }

        @Override
        public int getMaxStackSize() {
            return Math.min(max, super.getMaxStackSize());
        }

        @Override
        public void setChanged() {
            super.setChanged();
            menu.updateOutput();
        }
    }
}
