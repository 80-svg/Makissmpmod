package org.example.makismod.makissmpmod.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.example.makismod.makissmpmod.client.EffectVisualState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class EyeClosureOverlayMixin {
    @Inject(method = "renderCameraOverlays", at = @At("TAIL"))
    private void renderEyeClosureBeforeHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        EffectVisualState.renderEyeClosure(guiGraphics);
    }
}
