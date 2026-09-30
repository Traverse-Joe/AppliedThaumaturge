package com.traverse.appliedthaumics.client;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ProgressBar;
import appeng.client.gui.widgets.BackgroundPanel;
import com.traverse.appliedthaumics.menu.ArcaneAssemblerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.util.List;
import net.minecraft.world.entity.player.Inventory;

public class ArcaneAssemblerScreen extends UpgradeableScreen<ArcaneAssemblerMenu> {
    private final ProgressBar progressBar;

    public ArcaneAssemblerScreen(ArcaneAssemblerMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        widgets.add("wandSlot", new BackgroundPanel(style.getImage("wandSlot")));
        progressBar = new ProgressBar(menu, style.getImage("progressBar"), ProgressBar.Direction.VERTICAL);
        widgets.add("progressBar", progressBar);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int x = mouseX - getGuiLeft();
        int y = mouseY - getGuiTop();
        if (x >= 100 && x < 168 && y >= 37 && y < 45) {
            drawTooltip(graphics, mouseX, mouseY, List.of(Component.translatable(
                    "gui.thaumaturge.arcane_workbench.vis_available", getMenu().availableVis)));
        }
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        Component available = Component.translatable("gui.thaumaturge.arcane_workbench.vis_available", getMenu().availableVis);
        setTextContent("vis_available", available.copy().withColor(0x6E6EEE));
        progressBar.setFullMsg(Component.translatable("gui.appliedthaumics.progress", getMenu().getCurrentProgress())
                .append("\n").append(available));
    }
}
