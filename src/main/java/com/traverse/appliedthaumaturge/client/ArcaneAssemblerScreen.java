package com.traverse.appliedthaumaturge.client;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ProgressBar;
import appeng.client.gui.widgets.BackgroundPanel;
import com.traverse.appliedthaumaturge.menu.ArcaneAssemblerMenu;
import com.traverse.appliedthaumaturge.assembler.ArcaneAssemblerBlockEntity;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.workbench.MenuArcaneWorkbench;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.util.List;
import net.minecraft.world.entity.player.Inventory;

public class ArcaneAssemblerScreen extends UpgradeableScreen<ArcaneAssemblerMenu> {
    private final ProgressBar progressBar;
    private static final int BAR_LEFT = 5;
    private static final int BAR_TOP = 29;
    private static final int BAR_HEIGHT = 6;
    private static final int BAR_WIDTH = 16;
    private static final int BAR_SPACING = 10;

    public ArcaneAssemblerScreen(ArcaneAssemblerMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        widgets.add("wandSlot", new BackgroundPanel(style.getImage("wandSlot")));
        progressBar = new ProgressBar(menu, style.getImage("progressBar"), ProgressBar.Direction.VERTICAL);
        widgets.add("progressBar", progressBar);
    }

    @Override
    public void drawBG(GuiGraphicsExtractor graphics, int offsetX, int offsetY, int mouseX, int mouseY, float partialTick) {
        super.drawBG(graphics, offsetX, offsetY, mouseX, mouseY, partialTick);
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        for (int index = 0; index < 6; index++) {
            int x = getGuiLeft() + BAR_LEFT;
            int y = getGuiTop() + BAR_TOP + index * BAR_SPACING;
            int amount = getMenu().getStoredVis(index);
            var aspect = level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY)
                    .getOrThrow(MenuArcaneWorkbench.PRIMAL_ORDER.get(index));
            graphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, 0xFF56468A);
            graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF251F35);
            int width = Math.clamp((int) Math.ceil(amount * BAR_WIDTH / (double) ArcaneAssemblerBlockEntity.MAX_STORED_CENTIVIS), 0, BAR_WIDTH);
            if (width > 0) {
                graphics.fill(x, y, x + width, y + BAR_HEIGHT, 0xFF000000 | aspect.value().color());
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int x = mouseX - getGuiLeft();
        int y = mouseY - getGuiTop();
        for (int index = 0; index < 6; index++) {
            int top = BAR_TOP + index * BAR_SPACING;
            if (x >= BAR_LEFT - 1 && x < BAR_LEFT + BAR_WIDTH + 1 && y >= top - 1 && y < top + BAR_HEIGHT + 1) {
                var aspect = MenuArcaneWorkbench.PRIMAL_ORDER.get(index);
                drawTooltip(graphics, mouseX, mouseY, List.of(Component.translatable(
                        "gui.appliedthaumaturge.stored_primal_vis",
                        Component.translatable("aspect." + aspect.identifier().getNamespace() + "." + aspect.identifier().getPath()),
                        String.format(java.util.Locale.ROOT, "%.2f", getMenu().getStoredVis(index) / 100.0),
                        ArcaneAssemblerBlockEntity.MAX_STORED_CENTIVIS / 100),
                        Component.translatable(getMenu().visLinked ? "gui.appliedthaumaturge.relay_linked" : "gui.appliedthaumaturge.relay_unlinked")));
                return;
            }
        }
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
        progressBar.setFullMsg(Component.translatable("gui.appliedthaumaturge.progress", getMenu().getCurrentProgress())
                .append("\n").append(available));
    }
}
