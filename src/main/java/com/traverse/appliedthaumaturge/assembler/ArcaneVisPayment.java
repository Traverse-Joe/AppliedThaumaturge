package com.traverse.appliedthaumaturge.assembler;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.wands.WandVis;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingRecipe;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.content.workbench.SlotWorkbenchWand;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record ArcaneVisPayment(Map<ResourceKey<IAspect>, Integer> buffer,
                               Map<ResourceKey<IAspect>, Integer> wand,
                               Map<ResourceKey<IAspect>, Integer> crystals) {
    public static final int CENTIVIS_PER_CRYSTAL = WandEconomy.CRYSTAL_SUBSTITUTE_VIS * WandEconomy.CENTIVIS_PER_VIS;

    public static Map<ResourceKey<IAspect>, Integer> costs(ArcaneCraftingRecipe recipe) {
        Map<ResourceKey<IAspect>, Integer> costs = new LinkedHashMap<>();
        for (var requirement : recipe.crystalCost().entries()) {
            if (requirement.amount() > 0) {
                costs.merge(requirement.aspect().getKey(), Math.multiplyExact(requirement.amount(), CENTIVIS_PER_CRYSTAL), Integer::sum);
            }
        }
        return costs;
    }

    @Nullable
    public static ArcaneVisPayment plan(Map<ResourceKey<IAspect>, Integer> costs, WandVis stored, ItemStack wandStack) {
        Map<ResourceKey<IAspect>, Integer> buffer = new LinkedHashMap<>();
        Map<ResourceKey<IAspect>, Integer> wand = new LinkedHashMap<>();
        Map<ResourceKey<IAspect>, Integer> crystals = new LinkedHashMap<>();
        boolean hasWand = SlotWorkbenchWand.isUsableWand(wandStack);
        for (var cost : costs.entrySet()) {
            int buffered = Math.min(cost.getValue(), stored.amount(cost.getKey()));
            if (buffered > 0) {
                buffer.put(cost.getKey(), buffered);
            }
            int remaining = cost.getValue() - buffered;
            if (remaining <= 0) {
                continue;
            }
            if (hasWand) {
                if (WandVisHelper.getVis(wandStack, cost.getKey()) < remaining) {
                    return null;
                }
                wand.put(cost.getKey(), remaining);
            } else {
                crystals.put(cost.getKey(), Math.ceilDiv(remaining, CENTIVIS_PER_CRYSTAL));
            }
        }
        return new ArcaneVisPayment(Map.copyOf(buffer), Map.copyOf(wand), Map.copyOf(crystals));
    }
}
