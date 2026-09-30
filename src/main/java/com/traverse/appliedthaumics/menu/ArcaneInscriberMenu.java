package com.traverse.appliedthaumics.menu;

import appeng.menu.SlotSemantics;
import net.minecraft.network.codec.ByteBufCodecs;
import appeng.menu.guisync.ClientActionKey;
import appeng.menu.guisync.GuiSync;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.FakeSlot;
import appeng.util.inv.AppEngInternalInventory;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftingTransaction;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneWorkbenchContext;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingInput;
import com.traverse.appliedthaumics.knowledge.KnowledgeCoreContents;
import com.traverse.appliedthaumics.knowledge.KnowledgeCores;
import com.traverse.appliedthaumics.knowledge.KnowledgeRecipe;
import com.traverse.appliedthaumics.part.ArcaneInscriberPart;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ArcaneInscriberMenu extends MEStorageMenu implements ArcaneRecipeTarget {
    private static final ClientActionKey<Void> ACTION_ENCODE = new ClientActionKey<>("encode");
    private static final ClientActionKey<Void> ACTION_ERASE = new ClientActionKey<>("erase");
    private static final ClientActionKey<Void> ACTION_CLEAR = new ClientActionKey<>("clear");
    private static final ClientActionKey<Boolean> ACTION_SUBSTITUTE = new ClientActionKey<>("substitute");

    private final ArcaneInscriberPart part;
    private final AppEngInternalInventory preview = new AppEngInternalInventory(1);
    private final FakeSlot[] patternSlots = new FakeSlot[9];
    private int lastStateKey;

    @GuiSync(51)
    public boolean coreInserted;
    @GuiSync(52)
    public boolean alreadyStored;
    @GuiSync(53)
    public boolean substitute;

    public ArcaneInscriberMenu(MenuType<?> type, int id, Inventory ip, ArcaneInscriberPart host) {
        super(type, id, ip, host);
        this.part = host;
        for (int i = 0; i < 9; i++) {
            patternSlots[i] = new PatternSlot(this, host.getPattern(), i);
            addSlot(patternSlots[i], SlotSemantics.CRAFTING_GRID);
        }
        addSlot(new CoreSlot(this, host.getCore()), ATSlotSemantics.KNOWLEDGE_CORE);
        addSlot(new PreviewSlot(preview), SlotSemantics.CRAFTING_RESULT);
        registerClientAction(ACTION_ENCODE, this::encode);
        registerClientAction(ACTION_ERASE, this::erase);
        registerClientAction(ACTION_CLEAR, this::clear);
        registerClientAction(ACTION_SUBSTITUTE, ByteBufCodecs.BOOL, this::setSubstitute);
        refresh();
    }

    public void encode() {
        if (isClientSide()) {
            sendClientAction(ACTION_ENCODE);
            return;
        }
        ArcaneCraftingTransaction.Inspection inspection = inspect();
        ItemStack core = part.getCore().getStackInSlot(0);
        if (inspection == null || !inspection.successful() || inspection.recipeId() == null || !KnowledgeCores.isCore(core)) {
            return;
        }
        List<ItemStack> grid = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) {
            grid.add(part.getPattern().getStackInSlot(i).copy());
        }
        KnowledgeRecipe recipe = new KnowledgeRecipe(inspection.recipeId(), grid, inspection.output(), part.isSubstitute());
        if (!KnowledgeCores.canWrite(core, recipe)) {
            return;
        }
        part.getCore().setItemDirect(0, KnowledgeCores.write(core, recipe));
        refresh();
    }

    public void erase() {
        if (isClientSide()) {
            sendClientAction(ACTION_ERASE);
            return;
        }
        ItemStack core = part.getCore().getStackInSlot(0);
        ItemStack output = preview.getStackInSlot(0);
        if (!KnowledgeCores.isCore(core) || output.isEmpty()) {
            return;
        }
        int index = KnowledgeCores.contents(core).indexOfOutput(output);
        if (index >= 0) {
            part.getCore().setItemDirect(0, KnowledgeCores.erase(core, index));
            refresh();
        }
    }

    public void clear() {
        if (isClientSide()) {
            sendClientAction(ACTION_CLEAR);
            return;
        }
        for (int i = 0; i < 9; i++) {
            part.getPattern().setItemDirect(i, ItemStack.EMPTY);
        }
        refresh();
    }

    public void setSubstitute(boolean value) {
        if (isClientSide()) {
            sendClientAction(ACTION_SUBSTITUTE, value);
            return;
        }
        part.setSubstitute(value);
        substitute = value;
    }

    @Override
    public void fillFromRecipe(List<List<ItemStack>> slots) {
        if (isClientSide()) {
            return;
        }
        for (int i = 0; i < 9; i++) {
            List<ItemStack> candidates = i < slots.size() ? slots.get(i) : List.of();
            ItemStack first = candidates.stream().filter(s -> !s.isEmpty()).findFirst().orElse(ItemStack.EMPTY);
            part.getPattern().setItemDirect(i, first.isEmpty() ? ItemStack.EMPTY : first.copyWithCount(1));
        }
        refresh();
        broadcastChanges();
    }

    @Override
    public void slotsChanged(Container inventory) {
        refresh();
    }

    @Override
    public void broadcastChanges() {
        if (!isClientSide()) {
            int key = stateKey();
            if (key != lastStateKey) {
                refresh();
            }
        }
        super.broadcastChanges();
    }

    private int stateKey() {
        int key = ItemStack.hashItemAndComponents(part.getCore().getStackInSlot(0));
        for (int i = 0; i < 9; i++) {
            key = 31 * key + ItemStack.hashItemAndComponents(part.getPattern().getStackInSlot(i));
        }
        return key;
    }

    void refresh() {
        if (isClientSide()) {
            return;
        }
        lastStateKey = stateKey();
        ArcaneCraftingTransaction.Inspection inspection = inspect();
        ItemStack output = inspection != null && inspection.successful() ? inspection.output() : ItemStack.EMPTY;
        preview.setItemDirect(0, output);
        ItemStack core = part.getCore().getStackInSlot(0);
        coreInserted = KnowledgeCores.isCore(core);
        KnowledgeCoreContents contents = KnowledgeCores.contents(core);
        alreadyStored = !output.isEmpty() && contents.indexOfOutput(output) >= 0;
        substitute = part.isSubstitute();
    }

    @Nullable
    private ArcaneCraftingTransaction.Inspection inspect() {
        if (!(getPlayer() instanceof ServerPlayer player)) {
            return null;
        }
        List<ItemStack> items = new ArrayList<>(16);
        for (int i = 0; i < 9; i++) {
            items.add(part.getPattern().getStackInSlot(i).copy());
        }
        for (int i = 0; i < 7; i++) {
            items.add(ItemStack.EMPTY);
        }
        ArcaneCraftingInput input = ArcaneCraftingInput.of(3, 3, items).withPlayer(player);
        if (input.isEmpty()) {
            return null;
        }
        ArcaneWorkbenchContext context = ArcaneWorkbenchContext.placed(player, part.getHost().getBlockEntity().getBlockPos(), part.getHostId(), null);
        return ArcaneCraftingTransaction.inspect(context, player, input);
    }

    private static final class PatternSlot extends FakeSlot {
        private final ArcaneInscriberMenu menu;

        PatternSlot(ArcaneInscriberMenu menu, AppEngInternalInventory inv, int slot) {
            super(inv, slot);
            this.menu = menu;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            menu.refresh();
        }
    }

    private static final class CoreSlot extends AppEngSlot {
        private final ArcaneInscriberMenu menu;

        CoreSlot(ArcaneInscriberMenu menu, AppEngInternalInventory inv) {
            super(inv, 0);
            this.menu = menu;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return KnowledgeCores.isCore(stack) && super.mayPlace(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            menu.refresh();
        }
    }

    private static final class PreviewSlot extends AppEngSlot {
        PreviewSlot(AppEngInternalInventory inv) {
            super(inv, 0);
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
}
