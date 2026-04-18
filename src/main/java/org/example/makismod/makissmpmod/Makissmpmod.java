package org.example.makismod.makissmpmod;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.effect.ServerMobEffectEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import org.example.makismod.makissmpmod.commands.MindcontrolCommand;
import org.example.makismod.makissmpmod.commands.NickCommand;
import org.example.makismod.makissmpmod.commands.TpaCommand;
import org.example.makismod.makissmpmod.items.CoinDeflectionHandler;
import org.example.makismod.makissmpmod.items.FeatherItem;
import org.example.makismod.makissmpmod.items.IcarusWingsItem;
import org.example.makismod.makissmpmod.items.MagnetItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Makissmpmod implements ModInitializer {
    public static final String MOD_ID = "makissmpmod";
    public static final Set<UUID> ModlistPlayers = new HashSet<>();
    public static final Set<UUID> lockoutPlayers = new HashSet<>();
    public static final Logger LOGGER = LoggerFactory.getLogger("Makissmpmod");

    public static String INTEGRITY_SECRET = "";
    public static String MOD_VERSION = "1.0.0";
    public static int VERIFICATION_TIMEOUT = 10;
    public static final ConcurrentHashMap<UUID, String> pendingChallenges = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<UUID, Long> verificationTimestamps = new ConcurrentHashMap<>();

    @Override
    public void onInitialize() {
        loadIntegrityConfig();

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
        ServerMessageEvents.ALLOW_GAME_MESSAGE.register((minecraftServer, component, b) -> {
            String msg = component.getString();
            return !msg.contains("joined the game") && !msg.contains("left the game");
        });
        ServerMobEffectEvents.AFTER_REMOVE.register((effectInstance, entity, ctx) -> {
            if (entity instanceof Player player && effectInstance.getEffect() == ModEffects.FLIGHT) {
                player.getAbilities().flying = false;
                player.getAbilities().mayfly = false;
                player.onUpdateAbilities();
            }
        });
        AttackEntityCallback.EVENT.register(CoinDeflectionHandler::onAttackEntity);
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
                if (targetId == null) continue;

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
        PayloadTypeRegistry.playC2S().register(IntegrityPayloads.IntegrityResponseC2S.ID, IntegrityPayloads.IntegrityResponseC2S.CODEC);
        PayloadTypeRegistry.playS2C().register(LockoutPayload.lockoutoutpayload.ID, LockoutPayload.lockoutoutpayload.CODEC);
        PayloadTypeRegistry.playS2C().register(MindControlCapturePayload.CaptureStateS2CPayload.ID, MindControlCapturePayload.CaptureStateS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(MindControlInputPayload.TargetInputS2CPayload.ID, MindControlInputPayload.TargetInputS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(IntegrityPayloads.IntegrityChallengeS2C.ID, IntegrityPayloads.IntegrityChallengeS2C.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ModListPayload.MyPayLoad.ID, (myPayLoad, context) -> {
            context.server().execute(() -> {
                List<String> received = myPayLoad.message();
                ServerPlayer player = context.player();
                ServerPlayer adminPlayer = context.server().getPlayerList().getPlayer("Makis1445");
                Makissmpmod.ModlistPlayers.remove(player.getUUID());
                List<String> badMods = List.of("freecam", "meteor-client", "replaymod", "xaerominimap");
                if (!java.util.Collections.disjoint(received, badMods)) {
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
        });

        ServerPlayNetworking.registerGlobalReceiver(ShieldBlockAttackPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                if (payload.trigger()) {
                    CustomShieldItem.tryStartDash(context.player());
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(MindControlInputPayload.ControllerInputC2SPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                UUID targetId = MindControlManager.getTargetId(context.player());
                if (targetId == null) return;

                ServerPlayer target = context.server().getPlayerList().getPlayer(targetId);
                if (target == null) return;

                ServerPlayNetworking.send(target, new MindControlInputPayload.TargetInputS2CPayload(
                        payload.strafe(), payload.vertical(), payload.forward(),
                        payload.jumping(), payload.sprinting(), payload.sneaking(),
                        payload.attacking(), payload.using(), payload.yaw(), payload.pitch()
                ));
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(IntegrityPayloads.IntegrityResponseC2S.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                UUID playerId = player.getUUID();
                String expectedChallenge = pendingChallenges.get(playerId);

                if (expectedChallenge == null) {
                    player.connection.disconnect(Component.literal("Integrity verification expired - please rejoin"));
                    return;
                }
                if (!payload.challenge().equals(expectedChallenge)) {
                    player.connection.disconnect(Component.literal("Integrity verification failed - invalid challenge"));
                    return;
                }

                String computedHmac = computeHmac(payload.challenge(), INTEGRITY_SECRET);
                if (!payload.hmacResponse().equals(computedHmac)) {
                    LOGGER.warn("INTEGRITY: Failed verification for player {} - invalid HMAC", player.getName().getString());
                    player.connection.disconnect(Component.literal("Integrity verification failed - mod may be tampered"));
                    return;
                }

                pendingChallenges.remove(playerId);
                verificationTimestamps.remove(playerId);
                LOGGER.info("INTEGRITY: Player {} verified successfully", player.getName().getString());
            });
        });
    }

    public static void sendIntegrityChallenge(ServerPlayer player) {
        String challenge = UUID.randomUUID().toString();
        pendingChallenges.put(player.getUUID(), challenge);
        verificationTimestamps.put(player.getUUID(), System.currentTimeMillis());
        ServerPlayNetworking.send(player, new IntegrityPayloads.IntegrityChallengeS2C(challenge));
    }

    public static String computeHmac(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec sk = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            mac.init(sk);
            byte[] hmacBytes = mac.doFinal(data.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hmacBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            LOGGER.error("INTEGRITY: Failed to compute HMAC", e);
            return "error";
        }
    }

    private void loadIntegrityConfig() {
        LOGGER.info("INTEGRITY: Loading config from classpath...");

        try (InputStream configIs = Makissmpmod.class.getResourceAsStream("/integrity_config.json")) {
            if (configIs != null) {
                Gson gson = new Gson();
                JsonObject config = gson.fromJson(new String(configIs.readAllBytes()), JsonObject.class);
                if (config.has("version")) {
                    MOD_VERSION = config.get("version").getAsString();
                }
                if (config.has("verificationTimeout")) {
                    VERIFICATION_TIMEOUT = config.get("verificationTimeout").getAsInt();
                }
                LOGGER.info("INTEGRITY: Loaded config version={}", MOD_VERSION);
            }
        } catch (Exception e) {
            LOGGER.error("INTEGRITY: Failed to load config", e);
        }

        try (InputStream secretIs = Makissmpmod.class.getResourceAsStream("/integrity_secret.txt")) {
            if (secretIs != null) {
                INTEGRITY_SECRET = new String(secretIs.readAllBytes()).trim();
                LOGGER.info("INTEGRITY: Loaded secret ({} chars)", INTEGRITY_SECRET.length());
            } else {
                LOGGER.error("INTEGRITY: integrity_secret.txt not found in JAR!");
            }
        } catch (Exception e) {
            LOGGER.error("INTEGRITY: Failed to load secret", e);
        }
    }
}