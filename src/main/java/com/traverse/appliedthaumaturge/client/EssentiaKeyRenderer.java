package com.traverse.appliedthaumaturge.client;

import appeng.api.client.AEKeyRenderHandler;
import appeng.util.Platform;
import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledge;
import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.client.AspectRendering;
import com.mojang.blaze3d.vertex.PoseStack;
import com.traverse.appliedthaumaturge.me.key.EssentiaKey;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class EssentiaKeyRenderer implements AEKeyRenderHandler<EssentiaKey> {
    public static final EssentiaKeyRenderer INSTANCE = new EssentiaKeyRenderer();

    private EssentiaKeyRenderer() {
    }

    @Nullable
    static Holder<IAspect> resolve(EssentiaKey key) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return null;
        }
        return key.holderOrNull(mc.level.registryAccess());
    }

    @Override
    public void drawInGui(Minecraft minecraft, GuiGraphics graphics, int x, int y, EssentiaKey key) {
        Holder<IAspect> aspect = resolve(key);
        if (aspect == null) {
            AspectRendering.renderMissingGui(graphics, x, y);
            return;
        }
        AspectRendering.renderGui(graphics, minecraft.font, x, y, aspect, 0.0F);
    }

    @Override
    public void drawOnBlockFace(PoseStack poseStack, MultiBufferSource buffers, EssentiaKey key, float scale, int combinedLight, Level level) {
        Holder<IAspect> aspect = level != null ? key.holderOrNull(level.registryAccess()) : resolve(key);
        drawAspectOnBlockFace(poseStack, buffers, aspect, scale, combinedLight);
    }

    static void drawAspectOnBlockFace(PoseStack poseStack, MultiBufferSource buffers, @Nullable Holder<IAspect> aspect, float scale, int combinedLight) {
        AspectKnowledge knowledge = aspect != null ? AspectKnowledgeAccess.of(aspect) : AspectKnowledge.UNKNOWN;
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 0.01F);
        poseStack.scale(scale, scale, 1.0F);
        if (aspect == null) {
            AspectRendering.renderMissingQuad(poseStack.last(),
                    buffers.getBuffer(AspectRendering.missingRenderType(AspectRendering.BlendMode.ALPHA)), 1.0F, combinedLight);
        } else {
            AspectRendering.renderQuad(poseStack.last(),
                    buffers.getBuffer(AspectRendering.renderType(aspect, knowledge, AspectRendering.BlendMode.ALPHA)),
                    aspect, knowledge, 1.0F, false, combinedLight);
        }
        poseStack.popPose();
    }

    @Override
    public Component getDisplayName(EssentiaKey key) {
        Holder<IAspect> aspect = resolve(key);
        return aspect == null ? key.getDisplayName() : AspectComponents.name(aspect);
    }

    @Override
    public List<Component> getTooltip(EssentiaKey key) {
        List<Component> tooltip = new ArrayList<>();
        Holder<IAspect> aspect = resolve(key);
        if (aspect == null) {
            tooltip.add(key.getDisplayName());
        } else {
            tooltip.add(AspectComponents.name(aspect));
            Component description = AspectComponents.description(aspect);
            if (!description.getString().isEmpty()) {
                tooltip.add(description.copy().withStyle(net.minecraft.ChatFormatting.GRAY));
            }
        }
        tooltip.add(Component.literal(Platform.formatModName(key.getModId())));
        return tooltip;
    }
}
