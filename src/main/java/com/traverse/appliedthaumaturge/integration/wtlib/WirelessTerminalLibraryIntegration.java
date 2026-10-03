package com.traverse.appliedthaumaturge.integration.wtlib;

import appeng.helpers.WirelessTerminalMenuHost;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.api.storage.ITerminalHost;
import appeng.api.upgrades.Upgrades;
import appeng.menu.SlotSemantic;
import appeng.menu.slot.RestrictedInputSlot;
import de.mari_023.ae2wtlib.api.AE2wtlibAPI;
import de.mari_023.ae2wtlib.api.gui.AE2wtlibSlotSemantics;
import de.mari_023.ae2wtlib.api.terminal.WTMenuHost;
import de.mari_023.ae2wtlib.api.terminal.WUTHandler;
import de.mari_023.ae2wtlib.api.registration.WTDefinition;
import java.util.function.BiConsumer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import com.traverse.appliedthaumaturge.AppliedThaumaturge;
import com.traverse.appliedthaumaturge.registry.ATItems;
import com.traverse.appliedthaumaturge.registry.ATMenus;
import com.traverse.appliedthaumaturge.registry.ATTerminalHotkeys;
import de.mari_023.ae2wtlib.api.gui.Icon;
import de.mari_023.ae2wtlib.api.registration.AddTerminalEvent;
import de.mari_023.ae2wtlib.api.terminal.ItemWT;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public final class WirelessTerminalLibraryIntegration {
    public static final String ESSENTIA = "appliedthaumaturge_essentia";
    public static final String ARCANE = "appliedthaumaturge_arcane";

    private WirelessTerminalLibraryIntegration() {
    }

    public static void register() {
        AddTerminalEvent.register(event -> {
            event.builder(ESSENTIA, EssentiaWTMenuHost::new, ATMenus.WIRELESS_ESSENTIA_TERMINAL,
                    (ItemWT) ATItems.WIRELESS_ESSENTIA_TERMINAL.get(), icon("wireless_essentia_terminal"))
                    .hotkeyName(ATTerminalHotkeys.WIRELESS_ESSENTIA_TERMINAL).addTerminal();
            event.builder(ARCANE, ArcaneWTMenuHost::new, ATMenus.WIRELESS_ARCANE_TERMINAL,
                    (ItemWT) ATItems.WIRELESS_ARCANE_TERMINAL.get(), icon("wireless_arcane_terminal"))
                    .hotkeyName(ATTerminalHotkeys.WIRELESS_ARCANE_TERMINAL).addTerminal();
        });
    }

    private static Icon icon(String name) {
        return new Icon(0, 0, 16, 16,
                new Icon.Texture(AppliedThaumaturge.id("textures/item/" + name + ".png"), 16, 16));
    }

    public static WirelessTerminalItem essentiaItem(Item.Properties properties) {
        return new LibraryTerminalItem(properties, false);
    }

    public static WirelessTerminalItem arcaneItem(Item.Properties properties) {
        return new LibraryTerminalItem.Arcane(properties);
    }

    public static WirelessTerminalMenuHost<?> essentiaHost(WirelessTerminalItem item, Player player, ItemMenuHostLocator locator) {
        return new EssentiaWTMenuHost((ItemWT) item, player, locator, (p, menu) -> item.openFromInventory(p, locator));
    }

    public static void addSlots(ITerminalHost host, BiConsumer<Slot, SlotSemantic> addSlot) {
        if (host instanceof WTMenuHost wireless) {
            addSlot.accept(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.QE_SINGULARITY,
                    wireless.getSubInventory(WTMenuHost.INV_SINGULARITY), 0), AE2wtlibSlotSemantics.SINGULARITY);
        }
    }

    public static void registerUpgrades() {
        Upgrades.add(ATItems.UPGRADE_ALCHEMY, AE2wtlibAPI.getWUT(), 1);
        Upgrades.add(ATItems.UPGRADE_ARCANE, AE2wtlibAPI.getWUT(), 1);
        Upgrades.add(ATItems.UPGRADE_NODE, AE2wtlibAPI.getWUT(), 1);
    }

    public static boolean openUniversal(WirelessTerminalItem item, Player player, ItemMenuHostLocator locator) {
        ItemStack stack = locator.locateItem(player);
        if (!AE2wtlibAPI.isUniversalTerminal(stack)) {
            return false;
        }
        WTDefinition terminal = WTDefinition.of(item.getDefaultInstance());
        if (!WUTHandler.hasTerminal(stack, terminal)) {
            return false;
        }
        WUTHandler.setCurrentTerminal(player, locator, stack, terminal);
        return terminal.item().tryOpen(player, locator, false);
    }
}
