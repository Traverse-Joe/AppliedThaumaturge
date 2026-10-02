package com.traverse.appliedthaumaturge.client.jei;

import com.leclowndu93150.thaumaturge.compat.jei.category.ArcaneWorkbenchCategory;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingRecipe;
import com.traverse.appliedthaumaturge.menu.ArcaneRecipeTarget;
import com.traverse.appliedthaumaturge.network.ArcaneTransferPacket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferContext;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.RecipeTransferResult;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class ArcaneTransferHandler<M extends AbstractContainerMenu & ArcaneRecipeTarget>
        implements IRecipeTransferHandler<M, RecipeHolder<ArcaneCraftingRecipe>> {
    private static final int SLOTS = 15;
    private static final int MAX_CANDIDATES = 32;

    private final IRecipeTransferHandlerHelper helper;
    private final Class<M> menuClass;
    private final MenuType<M> menuType;

    public ArcaneTransferHandler(IRecipeTransferHandlerHelper helper, Class<M> menuClass, MenuType<M> menuType) {
        this.helper = helper;
        this.menuClass = menuClass;
        this.menuType = menuType;
    }

    @Override
    public Class<? extends M> getContainerClass() {
        return menuClass;
    }

    @Override
    public Optional<MenuType<M>> getMenuType() {
        return Optional.of(menuType);
    }

    @Override
    public IRecipeType<RecipeHolder<ArcaneCraftingRecipe>> getRecipeType() {
        return ArcaneWorkbenchCategory.RECIPE_TYPE;
    }

    @Override
    public IRecipeTransferError transferRecipe(IRecipeTransferContext<RecipeHolder<ArcaneCraftingRecipe>, M> context, boolean doTransfer) {
        List<IRecipeSlotView> inputs = context.getRecipeSlots().getSlotViews(RecipeIngredientRole.INPUT);
        if (inputs.size() < SLOTS) {
            return helper.createInternalError();
        }
        if (!doTransfer) {
            return null;
        }
        List<List<ItemStack>> slots = new ArrayList<>(SLOTS);
        for (int i = 0; i < SLOTS; i++) {
            slots.add(inputs.get(i).getItemStacks().limit(MAX_CANDIDATES).map(ItemStack::copy).toList());
        }
        ClientPacketDistributor.sendToServer(new ArcaneTransferPacket(context.getContainer().containerId, slots));
        context.completeRecipeTransfer(RecipeTransferResult.SUCCESS);
        return null;
    }

    @SuppressWarnings("removal")
    @Override
    public IRecipeTransferError transferRecipe(M menu, RecipeHolder<ArcaneCraftingRecipe> recipe, IRecipeSlotsView view,
                                               Player player, boolean maxTransfer, boolean doTransfer) {
        return helper.createInternalError();
    }
}
