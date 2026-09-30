package com.traverse.appliedthaumics.menu;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public interface ArcaneRecipeTarget {
    void fillFromRecipe(List<List<ItemStack>> slots);
}
