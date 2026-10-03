package com.traverse.appliedthaumaturge.integration.wtlib;

import appeng.menu.ISubMenu;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.util.inv.AppEngInternalInventory;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneWorkbenchContext;
import com.traverse.appliedthaumaturge.arcane.ArcaneTerminalHost;
import com.traverse.appliedthaumaturge.item.WirelessArcaneTerminalItem;
import com.traverse.appliedthaumaturge.registry.ATTerminalHotkeys;
import de.mari_023.ae2wtlib.api.terminal.ItemWT;
import de.mari_023.ae2wtlib.api.terminal.WTMenuHost;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class ArcaneWTMenuHost extends WTMenuHost implements ArcaneTerminalHost {
    private final WirelessArcaneTerminalItem.Host arcaneHost;

    public ArcaneWTMenuHost(ItemWT item, Player player, ItemMenuHostLocator locator,
                           BiConsumer<Player, ISubMenu> returnToMainMenu) {
        super(item, player, locator, returnToMainMenu);
        arcaneHost = new WirelessArcaneTerminalItem.Host(item, player, locator);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && (arcaneHost == null || arcaneHost.isValid());
    }

    @Override
    public String getCloseHotkey() {
        return ATTerminalHotkeys.WIRELESS_ARCANE_TERMINAL;
    }

    @Override
    public AppEngInternalInventory getArcaneInventory() {
        return arcaneHost.getArcaneInventory();
    }

    @Override
    public ArcaneWorkbenchContext getArcaneContext(ServerPlayer player) {
        return arcaneHost.getArcaneContext(player);
    }

    @Override
    public boolean matchesContext(ArcaneWorkbenchContext context) {
        return isValid() && arcaneHost.matchesContext(context);
    }

    @Override
    public BlockPos getAuraPosition() {
        return arcaneHost.getAuraPosition();
    }

    @Override
    public boolean hasChargingCard() {
        return arcaneHost.hasChargingCard();
    }

    @Override
    public void saveArcaneInventory() {
        arcaneHost.saveArcaneInventory();
    }
}
