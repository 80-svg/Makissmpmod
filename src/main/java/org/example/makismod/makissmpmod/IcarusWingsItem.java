package org.example.makismod.makissmpmod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class IcarusWingsItem extends Item {
    private static final float PERCENT_DRAIN_PER_SECOND = 0.01F;
    private static final int BREAK_DELAY_TICKS = 20 * 3;
    private static final int WARNING_SOUND_INTERVAL_TICKS = 10;
    private static final int WAX_COATED_DURATION_TICKS = 20 * 60;
    private static final int DISORIENTATION_DURATION_TICKS = 20 * 5;
    private static final int LIGHTNING_STRIKE_MIN_Y = 150;
    private static final int ALTITUDE_MELT_START_Y = 120;
    private static final int ALTITUDE_STEP_BLOCKS = 10;
    private static final float ALTITUDE_STEP_MULTIPLIER = 0.5F;
    private static final float DIRECT_SUNLIGHT_MULTIPLIER = 2.0F;
    private static final float HOT_BIOME_TEMPERATURE = 1.0F;
    private static final float HOT_BIOME_MULTIPLIER = 1.5F;
    private static final long DAY_END_TICK = 12300L;
    private static final Map<UUID, Integer> PENDING_BREAK_TICKS = new ConcurrentHashMap<>();

    public IcarusWingsItem(Properties properties) {
        super(properties);
    }

    public static void tickFlightDrain(MinecraftServer server) {
        tickPendingBreaks(server);

        if (server.getTickCount() % 20 == 0) {
            tickMeltDrain(server);
        }
    }

    private static void tickMeltDrain(MinecraftServer server) {
        List<ServerPlayer> playerList = server.getPlayerList().getPlayers();
        if (playerList.isEmpty()) return;
        for (ServerPlayer player : playerList) {
            ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
            if (!player.isFallFlying() || chestStack.getItem() != ModItems.ICARUS_WINGS || PENDING_BREAK_TICKS.containsKey(player.getUUID())) {
                continue;
            }

            float currentPercent = chestStack.getOrDefault(ModItems.ICARUS_PERCENT, 1.0F);
            if (currentPercent <= 0.0F) {
                continue;
            }

            float updatedPercent = Math.max(0.0F, currentPercent - (PERCENT_DRAIN_PER_SECOND * getMeltMultiplier(player)));
            if (updatedPercent <= 0.0F) {
                chestStack.set(ModItems.ICARUS_PERCENT, 0.0F);
                PENDING_BREAK_TICKS.put(player.getUUID(), BREAK_DELAY_TICKS);
                playBreakWarning(player);
                continue;
            }

            chestStack.set(ModItems.ICARUS_PERCENT, updatedPercent);
        }
    }

    private static float getMeltMultiplier(ServerPlayer player) {
        float meltMultiplier = 1.0F;
        BlockPos blockPos = player.blockPosition();

        if (isInDirectSunlight(player, blockPos)) {
            meltMultiplier *= DIRECT_SUNLIGHT_MULTIPLIER;
        }

        if (blockPos.getY() > ALTITUDE_MELT_START_Y) {
            int altitudeSteps = (blockPos.getY() - ALTITUDE_MELT_START_Y) / ALTITUDE_STEP_BLOCKS;
            meltMultiplier += altitudeSteps * ALTITUDE_STEP_MULTIPLIER;
        }

        if (player.level().getBiome(blockPos).value().getBaseTemperature() >= HOT_BIOME_TEMPERATURE) {
            meltMultiplier *= HOT_BIOME_MULTIPLIER;
        }

        return meltMultiplier;
    }

    private static boolean isInDirectSunlight(ServerPlayer player, BlockPos blockPos) {
        long timeOfDay = player.level().getDayTime() % 24000L;
        return timeOfDay < DAY_END_TICK && player.level().canSeeSky(blockPos);
    }

    private static void tickPendingBreaks(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID playerId = player.getUUID();
            Integer ticksRemaining = PENDING_BREAK_TICKS.get(playerId);
            if (ticksRemaining == null) {
                continue;
            }

            ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
            if (chestStack.getItem() != ModItems.ICARUS_WINGS) {
                PENDING_BREAK_TICKS.remove(playerId);
                continue;
            }

            int updatedTicks = ticksRemaining - 1;
            if (updatedTicks <= 0) {
                PENDING_BREAK_TICKS.remove(playerId);
                chestStack.hurtAndBreak(chestStack.getMaxDamage(), player, EquipmentSlot.CHEST);
                applyWingBreakConsequences(player);
                continue;
            }

            PENDING_BREAK_TICKS.put(playerId, updatedTicks);
            if (updatedTicks % WARNING_SOUND_INTERVAL_TICKS == 0) {
                playBreakWarning(player);
            }
        }
    }

    private static void playBreakWarning(ServerPlayer player) {
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket(player));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BREAK,
                SoundSource.PLAYERS, 0.9F, 0.7F);
    }

    private static void applyWingBreakConsequences(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(ModEffects.WAX_COATED, WAX_COATED_DURATION_TICKS));
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, DISORIENTATION_DURATION_TICKS));
        player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, DISORIENTATION_DURATION_TICKS));

        if (player.getBlockY() <= LIGHTNING_STRIKE_MIN_Y) {
            return;
        }

        ServerLevel level = (ServerLevel) player.level();
        LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(level, entity -> entity.setPos(player.getX(), player.getY(), player.getZ()),
                player.blockPosition(), EntitySpawnReason.TRIGGERED, true, false);
        if (lightningBolt != null) {
            lightningBolt.setCause(player);
            level.addFreshEntity(lightningBolt);
        }
    }
}
