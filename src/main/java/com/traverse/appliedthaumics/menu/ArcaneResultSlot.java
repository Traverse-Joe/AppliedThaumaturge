package com.traverse.appliedthaumics.menu;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.MEStorage;
import appeng.helpers.InventoryAction;
import appeng.menu.slot.CraftingTermSlot;
import appeng.util.inv.CarriedItemInventory;
import appeng.util.inv.PlayerInternalInventory;
import com.traverse.appliedthaumics.arcane.ArcaneCrafter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ArcaneResultSlot extends CraftingTermSlot {
    private final ArcaneTermMenu menu;
    private final IActionSource source;
    private final IEnergySource energy;
    private final MEStorage storage;

    public ArcaneResultSlot(Player player, IActionSource source, IEnergySource energy, MEStorage storage, InternalInventory grid, ArcaneTermMenu menu) {
        super(player, source, energy, storage, grid, grid, menu);
        this.menu = menu;
        this.source = source;
        this.energy = energy;
        this.storage = storage;
    }

    @Override
    public void doClick(InventoryAction action, Player who) {
        if (getItem().isEmpty() || !(who instanceof ServerPlayer player)) {
            return;
        }
        int perCraft = getItem().getCount();
        InternalInventory target;
        int times;
        switch (action) {
            case CRAFT_SHIFT -> {
                target = new PlayerInternalInventory(who.getInventory());
                times = Math.max(1, getItem().getMaxStackSize() / perCraft);
            }
            case CRAFT_ALL -> {
                target = new PlayerInternalInventory(who.getInventory());
                times = Math.max(1, getItem().getMaxStackSize() / perCraft * 36);
            }
            case CRAFT_STACK -> {
                target = new CarriedItemInventory(menu);
                times = Math.max(1, getItem().getMaxStackSize() / perCraft);
            }
            default -> {
                if (menu.getCarried().isEmpty()) {
                    menu.setCarried(craftOnce(player));
                    return;
                }
                target = new CarriedItemInventory(menu);
                times = 1;
            }
        }
        ItemStack start = getItem().copy();
        for (int i = 0; i < times; i++) {
            if (!ItemStack.isSameItemSameComponents(start, getItem()) || !target.simulateAdd(start).isEmpty()) {
                return;
            }
            ItemStack crafted = craftOnce(player);
            if (crafted.isEmpty()) {
                return;
            }
            ItemStack extra = target.addItems(crafted);
            if (!extra.isEmpty()) {
                player.drop(extra, false);
                return;
            }
        }
    }

    private ItemStack craftOnce(ServerPlayer player) {
        ItemStack crafted = ArcaneCrafter.craft(player, menu.getPart(), menu.getArcaneInventory(), storage, energy, source);
        if (!crafted.isEmpty()) {
            crafted.onCraftedBy(player, crafted.getCount());
        }
        menu.updateOutput();
        return crafted;
    }
}
