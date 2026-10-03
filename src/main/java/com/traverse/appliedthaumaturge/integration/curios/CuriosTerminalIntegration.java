package com.traverse.appliedthaumaturge.integration.curios;

import appeng.api.features.HotkeyAction;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.menu.locator.MenuLocators;
import com.traverse.appliedthaumaturge.registry.ATItems;
import com.traverse.appliedthaumaturge.registry.ATTerminalHotkeys;
import top.theillusivec4.curios.api.CuriosApi;

public final class CuriosTerminalIntegration {
    private CuriosTerminalIntegration() {
    }

    public static void register() {
        MenuLocators.register(CuriosTerminalLocator.class, CuriosTerminalLocator::writeToPacket,
                CuriosTerminalLocator::readFromPacket);
        register(ATItems.WIRELESS_ESSENTIA_TERMINAL.get(), ATTerminalHotkeys.WIRELESS_ESSENTIA_TERMINAL);
        register(ATItems.WIRELESS_ARCANE_TERMINAL.get(), ATTerminalHotkeys.WIRELESS_ARCANE_TERMINAL);
    }

    private static void register(WirelessTerminalItem item, String id) {
        HotkeyAction.register(player -> {
            var inventory = CuriosApi.getCuriosInventoryOrNull(player);
            if (inventory == null) {
                return false;
            }
            for (var entry : inventory.getCurios().entrySet()) {
                var stacks = entry.getValue().getStacks();
                for (int slot = 0; slot < stacks.getSlots(); slot++) {
                    if (inventory.isSlotActive(entry.getKey(), slot) && stacks.getStackInSlot(slot).is(item)
                            && item.openFromInventory(player, new CuriosTerminalLocator(entry.getKey(), slot))) {
                        return true;
                    }
                }
            }
            return false;
        }, id);
    }
}
