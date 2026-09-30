package com.traverse.appliedthaumics.wand;

import com.leclowndu93150.thaumaturge.api.wands.IWandRodOnUpdate;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class EntangledRodUpdate implements IWandRodOnUpdate {
    @Override
    public void onUpdate(ItemStack stack, Player player) {
        if (!player.level().isClientSide() && player.tickCount % 5 == 0) {
            EntangledWands.mirror(stack);
        }
    }
}
