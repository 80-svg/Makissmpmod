package org.example.makismod.makissmpmod.mixin.client;

import net.minecraft.client.player.LocalPlayer;
import org.example.makismod.makissmpmod.client.LockoutClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class BlockInputMixin {
    @Inject(method = "aiStep", at = @At("HEAD"))
    private void onAiStep(CallbackInfo callbackInfo) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        if (LockoutClientState.isInputLocked()) {
            LockoutClientState.applyCapturedInput(self);
        }
    }
}
