package com.traverse.appliedthaumics.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.traverse.appliedthaumics.AppliedThaumics;
import com.traverse.appliedthaumics.registry.ATItems;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMixin {
    @Unique
    private static final Identifier appliedthaumics$KNOWLEDGE_CORE_ICON = AppliedThaumics.id("textures/item/knowledge_core.png");

    @Inject(method = "blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIIIIII)V", at = @At("HEAD"), cancellable = true)
    private void appliedthaumics$drawAnimatedCategoryIcon(RenderPipeline pipeline, Identifier texture, int x, int y, float u, float v,
                                                         int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight,
                                                         int color, CallbackInfo ci) {
        if (!appliedthaumics$KNOWLEDGE_CORE_ICON.equals(texture)) {
            return;
        }
        GuiGraphicsExtractor graphics = (GuiGraphicsExtractor) (Object) this;
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(width / 16.0F, height / 16.0F);
        graphics.item(ATItems.KNOWLEDGE_CORE.toStack(), 0, 0);
        graphics.pose().popMatrix();
        ci.cancel();
    }
}
