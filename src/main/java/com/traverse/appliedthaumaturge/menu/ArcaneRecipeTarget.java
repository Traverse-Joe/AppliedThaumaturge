package com.traverse.appliedthaumaturge.menu;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public interface ArcaneRecipeTarget {
    void fillFromRecipe(List<List<ItemStack>> slots);
}
