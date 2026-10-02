package com.traverse.appliedthaumics.assembler;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEItemKey;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import appeng.menu.me.crafting.CraftingPlanSummary;
import appeng.menu.me.crafting.CraftingPlanSummaryEntry;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.content.workbench.SlotWorkbenchWand;
import com.traverse.appliedthaumics.me.key.VisKey;
import com.traverse.appliedthaumics.registry.ATDataComponents;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public final class ArcanePlanCheck {
    private ArcanePlanCheck() {
    }

    public static CraftingPlanSummary augment(IGrid grid, ICraftingPlan plan, CraftingPlanSummary summary) {
        List<CraftingPlanSummaryEntry> extra = entries(grid, plan);
        if (extra.isEmpty()) {
            return summary;
        }
        Map<AEKey, CraftingPlanSummaryEntry> merged = new LinkedHashMap<>();
        for (var entry : summary.getEntries()) {
            merged.put(entry.getWhat(), entry);
        }
        for (var entry : extra) {
            merged.merge(entry.getWhat(), entry, (first, second) -> new CraftingPlanSummaryEntry(first.getWhat(),
                    first.getMissingAmount() + second.getMissingAmount(), first.getStoredAmount() + second.getStoredAmount(),
                    first.getCraftAmount() + second.getCraftAmount()));
        }
        List<CraftingPlanSummaryEntry> entries = new ArrayList<>(merged.values());
        entries.sort(null);
        return new CraftingPlanSummary(summary.getUsedBytes(), summary.isSimulation()
                || extra.stream().anyMatch(entry -> entry.getMissingAmount() > 0), entries);
    }

    public static List<CraftingPlanSummaryEntry> entries(IGrid grid, ICraftingPlan plan) {
        if (grid == null || plan == null) {
            return List.of();
        }
        Map<AuraPool, Long> aura = new HashMap<>();
        Map<WandPool, Long> wand = new HashMap<>();
        Map<BufferPool, Long> buffers = new HashMap<>();
        Map<AEItemKey, Long> crystals = new HashMap<>();
        var storedItems = grid.getStorageService().getInventory().getAvailableStacks();
        Map<AEKey, long[]> totals = new LinkedHashMap<>();
        for (var entry : plan.patternTimes().entrySet()) {
            if (!(entry.getKey() instanceof ArcanePattern pattern) || entry.getValue() <= 0) {
                continue;
            }
            long remaining = entry.getValue();
            var candidates = grid.getActiveMachines(ArcaneAssemblerBlockEntity.class).stream()
                    .filter(assembler -> assembler.getAvailablePatterns().contains(pattern)).toList();
            if (candidates.isEmpty()) {
                var output = pattern.getPrimaryOutput();
                record(totals, output.what(), remaining * output.amount(), 0);
                continue;
            }
            for (int index = 0; index < candidates.size() && remaining > 0; index++) {
                var assembler = candidates.get(index);
                var stack = assembler.getInventory().getStackInSlot(ArcaneAssemblerBlockEntity.WAND_SLOT);
                Long pair = stack.get(ATDataComponents.ENTANGLEMENT.get());
                Object reservoir = pair == null ? assembler : pair;
                boolean hasWand = SlotWorkbenchWand.isUsableWand(stack);
                var costs = ArcaneAssemblerBlockEntity.getWandCost(pattern);
                List<AuraPool> pools = new ArrayList<>();
                for (var anchor : assembler.getAuraAnchors()) {
                    var pool = new AuraPool(assembler.getLevel(), ChunkPos.containing(anchor));
                    aura.computeIfAbsent(pool, ignored -> (long) Math.floor(AuraHelper.getVis(assembler.getLevel(), anchor) * 100.0));
                    pools.add(pool);
                }
                Map<ResourceKey<IAspect>, AEItemKey> crystalKeys = new HashMap<>();
                for (var requirement : pattern.recipe().getCrystals().entries()) {
                    AEItemKey key = AEItemKey.of(EssentiaCrystalFactory.of(requirement.aspect(), 1));
                    crystalKeys.put(requirement.aspect().getKey(), key);
                    crystals.computeIfAbsent(key, ignored -> Math.max(0, storedItems.get(key) - plan.usedItems().get(key)));
                }
                long auraCost = assembler.getAuraCost(pattern) * 100L;
                long crystalAuraCost = assembler.getAuraCost(pattern, !hasWand) * 100L;
                long availableAura = pools.stream().mapToLong(aura::get).sum();
                long capacity = remaining;
                long fullWandCrafts = remaining;
                int perCrystal = WandEconomy.CRYSTAL_SUBSTITUTE_VIS * WandEconomy.CENTIVIS_PER_VIS;
                for (var cost : costs.entrySet()) {
                    var bufferPool = new BufferPool(assembler, cost.getKey());
                    long buffered = buffers.computeIfAbsent(bufferPool, ignored -> (long) assembler.getStoredVis().amount(cost.getKey()));
                    var pool = new WandPool(reservoir, cost.getKey());
                    long available = wand.computeIfAbsent(pool, ignored -> hasWand ? (long) WandVisHelper.getVis(stack, cost.getKey()) : 0L);
                    long visCrafts = (buffered + available) / cost.getValue();
                    fullWandCrafts = Math.min(fullWandCrafts, visCrafts);
                    long crystalVis = hasWand ? 0 : crystals.get(crystalKeys.get(cost.getKey())) * perCrystal;
                    capacity = Math.min(capacity, (buffered + available + crystalVis) / cost.getValue());
                }
                long affordableWandCrafts = auraCost == 0 ? fullWandCrafts : Math.min(fullWandCrafts, availableAura / auraCost);
                long remainingAura = availableAura - affordableWandCrafts * auraCost;
                long affordableCrystalCrafts = crystalAuraCost == 0 ? remaining : remainingAura / crystalAuraCost;
                capacity = Math.min(capacity, affordableWandCrafts + affordableCrystalCrafts);
                long crafts = index == candidates.size() - 1 ? remaining : capacity;
                fullWandCrafts = Math.min(fullWandCrafts, crafts);
                for (var cost : costs.entrySet()) {
                    long required = crafts * cost.getValue();
                    var bufferPool = new BufferPool(assembler, cost.getKey());
                    long buffered = Math.min(required, buffers.get(bufferPool));
                    buffers.put(bufferPool, buffers.get(bufferPool) - buffered);
                    long unmet = required - buffered;
                    var pool = new WandPool(reservoir, cost.getKey());
                    long wandUsed = hasWand ? Math.min(unmet, wand.get(pool)) : 0;
                    wand.put(pool, wand.get(pool) - wandUsed);
                    if (hasWand) {
                        record(totals, VisKey.of(cost.getKey()), required, buffered + wandUsed);
                    } else {
                        record(totals, VisKey.of(cost.getKey()), buffered, buffered);
                        AEItemKey key = crystalKeys.get(cost.getKey());
                        long crystalNeed = Math.ceilDiv(unmet, perCrystal);
                        long crystalUsed = Math.min(crystalNeed, crystals.get(key));
                        crystals.put(key, crystals.get(key) - crystalUsed);
                        record(totals, key, crystalNeed, crystalUsed);
                    }
                }
                long needed = fullWandCrafts * auraCost + (crafts - fullWandCrafts) * crystalAuraCost;
                long spend = Math.min(needed, availableAura);
                record(totals, VisKey.AURA, needed, spend);
                for (var pool : pools) {
                    long take = Math.min(spend, aura.get(pool));
                    aura.put(pool, aura.get(pool) - take);
                    spend -= take;
                }
                remaining -= crafts;
            }
        }
        return totals.entrySet().stream().map(entry -> new CraftingPlanSummaryEntry(
                entry.getKey(), entry.getValue()[0], entry.getValue()[1], 0)).toList();
    }

    private static void record(Map<AEKey, long[]> totals, AEKey key, long needed, long used) {
        if (needed > 0) {
            long[] amounts = totals.computeIfAbsent(key, ignored -> new long[2]);
            amounts[0] += needed - used;
            amounts[1] += used;
        }
    }

    private record AuraPool(Level level, ChunkPos chunk) {
    }

    private record BufferPool(ArcaneAssemblerBlockEntity assembler, ResourceKey<IAspect> aspect) {
    }

    private record WandPool(Object reservoir, ResourceKey<IAspect> aspect) {
    }
}
