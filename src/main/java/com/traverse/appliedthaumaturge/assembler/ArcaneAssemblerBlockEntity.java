package com.traverse.appliedthaumaturge.assembler;

import appeng.api.config.Actionable;
import appeng.api.ids.AEComponents;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.util.DimensionalBlockPos;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import appeng.api.crafting.IPatternDetails;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.StorageHelper;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.core.definitions.AEItems;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.wands.WandVis;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.content.workbench.SlotWorkbenchWand;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingRecipe;
import com.leclowndu93150.thaumaturge.content.taint.item.ItemEssentiaCrystal;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.content.workbench.MenuArcaneWorkbench;
import com.traverse.appliedthaumaturge.block.entity.EssentiaNetworkBlockEntity;
import com.traverse.appliedthaumaturge.arcane.ArcaneTerminalAuraSource;
import com.traverse.appliedthaumaturge.knowledge.KnowledgeCores;
import com.traverse.appliedthaumaturge.knowledge.KnowledgeRecipe;
import com.traverse.appliedthaumaturge.registry.ATBlockEntities;
import com.traverse.appliedthaumaturge.registry.ATBlocks;
import com.traverse.appliedthaumaturge.registry.ATItems;
import com.traverse.appliedthaumaturge.vis.VisRelayLink;
import com.traverse.appliedthaumaturge.vis.VisRelayCharging;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

