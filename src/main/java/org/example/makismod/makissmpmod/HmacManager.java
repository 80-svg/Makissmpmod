package org.example.makismod.makissmpmod;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.util.UUID;

public class HmacManager {
    public static void sendIntegrityChallenge(ServerPlayer player) {
        String challenge = UUID.randomUUID().toString();
        Makissmpmod.pendingChallenges.put(player.getUUID(), challenge);
        Makissmpmod.verificationTimestamps.put(player.getUUID(), System.currentTimeMillis());
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
            Makissmpmod.LOGGER.error("INTEGRITY: Failed to compute HMAC", e);
            return "error";
        }
    }

    static void loadIntegrityConfig() {
        Makissmpmod.LOGGER.info("INTEGRITY: Loading config from classpath...");

        try (InputStream configIs = Makissmpmod.class.getResourceAsStream("/integrity_config.json")) {
            if (configIs != null) {
                Gson gson = new Gson();
                JsonObject config = gson.fromJson(new String(configIs.readAllBytes()), JsonObject.class);
                if (config.has("version")) {
                    Makissmpmod.MOD_VERSION = config.get("version").getAsString();
                }
                if (config.has("verificationTimeout")) {
                    Makissmpmod.VERIFICATION_TIMEOUT = config.get("verificationTimeout").getAsInt();
                }
                Makissmpmod.LOGGER.info("INTEGRITY: Loaded config version={}", Makissmpmod.MOD_VERSION);
            }
        } catch (Exception e) {
            Makissmpmod.LOGGER.error("INTEGRITY: Failed to load config", e);
        }

        try (InputStream secretIs = Makissmpmod.class.getResourceAsStream("/integrity_secret.txt")) {
            if (secretIs != null) {
                Makissmpmod.INTEGRITY_SECRET = new String(secretIs.readAllBytes()).trim();
                Makissmpmod.LOGGER.info("INTEGRITY: Loaded secret ({} chars)", Makissmpmod.INTEGRITY_SECRET.length());
            } else {
                Makissmpmod.LOGGER.error("INTEGRITY: integrity_secret.txt not found in JAR!");
            }
        } catch (Exception e) {
            Makissmpmod.LOGGER.error("INTEGRITY: Failed to load secret", e);
        }
    }
}
