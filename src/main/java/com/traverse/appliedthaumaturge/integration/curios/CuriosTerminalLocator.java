package com.traverse.appliedthaumaturge.integration.curios;

import appeng.menu.locator.ItemMenuHostLocator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;

public record CuriosTerminalLocator(String identifier, int slot) implements ItemMenuHostLocator {
    @Override
    @Nullable
    public BlockHitResult hitResult() {
        return null;
    }

    @Override
    public ItemStack locateItem(Player player) {
        var inventory = CuriosApi.getCuriosInventoryOrNull(player);
        if (inventory == null || slot < 0) {
            return ItemStack.EMPTY;
        }
        var handler = inventory.getStacksHandler(identifier).orElse(null);
        if (handler == null || slot >= handler.getStacks().getSlots() || !inventory.isSlotActive(identifier, slot)) {
            return ItemStack.EMPTY;
        }
        return handler.getStacks().getStackInSlot(slot);
    }

    public void writeToPacket(FriendlyByteBuf buffer) {
        buffer.writeUtf(identifier);
        buffer.writeVarInt(slot);
    }

    public static CuriosTerminalLocator readFromPacket(FriendlyByteBuf buffer) {
        return new CuriosTerminalLocator(buffer.readUtf(), buffer.readVarInt());
    }
}
