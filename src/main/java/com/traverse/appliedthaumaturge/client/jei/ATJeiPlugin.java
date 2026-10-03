package com.traverse.appliedthaumaturge.client.jei;

import com.leclowndu93150.thaumaturge.compat.jei.category.ArcaneWorkbenchCategory;
import com.traverse.appliedthaumaturge.AppliedThaumaturge;
import com.traverse.appliedthaumaturge.menu.ArcaneInscriberMenu;
import com.traverse.appliedthaumaturge.menu.ArcaneTermMenu;
import com.traverse.appliedthaumaturge.registry.ATItems;
import com.traverse.appliedthaumaturge.registry.ATMenus;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.Identifier;

@JeiPlugin
public class ATJeiPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return AppliedThaumaturge.id("jei");
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
                new ArcaneTransferHandler<>(registration.getTransferHelper(), ArcaneTermMenu.class, ATMenus.ARCANE_TERMINAL, ArcaneWorkbenchCategory.RECIPE_TYPE, 15), ArcaneWorkbenchCategory.RECIPE_TYPE);
        registration.addRecipeTransferHandler(
                new ArcaneTransferHandler<>(registration.getTransferHelper(), ArcaneTermMenu.class, ATMenus.WIRELESS_ARCANE_TERMINAL, ArcaneWorkbenchCategory.RECIPE_TYPE, 15), ArcaneWorkbenchCategory.RECIPE_TYPE);
        registration.addRecipeTransferHandler(
                new ArcaneTransferHandler<>(registration.getTransferHelper(), ArcaneInscriberMenu.class, ATMenus.ARCANE_INSCRIBER, ArcaneWorkbenchCategory.RECIPE_TYPE, 15), ArcaneWorkbenchCategory.RECIPE_TYPE);
        registration.addRecipeTransferHandler(
                new ArcaneTransferHandler<>(registration.getTransferHelper(), ArcaneTermMenu.class, ATMenus.ARCANE_TERMINAL, RecipeTypes.CRAFTING, 9), RecipeTypes.CRAFTING);
        registration.addRecipeTransferHandler(
                new ArcaneTransferHandler<>(registration.getTransferHelper(), ArcaneTermMenu.class, ATMenus.WIRELESS_ARCANE_TERMINAL, RecipeTypes.CRAFTING, 9), RecipeTypes.CRAFTING);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(ArcaneWorkbenchCategory.RECIPE_TYPE, ATItems.ARCANE_TERMINAL.get(), ATItems.ARCANE_INSCRIBER.get(), ATItems.WIRELESS_ARCANE_TERMINAL.get());
        registration.addCraftingStation(RecipeTypes.CRAFTING, ATItems.ARCANE_TERMINAL.get(), ATItems.WIRELESS_ARCANE_TERMINAL.get());
    }
}
