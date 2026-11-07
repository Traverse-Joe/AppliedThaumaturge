package com.traverse.appliedthaumaturge.mixin;

import com.traverse.appliedthaumaturge.AppliedThaumaturge;
import com.traverse.appliedthaumaturge.registry.ATItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
    @Unique
    private static final ResourceLocation appliedthaumaturge$KNOWLEDGE_CORE_ICON = AppliedThaumaturge.id("textures/item/knowledge_core.png");

    @Inject(method = "blit(Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V", at = @At("HEAD"), cancellable = true)
    private void appliedthaumaturge$drawAnimatedCategoryIcon(ResourceLocation texture, int x, int y, float u, float v,
                                                         int width, int height, int textureWidth, int textureHeight,
                                                         CallbackInfo ci) {
        if (!appliedthaumaturge$KNOWLEDGE_CORE_ICON.equals(texture)) {
            return;
        }
        GuiGraphics graphics = (GuiGraphics) (Object) this;
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(width / 16.0F, height / 16.0F, 1.0F);
        graphics.renderItem(ATItems.KNOWLEDGE_CORE.toStack(), 0, 0);
        graphics.pose().popPose();
        ci.cancel();
    }
}
