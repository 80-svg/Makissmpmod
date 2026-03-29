package org.example.makismod.makissmpmod;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.phys.Vec3;
import org.example.makismod.makissmpmod.effects.FlightEffect;
import org.example.makismod.makissmpmod.effects.LethargicEffect;
import org.example.makismod.makissmpmod.effects.WaxCoatedEffect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ModEffects {
    private static final double WAX_COATED_SINK_ACCELERATION = 0.16D;
    private static final double WAX_COATED_MAX_SINK_SPEED = -1.2D;
    private static final double HORIZONTAL_DRAG = 0.85D;
    private static final int LETHARGY_STILL_TICKS = 60;
    private static final double LETHARGY_MOVEMENT_EPSILON_SQUARED = 1.0E-4D;
    private static final Set<UUID> FORCED_CROUCH_PLAYERS = new HashSet<>();
    private static final Map<UUID, Integer> LETHARGY_STILLNESS = new HashMap<>();

    public static final Holder<MobEffect> WAX_COATED = Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            Identifier.fromNamespaceAndPath(Makissmpmod.MOD_ID, "wax_coated"),
            new WaxCoatedEffect());
    public static final Holder<MobEffect> LETHARGY = Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            Identifier.fromNamespaceAndPath(Makissmpmod.MOD_ID, "lethargy"),
            new LethargicEffect());
    public static final Holder<MobEffect> DROWSYNESS = Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            Identifier.fromNamespaceAndPath(Makissmpmod.MOD_ID, "drowsyness"),
            new DrowsynessEffect());
    public static final Holder<MobEffect> FLIGHT = Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            Identifier.fromNamespaceAndPath(Makissmpmod.MOD_ID, "flight"),
            new FlightEffect());
    private ModEffects() {
    }

    public static void initialize() {
    }

    public static void tickWaxCoatedPlayers(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.hasEffect(WAX_COATED) && player.isInWater()) {
                Vec3 velocity = player.getDeltaMovement();
                double downwardVelocity = Math.min(velocity.y - WAX_COATED_SINK_ACCELERATION, WAX_COATED_MAX_SINK_SPEED);
                player.setSwimming(false);
                player.setDeltaMovement(velocity.x * HORIZONTAL_DRAG, downwardVelocity, velocity.z * HORIZONTAL_DRAG);
                player.hurtMarked = true;
            }

            tickLethargicPlayer(player);
        }
    }

    private static void tickLethargicPlayer(ServerPlayer player) {
        UUID playerId = player.getUUID();
        boolean hasLethargy = player.hasEffect(LETHARGY);

        if (!hasLethargy) {
            LETHARGY_STILLNESS.remove(playerId);
            if (FORCED_CROUCH_PLAYERS.remove(playerId)) {
                player.setShiftKeyDown(false);
            }
            return;
        }

        Vec3 movementSinceLastTick = new Vec3(
                player.getX() - player.xOld,
                player.getY() - player.yOld,
                player.getZ() - player.zOld
        );
        boolean isStandingStill = movementSinceLastTick.lengthSqr() <= LETHARGY_MOVEMENT_EPSILON_SQUARED;

        if (!isStandingStill) {
            LETHARGY_STILLNESS.remove(playerId);
            if (FORCED_CROUCH_PLAYERS.remove(playerId)) {
                player.setShiftKeyDown(false);
            }
            return;
        }

        int stillTicks = LETHARGY_STILLNESS.merge(playerId, 1, Integer::sum);
        if (stillTicks < LETHARGY_STILL_TICKS) {
            if (FORCED_CROUCH_PLAYERS.contains(playerId)) {
                player.setShiftKeyDown(true);
            }
            return;
        }

        player.setShiftKeyDown(true);
        FORCED_CROUCH_PLAYERS.add(playerId);
    }

    private static final class DrowsynessEffect extends MobEffect {
        private DrowsynessEffect() {
            super(MobEffectCategory.HARMFUL, 0x4A5162);
        }
    }
}
