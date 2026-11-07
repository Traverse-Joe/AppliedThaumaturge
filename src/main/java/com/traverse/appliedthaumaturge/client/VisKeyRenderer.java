package com.traverse.appliedthaumaturge.client;

import appeng.api.client.AEKeyRenderHandler;
import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEItemKey;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.client.AspectRendering;
import com.mojang.blaze3d.vertex.PoseStack;
import com.traverse.appliedthaumaturge.me.key.VisKey;
import com.traverse.appliedthaumaturge.registry.ATItems;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class VisKeyRenderer implements AEKeyRenderHandler<VisKey> {
    public static final VisKeyRenderer INSTANCE = new VisKeyRenderer();

    private VisKeyRenderer() {
    }

    @Nullable
    static Holder<IAspect> resolve(VisKey key) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return null;
        }
        return key.holderOrNull(mc.level.registryAccess());
    }

    @Override
    public void drawInGui(Minecraft minecraft, GuiGraphics graphics, int x, int y, VisKey key) {
        if (key.equals(VisKey.AURA)) {
            AEKeyRendering.drawInGui(minecraft, graphics, x, y, AEItemKey.of(ATItems.UPGRADE_ARCANE.get()));
            return;
        }
        Holder<IAspect> aspect = resolve(key);
        if (aspect == null) {
            AspectRendering.renderMissingGui(graphics, x, y);
            return;
        }
        AspectRendering.renderGui(graphics, minecraft.font, x, y, aspect, 0.0F);
    }

    @Override
    public void drawOnBlockFace(PoseStack poseStack, MultiBufferSource buffers, VisKey key, float scale, int combinedLight, Level level) {
        Holder<IAspect> aspect = level != null ? key.holderOrNull(level.registryAccess()) : resolve(key);
        EssentiaKeyRenderer.drawAspectOnBlockFace(poseStack, buffers, aspect, scale, combinedLight);
    }

    @Override
    public Component getDisplayName(VisKey key) {
        return key.getDisplayName();
    }

    @Override
    public List<Component> getTooltip(VisKey key) {
        return List.of(key.getDisplayName());
    }
}
