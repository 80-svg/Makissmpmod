package org.example.makismod.makissmpmod.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.example.makismod.makissmpmod.client.EffectVisualState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class LethargyVignetteMixin {
    @Unique
    private static final Identifier LETHARGY_VIGNETTE =
            Identifier.fromNamespaceAndPath("minecraft", "textures/misc/vignette.png");

    @Shadow @Final private Minecraft minecraft;

    @Shadow
    protected abstract void renderTextureOverlay(GuiGraphics guiGraphics, Identifier identifier, float alpha);

    @Inject(method = "renderVignette", at = @At("TAIL"))
    private void renderLethargyVignette(GuiGraphics guiGraphics, Entity entity, CallbackInfo ci) {
        float strength = EffectVisualState.getVignetteStrength(this.minecraft);
        if (strength <= 0.0F) {
            return;
        }

        this.renderTextureOverlay(guiGraphics, LETHARGY_VIGNETTE, strength);
    }
}
