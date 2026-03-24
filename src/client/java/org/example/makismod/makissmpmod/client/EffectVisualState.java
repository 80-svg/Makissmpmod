package org.example.makismod.makissmpmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.example.makismod.makissmpmod.ModEffects;

public final class EffectVisualState {
    private static final int STILL_TICKS_UNTIL_DROWSY = 60;
    private static final double MOVEMENT_EPSILON_SQUARED = 1.0E-4D;
    private static final float MAX_CAMERA_ROLL_DEGREES = 6.0F;
    private static int stillTicks;
    private static float lethargyClosure;
    private static float drowsynessClosure;

    private EffectVisualState() {
    }

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            stillTicks = 0;
            lethargyClosure = approach(lethargyClosure, 0.0F, 0.08F);
            drowsynessClosure = approach(drowsynessClosure, 0.0F, 0.08F);
            return;
        }

        tickLethargy(player);
        tickDrowsyness(player);
    }

    public static float getVignetteStrength(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || !player.hasEffect(ModEffects.LETHARGY)) {
            return 0.0F;
        }

        return 0.35F + 0.25F * lethargyClosure;
    }

    public static float getCameraRollDegrees() {
        return MAX_CAMERA_ROLL_DEGREES * lethargyClosure;
    }

    public static void renderEyeClosure(net.minecraft.client.gui.GuiGraphics guiGraphics) {
        float closure = Math.max(lethargyClosure, drowsynessClosure);
        if (closure <= 0.0F) {
            return;
        }

        boolean closeCompletely = drowsynessClosure >= lethargyClosure;
        EyeClosureOverlay.render(guiGraphics, closure, closeCompletely);
    }

    private static void tickLethargy(LocalPlayer player) {
        if (!player.hasEffect(ModEffects.LETHARGY)) {
            stillTicks = 0;
            lethargyClosure = approach(lethargyClosure, 0.0F, 0.08F);
            return;
        }

        double dx = player.getX() - player.xOld;
        double dy = player.getY() - player.yOld;
        double dz = player.getZ() - player.zOld;
        boolean isStandingStill = dx * dx + dy * dy + dz * dz <= MOVEMENT_EPSILON_SQUARED;

        if (isStandingStill) {
            stillTicks++;
        } else {
            stillTicks = 0;
        }

        float targetClosure = stillTicks >= STILL_TICKS_UNTIL_DROWSY ? 1.0F : 0.0F;
        lethargyClosure = approach(lethargyClosure, targetClosure, isStandingStill ? 0.035F : 0.09F);
    }

    private static void tickDrowsyness(LocalPlayer player) {
        float targetClosure = player.hasEffect(ModEffects.DROWSYNESS) ? 1.0F : 0.0F;
        drowsynessClosure = approach(drowsynessClosure, targetClosure, targetClosure > drowsynessClosure ? 0.12F : 0.08F);
    }

    private static float approach(float current, float target, float step) {
        if (current < target) {
            return Math.min(current + step, target);
        }

        return Math.max(current - step, target);
    }
}
