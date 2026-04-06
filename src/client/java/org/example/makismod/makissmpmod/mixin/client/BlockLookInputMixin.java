package org.example.makismod.makissmpmod.mixin.client;

import net.minecraft.client.MouseHandler;
import org.example.makismod.makissmpmod.client.LockoutClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class BlockLookInputMixin {
    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void onTurnPlayer(double cursorDelta, CallbackInfo callbackInfo) {
        if (LockoutClientState.isInputLocked()) {
            callbackInfo.cancel();
        }
    }
}
