package org.example.makismod.makissmpmod.mixin.client;

import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import org.example.makismod.makissmpmod.client.LockoutClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends ClientInput {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo callbackInfo) {
        if (!LockoutClientState.isInputLocked()) {
            return;
        }

        LockoutClientState.ControlledInput input = LockoutClientState.getCapturedInput();
        this.keyPresses = input.toPlayerInput();
        this.moveVector = input.toMoveVector();
        callbackInfo.cancel();
    }
}
