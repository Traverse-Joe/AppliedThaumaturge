package com.traverse.appliedthaumics.client;

import appeng.client.api.AEKeyRenderer;
import appeng.util.Platform;
import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledge;
import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.client.AspectRendering;
import com.mojang.blaze3d.vertex.PoseStack;
import com.traverse.appliedthaumics.me.key.EssentiaKey;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class EssentiaKeyRenderer implements AEKeyRenderer<EssentiaKey, EssentiaKeyRenderer.State> {
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
    public void drawInGui(Minecraft minecraft, GuiGraphicsExtractor graphics, int x, int y, EssentiaKey key) {
        Holder<IAspect> aspect = resolve(key);
        if (aspect == null) {
            AspectRendering.renderMissingGui(graphics, x, y);
            return;
        }
        AspectRendering.renderGui(graphics, minecraft.font, x, y, aspect, 0.0F);
    }

    @Override
    public Class<State> stateClass() {
        return State.class;
    }

    @Override
    public State createState() {
        return new State();
    }

    @Override
    public void extract(State state, EssentiaKey key, @Nullable Level level, int seed) {
        state.aspect = level != null ? key.holderOrNull(level.registryAccess()) : resolve(key);
        state.knowledge = state.aspect != null ? AspectKnowledgeAccess.of(state.aspect) : AspectKnowledge.UNKNOWN;
    }

    @Override
    public void submit(PoseStack poseStack, State state, SubmitNodeCollector nodes, int lightCoords) {
        Holder<IAspect> aspect = state.aspect;
        AspectKnowledge knowledge = state.knowledge;
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 0.01F);
        if (aspect == null) {
            nodes.submitCustomGeometry(poseStack, AspectRendering.missingRenderType(AspectRendering.BlendMode.ALPHA),
                    (pose, buffer) -> AspectRendering.renderMissingQuad(pose, buffer, 1.0F, lightCoords));
        } else {
            nodes.submitCustomGeometry(poseStack, AspectRendering.renderType(aspect, knowledge, AspectRendering.BlendMode.ALPHA),
                    (pose, buffer) -> AspectRendering.renderQuad(pose, buffer, aspect, knowledge, 1.0F, false, lightCoords));
        }
        poseStack.popPose();
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

    public static final class State {
        @Nullable
        Holder<IAspect> aspect;
        AspectKnowledge knowledge = AspectKnowledge.UNKNOWN;
    }
}
