package org.example.makismod.makissmpmod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class IcarusWingsItem extends Item {
    private static final float PERCENT_DRAIN_PER_SECOND = 0.01F;
    private static final int ALTITUDE_MELT_START_Y = 120;
    private static final int ALTITUDE_STEP_BLOCKS = 10;
    private static final float ALTITUDE_STEP_MULTIPLIER = 0.5F;
    private static final float DIRECT_SUNLIGHT_MULTIPLIER = 2.0F;
    private static final float HOT_BIOME_TEMPERATURE = 1.0F;
    private static final float HOT_BIOME_MULTIPLIER = 1.5F;
    private static final long DAY_END_TICK = 12300L;

    public IcarusWingsItem(Properties properties) {
        super(properties);
    }

    public static void tickFlightDrain(MinecraftServer server) {
        if (server.getTickCount() % 20 != 0) {
            return;
        }
        List<ServerPlayer> playerList = server.getPlayerList().getPlayers();
        if (playerList.isEmpty()) return;
        for (ServerPlayer player : playerList) {
            ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
            if (!player.isFallFlying() || chestStack.getItem() != ModItems.ICARUS_WINGS) {
                continue;
            }

            float currentPercent = chestStack.getOrDefault(ModItems.ICARUS_PERCENT, 1.0F);
            if (currentPercent <= 0.0F) {
                continue;
            }

            float updatedPercent = Math.max(0.0F, currentPercent - (PERCENT_DRAIN_PER_SECOND * getMeltMultiplier(player)));
            if (updatedPercent <= 0.0F) {
                chestStack.hurtAndBreak(chestStack.getMaxDamage(), player, EquipmentSlot.CHEST);
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
}
