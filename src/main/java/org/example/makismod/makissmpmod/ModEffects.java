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

public final class ModEffects {
    private static final double WAX_COATED_SINK_ACCELERATION = 0.16D;
    private static final double WAX_COATED_MAX_SINK_SPEED = -1.2D;
    private static final double HORIZONTAL_DRAG = 0.85D;

    public static final Holder<MobEffect> WAX_COATED = Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            Identifier.fromNamespaceAndPath(Makissmpmod.MOD_ID, "wax_coated"),
            new WaxCoatedEffect());

    private ModEffects() {
    }

    public static void initialize() {
    }

    public static void tickWaxCoatedPlayers(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!player.hasEffect(WAX_COATED) || !player.isInWater()) {
                continue;
            }

            Vec3 velocity = player.getDeltaMovement();
            double downwardVelocity = Math.min(velocity.y - WAX_COATED_SINK_ACCELERATION, WAX_COATED_MAX_SINK_SPEED);
            player.setSwimming(false);
            player.setDeltaMovement(velocity.x * HORIZONTAL_DRAG, downwardVelocity, velocity.z * HORIZONTAL_DRAG);
            player.hurtMarked = true;
        }
    }

    private static final class WaxCoatedEffect extends MobEffect {
        private WaxCoatedEffect() {
            super(MobEffectCategory.HARMFUL, 0xE5C15A);
        }
    }
}
