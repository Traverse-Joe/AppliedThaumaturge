package com.traverse.appliedthaumaturge.menu;

import appeng.helpers.WirelessTerminalMenuHost;
import appeng.menu.me.common.MEStorageMenu;
import com.traverse.appliedthaumaturge.integration.TerminalIntegration;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public final class WirelessEssentiaTerminalMenu extends MEStorageMenu {
    public WirelessEssentiaTerminalMenu(MenuType<?> type, int id, Inventory inventory, WirelessTerminalMenuHost<?> host) {
        super(type, id, inventory, host);
        TerminalIntegration.addSlots(host, this::addSlot);
    }
}