public class ArcaneAssemblerBlockEntity extends EssentiaNetworkBlockEntity
        implements ICraftingProvider, IGridTickable, IUpgradeableObject, InternalInventoryHost {
    public static final int GRID_START = 0;
    public static final int OUTPUT_SLOT = 9;
    public static final int CORE_SLOT = 10;
    public static final int WAND_SLOT = 11;
    public static final int MAX_STORED_CENTIVIS = 150 * WandEconomy.CENTIVIS_PER_VIS;
    private static final int[] SPEED = {5, 10, 20, 34, 50, 100};

    private final AppEngInternalInventory inventory = new AppEngInternalInventory(this, 12, 64);
    private final IUpgradeInventory upgrades;
    private final List<ItemStack> crystals = new ArrayList<>();
    private final IActionSource source = IActionSource.ofMachine(this);
    @Nullable
    private ArcanePattern job;
    @Nullable
    private List<IPatternDetails> patterns;
    private int progress;
    @Nullable
    private VisRelayLink visSource;
    private int visChargeTicks;
    private WandVis storedVis = WandVis.EMPTY;

    public ArcaneAssemblerBlockEntity(BlockPos pos, BlockState state) {
        super(ATBlockEntities.ARCANE_ASSEMBLER.get(), pos, state);
        this.upgrades = UpgradeInventories.forMachine(ATBlocks.ARCANE_ASSEMBLER_ITEM.get(), 6, this::saveChanges);
        getMainNode().addService(ICraftingProvider.class, this).addService(IGridTickable.class, this);
    }

    public InternalInventory getInventory() {
        return inventory;
    }

    public boolean useVisMemoryCard(ItemStack stack, Player player) {
        if (!(stack.getItem() instanceof IMemoryCard card) || InteractionUtil.isInAlternateUseMode(player)) {
            return false;
        }
        VisRelayLink saved = VisRelayLink.fromCard(stack);
        boolean clear = visSource != null && !stack.has(AEComponents.EXPORTED_SETTINGS_SOURCE);
        if (saved == null && !clear) {
            return false;
        }
        if (isClientSide()) {
            return true;
        }
        if (level == null || !Platform.hasPermissions(new DimensionalBlockPos(this), player)) {
            return false;
        }
        if (saved != null && saved.resolve(level, getMainNode()) == null) {
            card.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);
        } else {
            visSource = saved;
            visChargeTicks = 0;
            patterns = null;
            ICraftingProvider.requestUpdate(getMainNode());
            saveChanges();
            wake();
            card.notifyUser(player, clear ? MemoryCardMessages.SETTINGS_CLEARED : MemoryCardMessages.SETTINGS_LOADED);
        }
        return true;
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    public int getAvailableVis() {
        if (level == null) {
            return 0;
        }
        float total = 0;
        for (BlockPos anchor : getAuraAnchors()) {
            total += AuraHelper.getVis(level, anchor);
        }
        return (int) total;
    }

    public int getProgress() {
        return progress;
    }

    public WandVis getStoredVis() {
        return storedVis;
    }

    public boolean isVisLinked() {
        return visSource != null;
    }

    private void replenishVis() {
        var relay = visSource == null ? null : visSource.resolve(level, getMainNode());
        if (relay == null) {
            return;
        }
        boolean changed = false;
        for (var aspect : MenuArcaneWorkbench.PRIMAL_ORDER) {
            int room = MAX_STORED_CENTIVIS - storedVis.amount(aspect);
            if (room <= 0) {
                continue;
            }
            int drained;
            try (Transaction tx = Transaction.openRoot()) {
                drained = relay.drainCentivis(aspect, room, tx);
                if (drained > 0) {
                    tx.commit();
                }
            }
            if (drained > 0) {
                storedVis = storedVis.with(aspect, storedVis.amount(aspect) + drained);
                changed = true;
            }
        }
        if (changed) {
            saveChanges();
        }
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        saveChanges();
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        if (slot == CORE_SLOT || slot == WAND_SLOT) {
            patterns = null;
            ICraftingProvider.requestUpdate(getMainNode());
        }
        wake();
    }

    @Override
    public boolean isClientSide() {
        return level != null && level.isClientSide();
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        if (patterns == null) {
            patterns = buildPatterns();
        }
        return patterns;
    }

    private List<IPatternDetails> buildPatterns() {
        ItemStack core = inventory.getStackInSlot(CORE_SLOT);
        if (!(level instanceof ServerLevel serverLevel) || !KnowledgeCores.isCore(core)) {
            return List.of();
        }
        List<IPatternDetails> out = new ArrayList<>();
        for (KnowledgeRecipe knowledge : KnowledgeCores.contents(core).recipes()) {
            ArcaneCraftingRecipe recipe = resolve(serverLevel, knowledge);
            if (recipe != null) {
                ArcanePattern pattern = ArcanePattern.create(knowledge, recipe, visSource != null
                        || !storedVis.isEmpty() || SlotWorkbenchWand.isUsableWand(inventory.getStackInSlot(WAND_SLOT)));
                if (pattern != null) {
                    out.add(pattern);
                }
            }
        }
        return out;
    }

    @Nullable
    private static ArcaneCraftingRecipe resolve(ServerLevel level, KnowledgeRecipe knowledge) {
        RecipeHolder<?> holder = level.getServer().getRecipeManager().byKey(knowledge.recipe()).orElse(null);
        Recipe<?> recipe = holder == null ? null : holder.value();
        return recipe instanceof ArcaneCraftingRecipe arcane ? arcane : null;
    }

    @Override
    public boolean isBusy() {
        return job != null || !inventory.getStackInSlot(OUTPUT_SLOT).isEmpty() || hasLeftovers();
    }

    private boolean hasLeftovers() {
        if (job != null) {
            return false;
        }
        if (!crystals.isEmpty()) {
            return true;
        }
        for (int i = 0; i < 9; i++) {
            if (!inventory.getStackInSlot(GRID_START + i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void salvage() {
        IGrid grid = getMainNode().getGrid();
        if (grid == null || !getMainNode().isActive()) {
            return;
        }
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getStackInSlot(GRID_START + i);
            if (!stack.isEmpty()) {
                inventory.setItemDirect(GRID_START + i, returnToNetwork(grid, stack));
            }
        }
        List<ItemStack> kept = new ArrayList<>();
        for (ItemStack crystal : crystals) {
            ItemStack left = returnToNetwork(grid, crystal);
            if (!left.isEmpty()) {
                kept.add(left);
            }
        }
        crystals.clear();
        crystals.addAll(kept);
        saveChanges();
    }

    private ItemStack returnToNetwork(IGrid grid, ItemStack stack) {
        long inserted = StorageHelper.poweredInsert(grid.getEnergyService(), grid.getStorageService().getInventory(), AEItemKey.of(stack), stack.getCount(), source);
        ItemStack left = stack.copy();
        left.shrink((int) inserted);
        return left;
    }

    @Override
    public boolean pushPattern(IPatternDetails details, KeyCounter[] inputs) {
        if (isBusy() || !(details instanceof ArcanePattern pattern) || !getAvailablePatterns().contains(pattern)) {
            return false;
        }
        crystals.clear();
        for (int i = 0; i < inputs.length; i++) {
            int gridSlot = pattern.gridSlotOf(i);
            for (var entry : inputs[i]) {
                if (!(entry.getKey() instanceof AEItemKey key)) {
                    continue;
                }
                ItemStack stack = key.toStack((int) entry.getLongValue());
                if (gridSlot >= 0) {
                    inventory.setItemDirect(GRID_START + gridSlot, stack);
                } else {
                    crystals.add(stack);
                }
            }
        }
        job = pattern;
        progress = 0;
        saveChanges();
        wake();
        return true;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 20, !isBusy() && visSource == null);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (visSource != null) {
            visChargeTicks += ticksSinceLastCall;
            if (visChargeTicks >= 5) {
                visChargeTicks %= 5;
                replenishVis();
                ItemStack wand = inventory.getStackInSlot(WAND_SLOT);
                if (job == null && VisRelayCharging.charge(wand, visSource.resolve(level, getMainNode()))) {
                    inventory.setItemDirect(WAND_SLOT, wand);
                    saveChanges();
                }
            }
        }
        if (!inventory.getStackInSlot(OUTPUT_SLOT).isEmpty()) {
            flushOutput();
            return isBusy() ? TickRateModulation.SLOWER : idleTickRate(ticksSinceLastCall);
        }
        if (hasLeftovers()) {
            salvage();
            return hasLeftovers() ? TickRateModulation.SLOWER : idleTickRate(ticksSinceLastCall);
        }
        if (job == null) {
            return idleTickRate(ticksSinceLastCall);
        }
        if (progress < 100) {
            progress = Math.min(100, progress + SPEED[Math.min(5, upgrades.getInstalledUpgrades(AEItems.SPEED_CARD))] * ticksSinceLastCall);
            return TickRateModulation.URGENT;
        }
        if (!finishCraft()) {
            return TickRateModulation.SLOWER;
        }
        flushOutput();
        return TickRateModulation.URGENT;
    }

    private TickRateModulation idleTickRate(int ticksSinceLastCall) {
        if (visSource == null) {
            return TickRateModulation.SLEEP;
        }
        return ticksSinceLastCall > 5 ? TickRateModulation.FASTER
                : ticksSinceLastCall < 5 ? TickRateModulation.SLOWER : TickRateModulation.SAME;
    }

    private boolean finishCraft() {
        if (!(level instanceof ServerLevel serverLevel) || job == null) {
            return false;
        }
        ArcaneCraftingRecipe recipe = job.recipe();
        ItemStack wand = inventory.getStackInSlot(WAND_SLOT).copy();
        ArcaneVisPayment payment = ArcaneVisPayment.plan(ArcaneVisPayment.costs(recipe), storedVis, wand);
        if (payment == null) {
            return false;
        }
        for (var requirement : payment.crystals().entrySet()) {
            int stored = crystals.stream().filter(crystal -> {
                Holder<IAspect> type = ItemEssentiaCrystal.aspectOf(crystal);
                return type != null && type.is(requirement.getKey());
            }).mapToInt(ItemStack::getCount).sum();
            int missing = requirement.getValue() - stored;
            if (missing > 0) {
                IGrid grid = getMainNode().getGrid();
                if (grid == null) {
                    return false;
                }
                var aspect = serverLevel.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(requirement.getKey());
                AEItemKey key = AEItemKey.of(EssentiaCrystalFactory.of(aspect, 1));
                long taken = StorageHelper.poweredExtraction(grid.getEnergyService(), grid.getStorageService().getInventory(),
                        key, missing, source, Actionable.MODULATE);
                if (taken > 0) {
                    crystals.add(key.toStack((int) taken));
                    saveChanges();
                }
                if (taken < missing) {
                    return false;
                }
            }
        }
        List<ItemStack> items = new ArrayList<>(16);
        for (int i = 0; i < 9; i++) {
            items.add(inventory.getStackInSlot(GRID_START + i).copy());
        }
        for (var primal : MenuArcaneWorkbench.PRIMAL_ORDER) {
            ItemStack match = ItemStack.EMPTY;
            for (ItemStack crystal : crystals) {
                Holder<IAspect> aspect = ItemEssentiaCrystal.aspectOf(crystal);
                if (aspect != null && aspect.is(primal)) {
                    match = crystal.copyWithCount(payment.crystals().getOrDefault(primal, 0));
                }
            }
            items.add(match);
        }
        items.add(wand);
        ArcaneCraftingInput input = ArcaneCraftingInput.of(3, 3, items);
        int vis = getAuraCost(job, !payment.crystals().isEmpty());
        ItemStack output = recipe.assemble(input);
        List<ItemStack> remainders = recipe.getRemainingItems(input);
        if (output.isEmpty()) {
            return false;
        }
        try (Transaction tx = Transaction.openRoot()) {
            if (vis > 0 && !drainAura(serverLevel, vis, tx)) {
                return false;
            }
            if (!payment.wand().isEmpty() && !WandVisHelper.consumeAllVisRaw(wand, payment.wand(), true)) {
                return false;
            }
            tx.commit();
        }
        if (!payment.wand().isEmpty()) {
            WandVisHelper.consumeAllVisRaw(wand, payment.wand(), false);
            inventory.setItemDirect(WAND_SLOT, wand);
        }
        for (int i = 0; i < 9; i++) {
            inventory.setItemDirect(GRID_START + i, ItemStack.EMPTY);
        }
        for (var cost : payment.buffer().entrySet()) {
            storedVis = storedVis.with(cost.getKey(), storedVis.amount(cost.getKey()) - cost.getValue());
        }
        for (var cost : payment.crystals().entrySet()) {
            int remaining = cost.getValue();
            for (ItemStack crystal : crystals) {
                Holder<IAspect> aspect = ItemEssentiaCrystal.aspectOf(crystal);
                if (remaining > 0 && aspect != null && aspect.is(cost.getKey())) {
                    int used = Math.min(remaining, crystal.getCount());
                    crystal.shrink(used);
                    remaining -= used;
                }
            }
        }
        crystals.removeIf(ItemStack::isEmpty);
        job = null;
        progress = 0;
        inventory.setItemDirect(OUTPUT_SLOT, output);
        IGrid grid = getMainNode().getGrid();
        for (ItemStack remainder : remainders) {
            if (!remainder.isEmpty() && grid != null) {
                StorageHelper.poweredInsert(grid.getEnergyService(), grid.getStorageService().getInventory(), AEItemKey.of(remainder), remainder.getCount(), source);
            }
        }
        saveChanges();
        return true;
    }

    public static Map<ResourceKey<IAspect>, Integer> getWandCost(ArcanePattern pattern) {
        Map<ResourceKey<IAspect>, Integer> cost = new LinkedHashMap<>();
        if (pattern.usesWand()) {
            for (var crystal : pattern.recipe().getCrystals().entries()) {
                if (crystal.amount() > 0) {
                    cost.merge(crystal.aspect().getKey(), crystal.amount() * WandEconomy.CRYSTAL_SUBSTITUTE_VIS
                            * WandEconomy.CENTIVIS_PER_VIS, Integer::sum);
                }
            }
        }
        return cost;
    }

    public int getAuraCost(ArcanePattern pattern) {
        return getAuraCost(pattern, pattern.crystalCount() > 0);
    }

    public int getAuraCost(ArcanePattern pattern, boolean paysCrystals) {
        float modifier = paysCrystals ? WandEconomy.CRAFT_AURA_SURCHARGE : 1.0F;
        if (!paysCrystals && pattern.usesWand() && SlotWorkbenchWand.isUsableWand(inventory.getStackInSlot(WAND_SLOT))) {
            ItemStack wand = inventory.getStackInSlot(WAND_SLOT);
            modifier = 0;
            for (var primal : MenuArcaneWorkbench.PRIMAL_ORDER) {
                modifier += WandVisHelper.getConsumptionModifier(wand, null, primal, true);
            }
            modifier /= MenuArcaneWorkbench.PRIMAL_ORDER.size();
        }
        int base = pattern.recipe().getBaseVis();
        return base <= 0 ? 0 : Math.max(1, (int) Math.ceil(base * modifier));
    }

    public List<BlockPos> getAuraAnchors() {
        return ArcaneTerminalAuraSource.anchors(worldPosition, upgrades.isInstalled(ATItems.UPGRADE_ARCANE));
    }

    private boolean drainAura(ServerLevel level, int vis, Transaction tx) {
        List<BlockPos> anchors = getAuraAnchors();
        float remaining = vis;
        int share = Math.max(1, vis / anchors.size());
        while (remaining > 0) {
            float pass = 0;
            for (BlockPos anchor : anchors) {
                float drained = AuraHelper.drainVis(level, anchor, Math.min(share, remaining), tx);
                pass += drained;
                remaining -= drained;
                if (remaining <= 0) {
                    break;
                }
            }
            if (pass <= 0) {
                return false;
            }
        }
        return true;
    }

    private void flushOutput() {
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        IGrid grid = getMainNode().getGrid();
        if (output.isEmpty() || grid == null || !getMainNode().isActive()) {
            return;
        }
        AEKey key = AEItemKey.of(output);
        long inserted = StorageHelper.poweredInsert(grid.getEnergyService(), grid.getStorageService().getInventory(), key, output.getCount(), source, Actionable.MODULATE);
        if (inserted > 0) {
            ItemStack left = output.copy();
            left.shrink((int) inserted);
            inventory.setItemDirect(OUTPUT_SLOT, left);
            saveChanges();
        }
    }

    private void wake() {
        getMainNode().ifPresent((grid, node) -> grid.getTickManager().wakeDevice(node));
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) {
                drops.add(stack);
            }
        }
        for (ItemStack stack : upgrades) {
            if (!stack.isEmpty()) {
                drops.add(stack);
            }
        }
        drops.addAll(crystals);
    }

    @Override
    public void loadTag(ValueInput data) {
        super.loadTag(data);
        inventory.readFromNBT(data, "inventory");
        upgrades.readFromNBT(data, "upgrades");
        progress = data.getIntOr("progress", 0);
        storedVis = data.read("storedVis", WandVis.CODEC).orElse(WandVis.EMPTY);
        for (var aspect : MenuArcaneWorkbench.PRIMAL_ORDER) {
            storedVis = storedVis.with(aspect, Math.clamp(storedVis.amount(aspect), 0, MAX_STORED_CENTIVIS));
        }
        visSource = data.read("visSource", VisRelayLink.CODEC).orElse(null);
        visChargeTicks = 0;
        crystals.clear();
        data.read("crystals", ItemStack.OPTIONAL_CODEC.listOf()).ifPresent(list -> list.stream().filter(s -> !s.isEmpty()).forEach(crystals::add));
        patterns = null;
        job = null;
    }

    @Override
    public void saveAdditional(ValueOutput data) {
        super.saveAdditional(data);
        inventory.writeToNBT(data, "inventory");
        upgrades.writeToNBT(data, "upgrades");
        data.putInt("progress", progress);
        data.store("storedVis", WandVis.CODEC, storedVis);
        if (visSource != null) {
            data.store("visSource", VisRelayLink.CODEC, visSource);
        }
        data.store("crystals", ItemStack.OPTIONAL_CODEC.listOf(), List.copyOf(crystals));
    }
}
