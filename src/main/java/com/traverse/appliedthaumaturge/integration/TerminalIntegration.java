package com.traverse.appliedthaumaturge.integration;

import appeng.core.AEConfig;
import appeng.api.storage.ITerminalHost;
import appeng.menu.SlotSemantic;
import appeng.helpers.WirelessTerminalMenuHost;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.menu.locator.ItemMenuHostLocator;
import com.traverse.appliedthaumaturge.integration.wtlib.WirelessTerminalLibraryIntegration;
import com.traverse.appliedthaumaturge.item.WirelessArcaneTerminalItem;
import com.traverse.appliedthaumaturge.item.WirelessEssentiaTerminalItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.inventory.Slot;
import java.util.function.BiConsumer;
import net.neoforged.fml.ModList;

public final class TerminalIntegration {
    private TerminalIntegration() {
    }

    public static void register() {
        if (ModList.get().isLoaded("ae2wtlib")) {
            WirelessTerminalLibraryIntegration.register();
        }
    }

    public static WirelessTerminalItem essentiaItem(Item.Properties properties) {
        return ModList.get().isLoaded("ae2wtlib")
                ? WirelessTerminalLibraryIntegration.essentiaItem(properties)
                : new WirelessEssentiaTerminalItem(AEConfig.instance().getWirelessTerminalBattery(), properties.stacksTo(1));
    }

    public static WirelessTerminalItem arcaneItem(Item.Properties properties) {
        return ModList.get().isLoaded("ae2wtlib")
                ? WirelessTerminalLibraryIntegration.arcaneItem(properties)
                : new WirelessArcaneTerminalItem(AEConfig.instance().getWirelessTerminalBattery(), properties.stacksTo(1));
    }

    public static WirelessTerminalMenuHost<?> essentiaHost(WirelessTerminalItem item, Player player, ItemMenuHostLocator locator) {
        return ModList.get().isLoaded("ae2wtlib")
                ? WirelessTerminalLibraryIntegration.essentiaHost(item, player, locator)
                : new WirelessEssentiaTerminalItem.Host(item, player, locator);
    }

    public static void addSlots(ITerminalHost host, BiConsumer<Slot, SlotSemantic> addSlot) {
        if (ModList.get().isLoaded("ae2wtlib")) {
            WirelessTerminalLibraryIntegration.addSlots(host, addSlot);
        }
    }

    public static void registerUpgrades() {
        if (ModList.get().isLoaded("ae2wtlib")) {
            WirelessTerminalLibraryIntegration.registerUpgrades();
        }
    }

    public static boolean openFromCurios(WirelessTerminalItem item, Player player, ItemMenuHostLocator locator) {
        if (locator.locateItem(player).is(item)) {
            return item.openFromInventory(player, locator);
        }
        return ModList.get().isLoaded("ae2wtlib")
                && WirelessTerminalLibraryIntegration.openUniversal(item, player, locator);
    }
}
