package com.traverse.appliedthaumics.client.jei;

import com.leclowndu93150.thaumaturge.compat.jei.category.ArcaneWorkbenchCategory;
import com.traverse.appliedthaumics.AppliedThaumics;
import com.traverse.appliedthaumics.menu.ArcaneInscriberMenu;
import com.traverse.appliedthaumics.menu.ArcaneTermMenu;
import com.traverse.appliedthaumics.registry.ATItems;
import com.traverse.appliedthaumics.registry.ATMenus;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.Identifier;

@JeiPlugin
public class ATJeiPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return AppliedThaumics.id("jei");
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
                new ArcaneTransferHandler<>(registration.getTransferHelper(), ArcaneTermMenu.class, ATMenus.ARCANE_TERMINAL), ArcaneWorkbenchCategory.RECIPE_TYPE);
        registration.addRecipeTransferHandler(
                new ArcaneTransferHandler<>(registration.getTransferHelper(), ArcaneInscriberMenu.class, ATMenus.ARCANE_INSCRIBER), ArcaneWorkbenchCategory.RECIPE_TYPE);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(ArcaneWorkbenchCategory.RECIPE_TYPE, ATItems.ARCANE_TERMINAL.get(), ATItems.ARCANE_INSCRIBER.get());
    }
}
