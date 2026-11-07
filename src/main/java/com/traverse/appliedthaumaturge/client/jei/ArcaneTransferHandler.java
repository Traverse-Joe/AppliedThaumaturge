package com.traverse.appliedthaumaturge.client.jei;

import com.traverse.appliedthaumaturge.menu.ArcaneRecipeTarget;
import com.traverse.appliedthaumaturge.network.ArcaneTransferPacket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class ArcaneTransferHandler<M extends AbstractContainerMenu & ArcaneRecipeTarget, R>
        implements IRecipeTransferHandler<M, R> {
    private static final int MAX_CANDIDATES = 32;

    private final IRecipeTransferHandlerHelper helper;
    private final Class<M> menuClass;
    @Nullable
    private final MenuType<M> menuType;
    private final RecipeType<R> recipeType;
    private final int slotCount;

    public ArcaneTransferHandler(IRecipeTransferHandlerHelper helper, Class<M> menuClass, @Nullable MenuType<M> menuType,
                                RecipeType<R> recipeType, int slotCount) {
        this.helper = helper;
        this.menuClass = menuClass;
        this.menuType = menuType;
        this.recipeType = recipeType;
        this.slotCount = slotCount;
    }

    @Override
    public Class<? extends M> getContainerClass() {
        return menuClass;
    }

    @Override
    public Optional<MenuType<M>> getMenuType() {
        return Optional.ofNullable(menuType);
    }

    @Override
    public RecipeType<R> getRecipeType() {
        return recipeType;
    }

    @Override
    public IRecipeTransferError transferRecipe(M menu, R recipe, IRecipeSlotsView view,
                                               Player player, boolean maxTransfer, boolean doTransfer) {
        List<IRecipeSlotView> inputs = view.getSlotViews(RecipeIngredientRole.INPUT);
        if (inputs.size() < slotCount) {
            return helper.createInternalError();
        }
        if (!doTransfer) {
            return null;
        }
        List<List<ItemStack>> slots = new ArrayList<>(slotCount);
        for (int i = 0; i < slotCount; i++) {
            slots.add(inputs.get(i).getItemStacks().limit(MAX_CANDIDATES).map(ItemStack::copy).toList());
        }
        PacketDistributor.sendToServer(new ArcaneTransferPacket(menu.containerId, slots));
        return null;
    }
}
