package org.example.makismod.makissmpmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;

public final class LockoutClientState {
    private static boolean inputLocked;
    private static boolean captureEnabled;
    private static int cameraTargetEntityId = -1;
    private static ControlledInput capturedInput = ControlledInput.idle();

    private LockoutClientState() {}

    public static boolean isInputLocked() {
        return inputLocked;
    }

    public static void setInputLocked(boolean locked) {
        inputLocked = locked;
    }

    public static boolean isCaptureEnabled() {
        return captureEnabled;
    }

    public static void startCapturingInput() {
        captureEnabled = true;
    }

    public static void stopCapturingInput() {
        captureEnabled = false;
    }

    public static void setCameraTargetEntityId(int entityId) {
        cameraTargetEntityId = entityId;
    }

    public static void clearCameraTarget() {
        cameraTargetEntityId = -1;
    }

    public static ControlledInput getCapturedInput() {
        return capturedInput;
    }

    public static void setCapturedInput(ControlledInput input) {
        capturedInput = input;
    }

    public static void captureCurrentInput(Minecraft client) {
        if (!captureEnabled || inputLocked) {
            return;
        }

        LocalPlayer player = client.player;
        if (player == null) {
            return;
        }

        Options options = client.options;
        float strafe = axisValue(options.keyRight.isDown(), options.keyLeft.isDown());
        float vertical = axisValue(options.keyShift.isDown(), options.keyJump.isDown());
        float forward = axisValue(options.keyDown.isDown(), options.keyUp.isDown());
        boolean jumping = options.keyJump.isDown();
        boolean sprinting = options.keySprint.isDown();
        boolean sneaking = options.keyShift.isDown();
        boolean attacking = options.keyAttack.isDown();
        boolean using = options.keyUse.isDown();
        capturedInput = new ControlledInput(
                strafe,
                vertical,
                forward,
                jumping,
                sprinting,
                sneaking,
                attacking,
                using,
                player.getYRot(),
                player.getXRot()
        );
    }

    public static void applyCapturedInput(LocalPlayer player) {
        ControlledInput input = capturedInput;
        player.setJumping(input.jumping());
        player.setSprinting(input.sprinting());
        player.setShiftKeyDown(input.sneaking());
        player.setYRot(input.yaw());
        player.setXRot(input.pitch());
    }

    public static void applyCapturedKeyStates(Minecraft client) {
        ControlledInput input = capturedInput;
        client.options.keyAttack.setDown(false);
        client.options.keyUse.setDown(false);
        client.options.keyJump.setDown(input.jumping());
        client.options.keySprint.setDown(input.sprinting());
        client.options.keyShift.setDown(input.sneaking());
    }

    public static void resetCapturedInput() {
        capturedInput = ControlledInput.idle();
    }

    public static void resetAll() {
        inputLocked = false;
        captureEnabled = false;
        clearCameraTarget();
        resetCapturedInput();
    }

    public static void syncCamera(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            return;
        }

        if (cameraTargetEntityId == -1) {
            if (client.getCameraEntity() != player) {
                client.setCameraEntity(player);
            }
            return;
        }

        if (client.level == null) {
            return;
        }

        if (client.getCameraEntity() != null && client.getCameraEntity().getId() == cameraTargetEntityId) {
            return;
        }

        var target = client.level.getEntity(cameraTargetEntityId);
        if (target != null) {
            client.setCameraEntity(target);
        }
    }

    private static float axisValue(boolean negative, boolean positive) {
        if (negative == positive) {
            return 0.0F;
        }
        return positive ? 1.0F : -1.0F;
    }

    public record ControlledInput(
            float strafe,
            float vertical,
            float forward,
            boolean jumping,
            boolean sprinting,
            boolean sneaking,
            boolean attacking,
            boolean using,
            float yaw,
            float pitch
    ) {
        public ControlledInput {
            strafe = clamp(strafe);
            vertical = clamp(vertical);
            forward = clamp(forward);
        }

        public static ControlledInput idle() {
            return new ControlledInput(0.0F, 0.0F, 0.0F, false, false, false, false, false, 0.0F, 0.0F);
        }

        private static float clamp(float value) {
            return Math.max(-1.0F, Math.min(1.0F, value));
        }

        public Input toPlayerInput() {
            return new Input(
                    forward > 0.0F,
                    forward < 0.0F,
                    strafe < 0.0F,
                    strafe > 0.0F,
                    jumping,
                    sneaking,
                    sprinting
            );
        }

        public Vec2 toMoveVector() {
            return new Vec2(strafe, forward).normalized();
        }
    }
}
