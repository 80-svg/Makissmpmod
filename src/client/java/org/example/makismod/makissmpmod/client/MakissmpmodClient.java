package org.example.makismod.makissmpmod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.example.makismod.makissmpmod.*;

import java.util.List;

public class MakissmpmodClient implements ClientModInitializer {
    private static boolean sentBlockAttackThisPress;
    private static boolean replayingAttack;
    private static boolean replayingUse;

    @Override
    public void onInitializeClient() {
        LockoutClientState.resetAll();
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            List<String> modIds = FabricLoader.getInstance()
                            .getAllMods()
                                    .stream()
                                            .map(modContainer -> modContainer.getMetadata().getId())
                                                    .toList();
            sender.sendPacket(new ModListPayload.MyPayLoad(modIds));
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> LockoutClientState.resetAll());
        ClientPlayNetworking.registerGlobalReceiver(LockoutPayload.lockoutoutpayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                LockoutClientState.setInputLocked(payload.playerLockedInput());
                if (!payload.playerLockedInput()) {
                    LockoutClientState.resetCapturedInput();
                    LockoutClientState.applyCapturedKeyStates(context.client());
                }
                String actorName = payload.actorPlayerName().isBlank() ? "Unknown player" : payload.actorPlayerName();
                String overlayText = payload.playerLockedInput()
                        ? actorName + " locked your input."
                        : actorName + " unlocked your input.";
                if (context.client().player != null) {
                    context.client().gui.setOverlayMessage(Component.literal(overlayText), false);
                    ClientLevel level = context.client().level;
                    if (level != null) {
                        Entity target = level.getEntity((int) payload.entityID());
                    }
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(MindControlCapturePayload.CaptureStateS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (payload.capturing()) {
                    LockoutClientState.startCapturingInput();
                    LockoutClientState.setCameraTargetEntityId(payload.targetEntityId());
                } else {
                    LockoutClientState.stopCapturingInput();
                    LockoutClientState.clearCameraTarget();
                    LockoutClientState.resetCapturedInput();
                    LockoutClientState.syncCamera(context.client());
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(MindControlInputPayload.TargetInputS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> LockoutClientState.setCapturedInput(new LockoutClientState.ControlledInput(
                    payload.strafe(),
                    payload.vertical(),
                    payload.forward(),
                    payload.jumping(),
                    payload.sprinting(),
                    payload.sneaking(),
                    payload.attacking(),
                    payload.using(),
                    payload.yaw(),
                    payload.pitch()
            )));
        });
//        EntityModelLayerRegistry.registerModelLayer(EpsteinModelLayers.EPSTEIN, EpsteinEntityModel::createBodyLayer);
//        EntityRenderers.register(ModEntityTypes.EPSTEIN_ENTITY, EpsteinEntityRenderer::new);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                sentBlockAttackThisPress = false;
                LockoutClientState.resetAll();
                EffectVisualState.tick(client);
                return;
            }

            if (LockoutClientState.isCaptureEnabled()) {
                LockoutClientState.captureCurrentInput(client);
                LockoutClientState.ControlledInput input = LockoutClientState.getCapturedInput();
                ClientPlayNetworking.send(new MindControlInputPayload.ControllerInputC2SPayload(
                        input.strafe(),
                        input.vertical(),
                        input.forward(),
                        input.jumping(),
                        input.sprinting(),
                        input.sneaking(),
                        input.attacking(),
                        input.using(),
                        input.yaw(),
                        input.pitch()
                ));
            }
            LockoutClientState.syncCamera(client);
            if (LockoutClientState.isInputLocked()) {
                LockoutClientState.applyCapturedKeyStates(client);
                client.gameRenderer.pick(1.0F);
                replayControlledClicks(client);
            } else {
                replayingAttack = false;
                replayingUse = false;
            }

            boolean shouldTrigger = CustomShieldItem.isBlockingComboReady(client.player)
                    && client.options.keyAttack.isDown();

            if (shouldTrigger && !sentBlockAttackThisPress) {
                ClientPlayNetworking.send(new ShieldBlockAttackPayload(true));
            }

            sentBlockAttackThisPress = shouldTrigger;
            EffectVisualState.tick(client);
        });
    }

    private static void replayControlledClicks(Minecraft client) {
        LockoutClientState.ControlledInput input = LockoutClientState.getCapturedInput();
        if (client.player == null || client.gameMode == null) {
            replayingAttack = false;
            replayingUse = false;
            return;
        }

        HitResult hitResult = client.hitResult;
        if (input.attacking()) {
            if (hitResult instanceof EntityHitResult entityHitResult) {
                if (!replayingAttack && entityHitResult.getEntity() != client.player) {
                    client.gameMode.attack(client.player, entityHitResult.getEntity());
                    client.player.swing(InteractionHand.MAIN_HAND);
                }
                client.gameMode.stopDestroyBlock();
            } else if (hitResult instanceof BlockHitResult blockHitResult) {
                Direction direction = blockHitResult.getDirection();
                if (!replayingAttack) {
                    client.gameMode.startDestroyBlock(blockHitResult.getBlockPos(), direction);
                } else {
                    client.gameMode.continueDestroyBlock(blockHitResult.getBlockPos(), direction);
                }
                client.player.swing(InteractionHand.MAIN_HAND);
            } else if (replayingAttack) {
                client.gameMode.stopDestroyBlock();
            }
        } else if (replayingAttack) {
            client.gameMode.stopDestroyBlock();
        }

        if (input.using() && !replayingUse) {
            if (hitResult instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() != client.player) {
                client.gameMode.interactAt(client.player, entityHitResult.getEntity(), entityHitResult, InteractionHand.MAIN_HAND);
                client.gameMode.interact(client.player, entityHitResult.getEntity(), InteractionHand.MAIN_HAND);
            } else if (hitResult instanceof BlockHitResult blockHitResult) {
                client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, blockHitResult);
            } else {
                client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
            }
        }

        replayingAttack = input.attacking();
        replayingUse = input.using();
    }
}
