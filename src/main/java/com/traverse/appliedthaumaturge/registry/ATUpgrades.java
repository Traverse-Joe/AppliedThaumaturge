package com.traverse.appliedthaumaturge.registry;

import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;
import appeng.api.features.GridLinkables;
import appeng.core.localization.GuiText;
import appeng.items.tools.powered.WirelessTerminalItem;

public final class ATUpgrades {
    private ATUpgrades() {
    }

    public static void register() {
        String cells = GuiText.StorageCells.getTranslationKey();
        for (var cell : ATItems.cells()) {
            Upgrades.add(AEItems.INVERTER_CARD, cell, 1, cells);
            Upgrades.add(AEItems.EQUAL_DISTRIBUTION_CARD, cell, 1, cells);
            Upgrades.add(AEItems.VOID_CARD, cell, 1, cells);
        }

        for (var cell : ATItems.portableCells()) {
            Upgrades.add(AEItems.INVERTER_CARD, cell, 1, cells);
            Upgrades.add(AEItems.EQUAL_DISTRIBUTION_CARD, cell, 1, cells);
            Upgrades.add(AEItems.VOID_CARD, cell, 1, cells);
            Upgrades.add(AEItems.ENERGY_CARD, cell, 2, cells);
        }

        String buses = "group.appliedthaumaturge.essentia_io_buses";
        Upgrades.add(AEItems.REDSTONE_CARD, ATItems.ESSENTIA_IMPORT_BUS, 1, buses);
        Upgrades.add(AEItems.CAPACITY_CARD, ATItems.ESSENTIA_IMPORT_BUS, 5, buses);
        Upgrades.add(AEItems.SPEED_CARD, ATItems.ESSENTIA_IMPORT_BUS, 4, buses);
        Upgrades.add(AEItems.INVERTER_CARD, ATItems.ESSENTIA_IMPORT_BUS, 1, buses);
        Upgrades.add(AEItems.REDSTONE_CARD, ATItems.ESSENTIA_EXPORT_BUS, 1, buses);
        Upgrades.add(AEItems.CAPACITY_CARD, ATItems.ESSENTIA_EXPORT_BUS, 5, buses);
        Upgrades.add(AEItems.SPEED_CARD, ATItems.ESSENTIA_EXPORT_BUS, 4, buses);

        Upgrades.add(AEItems.INVERTER_CARD, ATItems.ESSENTIA_STORAGE_BUS, 1);
        Upgrades.add(AEItems.CAPACITY_CARD, ATItems.ESSENTIA_STORAGE_BUS, 5);
        Upgrades.add(AEItems.VOID_CARD, ATItems.ESSENTIA_STORAGE_BUS, 1);

        Upgrades.add(ATItems.UPGRADE_ARCANE, ATItems.ARCANE_TERMINAL, 1);
        Upgrades.add(ATItems.UPGRADE_ARCANE, ATBlocks.ARCANE_ASSEMBLER_ITEM, 1);
        Upgrades.add(AEItems.SPEED_CARD, ATBlocks.ARCANE_ASSEMBLER_ITEM, 5);
        Upgrades.add(AEItems.CAPACITY_CARD, ATBlocks.ARCANE_ASSEMBLER_ITEM, 2);

        Upgrades.add(AEItems.ENERGY_CARD, ATItems.WIRELESS_ESSENTIA_TERMINAL, 2, GuiText.WirelessTerminals.getTranslationKey());
        Upgrades.add(ATItems.UPGRADE_ALCHEMY, ATItems.WIRELESS_ESSENTIA_TERMINAL, 1);
        GridLinkables.register(ATItems.WIRELESS_ESSENTIA_TERMINAL, WirelessTerminalItem.LINKABLE_HANDLER);
        Upgrades.add(AEItems.ENERGY_CARD, ATItems.WIRELESS_ARCANE_TERMINAL, 2, GuiText.WirelessTerminals.getTranslationKey());
        Upgrades.add(ATItems.UPGRADE_ARCANE, ATItems.WIRELESS_ARCANE_TERMINAL, 1);
        Upgrades.add(ATItems.UPGRADE_NODE, ATItems.WIRELESS_ARCANE_TERMINAL, 1);
        GridLinkables.register(ATItems.WIRELESS_ARCANE_TERMINAL, WirelessTerminalItem.LINKABLE_HANDLER);
    }
}
