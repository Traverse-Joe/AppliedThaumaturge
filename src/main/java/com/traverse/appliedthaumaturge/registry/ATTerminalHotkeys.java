package com.traverse.appliedthaumaturge.registry;

import appeng.api.features.HotkeyAction;
import appeng.hotkeys.InventoryHotkeyAction;
import appeng.items.tools.powered.WirelessTerminalItem;
import com.traverse.appliedthaumaturge.integration.curios.CuriosTerminalIntegration;
import net.neoforged.fml.ModList;

public final class ATTerminalHotkeys {
    public static final String WIRELESS_ESSENTIA_TERMINAL = "appliedthaumaturge.wireless_essentia_terminal";
    public static final String WIRELESS_ARCANE_TERMINAL = "appliedthaumaturge.wireless_arcane_terminal";

    private ATTerminalHotkeys() {
    }

    public static void register() {
        register(ATItems.WIRELESS_ESSENTIA_TERMINAL.get(), WIRELESS_ESSENTIA_TERMINAL);
        register(ATItems.WIRELESS_ARCANE_TERMINAL.get(), WIRELESS_ARCANE_TERMINAL);
        if (ModList.get().isLoaded("curios")) {
            CuriosTerminalIntegration.register();
        }
    }

    private static void register(WirelessTerminalItem item, String id) {
        HotkeyAction.register(new InventoryHotkeyAction(item, item::openFromInventory), id);
    }
}
