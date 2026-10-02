package com.traverse.appliedthaumaturge.menu;

import appeng.menu.me.common.MEStorageMenu;
import com.traverse.appliedthaumaturge.item.PortableEssentiaCellItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class PortableEssentiaCellMenu extends MEStorageMenu {
    public PortableEssentiaCellMenu(MenuType<?> type, int id, Inventory inventory, PortableEssentiaCellItem.Host host) {
        super(type, id, inventory, host);
    }

    @Override
    protected boolean hideViewCells() {
        return true;
    }

    @Override
    protected boolean showsCraftables() {
        return false;
    }
}
