package com.traverse.appliedthaumaturge.integration.wtlib.client;

import appeng.client.gui.style.ScreenStyle;
import com.traverse.appliedthaumaturge.client.ArcaneTermScreen;
import com.traverse.appliedthaumaturge.menu.ArcaneTermMenu;
import de.mari_023.ae2wtlib.api.AE2wtlibAPI;
import de.mari_023.ae2wtlib.api.gui.ScrollingUpgradesPanel;
import de.mari_023.ae2wtlib.api.terminal.IUniversalTerminalCapable;
import de.mari_023.ae2wtlib.api.terminal.WTMenuHost;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class ArcaneWTScreen extends ArcaneTermScreen implements IUniversalTerminalCapable {
    private final ScrollingUpgradesPanel upgradesPanel;

    public ArcaneWTScreen(ArcaneTermMenu menu, Inventory inventory, Component title, ScreenStyle style) {
        super(menu, inventory, title, style);
        if (AE2wtlibAPI.isUniversalTerminal(getHost().getItemStack())) {
            addTerminalSelectionPanel(widgets);
        }
        upgradesPanel = addUpgradePanel(widgets, menu);
    }

    @Override
    public void init() {
        super.init();
        upgradesPanel.setMaxRows(Math.max(2, getVisibleRows()));
    }

    @Override
    public WTMenuHost getHost() {
        return (WTMenuHost) getMenu().getHost();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return super.keyPressed(event) || checkForTerminalKeys(event);
    }
}
