package org.example.makismod.makissmpmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.effect.ServerMobEffectEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.entity.player.Player;
import org.example.makismod.makissmpmod.commands.MindcontrolCommand;
import org.example.makismod.makissmpmod.commands.NickCommand;
import org.example.makismod.makissmpmod.commands.TpaCommand;
import org.example.makismod.makissmpmod.items.FeatherItem;
import org.example.makismod.makissmpmod.items.IcarusWingsItem;
import org.example.makismod.makissmpmod.items.MagnetItem;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Makissmpmod implements ModInitializer {
    public static final String MOD_ID = "makissmpmod";
    public static final Set<UUID> ModlistPlayers = new HashSet<>();
    public static final Set<UUID> lockoutPlayers = new HashSet<>();
    public static final Logger LOGGER = LoggerFactory.getLogger("Makissmpmod");
    @Override
    public void onInitialize() {
        ModEffects.initialize();
        ModItems.initialize();
        ModPotions.initialize();
        ModEntityTypes.init();
        ModEntityTypes.registerAttributes();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            TpaCommand.register(dispatcher);
            NickCommand.register(dispatcher);
            MindcontrolCommand.register(dispatcher);
        });
        ServerMessageEvents.ALLOW_GAME_MESSAGE.register(((minecraftServer, component, b) -> {
            String msg = component.getString();
            if (msg.contains("joined the game") || msg.contains("left the game")) {
                return false;
            }
            return true;
        }));
        ServerMobEffectEvents.AFTER_REMOVE.register(((effectInstance, entity, ctx) -> {
            if (entity instanceof Player player && effectInstance.getEffect() == ModEffects.FLIGHT) {
                player.getAbilities().flying = false;
                player.getAbilities().mayfly = false;
                player.onUpdateAbilities();
            }
        }));
        ServerPlayConnectionEvents.JOIN.register(new ConnectionMessages());
        ServerPlayConnectionEvents.DISCONNECT.register(new ConnectionMessages());
        ServerTickEvents.END_SERVER_TICK.register(CustomShieldItem::tickActiveDashes);
        ServerTickEvents.END_SERVER_TICK.register(FeatherItem::tickFeatherFlight);
        ServerTickEvents.END_SERVER_TICK.register(IcarusWingsItem::tickFlightDrain);
        ServerTickEvents.END_SERVER_TICK.register(ModEffects::tickWaxCoatedPlayers);
        ServerTickEvents.END_SERVER_TICK.register(MagnetItem::tickMagnetItem);
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer controller : server.getPlayerList().getPlayers()) {
                UUID targetId = MindControlManager.getTargetId(controller);
                if (targetId == null) {
                    continue;
                }

                ServerPlayer target = server.getPlayerList().getPlayer(targetId);
                if (target == null) {
                    MindControlManager.releaseController(controller);
                    continue;
                }

                MindControlManager.syncControllerToTarget(controller, target);
            }
        });
        PayloadTypeRegistry.playC2S().register(ModListPayload.MyPayLoad.ID, ModListPayload.MyPayLoad.CODEC);
        PayloadTypeRegistry.playC2S().register(ShieldBlockAttackPayload.ID, ShieldBlockAttackPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(MindControlInputPayload.ControllerInputC2SPayload.ID, MindControlInputPayload.ControllerInputC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(LockoutPayload.lockoutoutpayload.ID, LockoutPayload.lockoutoutpayload.CODEC);
        PayloadTypeRegistry.playS2C().register(MindControlCapturePayload.CaptureStateS2CPayload.ID, MindControlCapturePayload.CaptureStateS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(MindControlInputPayload.TargetInputS2CPayload.ID, MindControlInputPayload.TargetInputS2CPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ModListPayload.MyPayLoad.ID, ((myPayLoad, context) -> {
            context.server().execute(() -> {
                List<String> received = myPayLoad.message();
                ServerPlayer player = context.player();
                String adminL = "Makis1445";
                ServerPlayer adminPlayer = context.server().getPlayerList().getPlayer(adminL);
                Makissmpmod.ModlistPlayers.remove(player.getUUID());
                List<String> badMods = List.of("freecam", "meteor-client", "replaymod", "xaerominimap");
                    if (!Collections.disjoint(received, badMods)) {
                        player.connection.disconnect(Component.literal("You are a bad boy for installing unallowed mods"));
                    }
                List<String> boringMods = List.of("fabric", "org_", "com_");
                Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                    if (adminPlayer != null) {
                        for (String s : received) {
                            boolean isBoring = boringMods.stream().anyMatch(s::startsWith);
                            if (!isBoring) {
                                adminPlayer.sendSystemMessage(Component.literal(s));
                            }
                        }
                    }
                }, 1, TimeUnit.SECONDS);
            });
        }));
        ServerPlayNetworking.registerGlobalReceiver(ShieldBlockAttackPayload.ID, ((payload, context) -> {
            context.server().execute(() -> {
                if (!payload.trigger()) {
                    return;
                }

                CustomShieldItem.tryStartDash(context.player());
            });
        }));
        ServerPlayNetworking.registerGlobalReceiver(MindControlInputPayload.ControllerInputC2SPayload.ID, ((payload, context) -> {
            context.server().execute(() -> {
                UUID targetId = MindControlManager.getTargetId(context.player());
                if (targetId == null) {
                    return;
                }

                ServerPlayer target = context.server().getPlayerList().getPlayer(targetId);
                if (target == null) {
                    return;
                }

                ServerPlayNetworking.send(target, new MindControlInputPayload.TargetInputS2CPayload(
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
                ));
            });
        }));
    }
}
