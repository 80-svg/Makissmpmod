package org.example.makismod.makissmpmod.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.example.makismod.makissmpmod.client.EffectVisualState;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class LethargyCameraMixin {
    @Shadow @Final private static Vector3f FORWARDS;
    @Shadow @Final private static Vector3f UP;
    @Shadow @Final private static Vector3f LEFT;
    @Shadow private Vector3f forwards;
    @Shadow private Vector3f up;
    @Shadow private Vector3f left;
    @Shadow private Quaternionf rotation;

    @Inject(method = "setup", at = @At("TAIL"))
    private void applyLethargyCameraTilt(Level level, Entity entity, boolean detached, boolean mirrored, float partialTickTime, CallbackInfo ci) {
        float rollRadians = (float) Math.toRadians(EffectVisualState.getCameraRollDegrees());
        if (rollRadians == 0.0F) {
            return;
        }

        this.rotation.rotateLocalZ(rollRadians);
        this.forwards.set(this.rotation.transform(new Vector3f(FORWARDS)));
        this.up.set(this.rotation.transform(new Vector3f(UP)));
        this.left.set(this.rotation.transform(new Vector3f(LEFT)));
    }
}
