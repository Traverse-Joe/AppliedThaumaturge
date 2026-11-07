package com.traverse.appliedthaumaturge.me.strategy;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public abstract class EssentiaItemAccess {
    protected final Player player;

    private EssentiaItemAccess(Player player) {
        this.player = player;
    }

    public static EssentiaItemAccess forPlayerSlot(Player player, int slot) {
        return new EssentiaItemAccess(player) {
            @Override
            public ItemStack getStack() {
                return player.getInventory().getItem(slot);
            }

            @Override
            protected void setStack(ItemStack stack) {
                player.getInventory().setItem(slot, stack);
            }
        };
    }

    public static EssentiaItemAccess forPlayerCursor(Player player, AbstractContainerMenu menu) {
        return new EssentiaItemAccess(player) {
            @Override
            public ItemStack getStack() {
                return menu.getCarried();
            }

            @Override
            protected void setStack(ItemStack stack) {
                menu.setCarried(stack);
            }
        };
    }

    public static EssentiaItemAccess forPlayerInteraction(Player player, InteractionHand hand) {
        return new EssentiaItemAccess(player) {
            @Override
            public ItemStack getStack() {
                return player.getItemInHand(hand);
            }

            @Override
            protected void setStack(ItemStack stack) {
                player.setItemInHand(hand, stack);
            }

            @Override
            protected boolean infinite() {
                return player.hasInfiniteMaterials();
            }
        };
    }

    public abstract ItemStack getStack();

    protected abstract void setStack(ItemStack stack);

    protected boolean infinite() {
        return false;
    }

    public ItemStack getOne() {
        ItemStack stack = getStack();
        return stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
    }

    public boolean exchange(ItemStack replacement, boolean simulate) {
        ItemStack current = getStack();
        if (current.isEmpty()) {
            return false;
        }
        if (simulate) {
            return true;
        }
        if (infinite()) {
            if (!replacement.isEmpty() && !player.getInventory().contains(replacement)) {
                player.getInventory().placeItemBackInInventory(replacement.copy());
            }
            return true;
        }
        if (current.getCount() == 1) {
            setStack(replacement.copy());
        } else {
            current.shrink(1);
            setStack(current);
            if (!replacement.isEmpty()) {
                player.getInventory().placeItemBackInInventory(replacement.copy());
            }
        }
        return true;
    }
}
