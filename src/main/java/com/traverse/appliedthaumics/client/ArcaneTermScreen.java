package com.traverse.appliedthaumics.client;

import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.style.ScreenStyle;
import com.traverse.appliedthaumics.menu.ArcaneTermMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ArcaneTermScreen extends MEStorageScreen<ArcaneTermMenu> {
    public ArcaneTermScreen(ArcaneTermMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        ArcaneTermMenu menu = getMenu();
        boolean shortfall = menu.visCost > menu.availableVis;
        setTextContent("vis_available", Component.translatable("gui.thaumaturge.arcane_workbench.vis_available", menu.availableVis)
                .withColor(shortfall ? 0xEE6E6E : 0x6E6EEE));
        Component required = Component.empty();
        if (menu.visCost > 0) {
            if (menu.visCost < menu.baseVis) {
                int discount = Math.round(100.0F - menu.visCost * 100.0F / menu.baseVis);
                required = Component.translatable("gui.thaumaturge.arcane_workbench.required_vis_discount", menu.visCost, discount);
            } else if (menu.crudeCost) {
                required = Component.translatable("gui.thaumaturge.arcane_workbench.required_vis_crude", menu.visCost);
            } else {
                required = Component.translatable("gui.thaumaturge.arcane_workbench.required_vis", menu.visCost);
            }
            required = required.copy().withColor(0xC0FFFF);
        }
        setTextContent("vis_required", required);
    }
}
