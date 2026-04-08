package org.example.makismod.makissmpmod.items;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class MagnetItem extends Item {
    private static final String ENABLED_TAG = "magnet_enabled";
    private static final double MAGNET_RANGE = 8.0;
    private static final double PULL_SPEED = 0.15;
    private static final double MAX_SPEED = 0.8;

    public MagnetItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            boolean enabled = toggleMagnet(stack);

            if (enabled) {
                player.displayClientMessage(Component.literal("Magnet enabled"), true);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE.value(), SoundSource.PLAYERS, 0.5F, 1.2F);
            } else {
                player.displayClientMessage(Component.literal("Magnet disabled"), true);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE.value(), SoundSource.PLAYERS, 0.5F, 0.8F);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isMagnetEnabled(stack);
    }

    private static boolean isMagnetEnabled(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            return customData.copyTag().getBoolean(ENABLED_TAG).orElse(false);
        }
        return false;
    }

    private static boolean toggleMagnet(ItemStack stack) {
        boolean current = isMagnetEnabled(stack);
        boolean newState = !current;

        CustomData.update(DataComponents.CUSTOM_DATA, stack, compoundTag -> {
            compoundTag.putBoolean(ENABLED_TAG, newState);
        });

        return newState;
    }

    public static void tickMagnetItem(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();

            ItemStack magnetStack = null;
            EquipmentSlot slot = null;

            if (!mainHand.isEmpty() && mainHand.getItem() instanceof MagnetItem && isMagnetEnabled(mainHand)) {
                magnetStack = mainHand;
                slot = EquipmentSlot.MAINHAND;
            } else if (!offHand.isEmpty() && offHand.getItem() instanceof MagnetItem && isMagnetEnabled(offHand)) {
                magnetStack = offHand;
                slot = EquipmentSlot.OFFHAND;
            }

            if (magnetStack == null) {
                continue;
            }

            pullItemsTowardPlayer(player, server.overworld());

            if (player.tickCount % 20 == 0) {
                magnetStack.hurtAndBreak(1, player, slot);
            }
        }
    }

    private static void pullItemsTowardPlayer(ServerPlayer player, ServerLevel level) {
        AABB magnetArea = player.getBoundingBox().inflate(MAGNET_RANGE);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, magnetArea);

        for (ItemEntity item : items) {
            if (item.isRemoved()) {
                continue;
            }

            Vec3 itemPos = item.position();
            Vec3 playerPos = player.position().add(0, player.getEyeHeight() * 0.5, 0);
            Vec3 direction = playerPos.subtract(itemPos).normalize();

            double distance = itemPos.distanceTo(playerPos);

            if (distance < 1.5) {
                continue;
            }

            Vec3 currentMotion = item.getDeltaMovement();

            double pullStrength = PULL_SPEED * (1.0 - (distance / MAGNET_RANGE));
            if (pullStrength < 0.02) pullStrength = 0.02;

            Vec3 newMotion = currentMotion.add(
                direction.x * pullStrength,
                direction.y * pullStrength + 0.08,
                direction.z * pullStrength
            );

            if (newMotion.length() > MAX_SPEED) {
                newMotion = newMotion.normalize().scale(MAX_SPEED);
            }

            item.setDeltaMovement(newMotion);
            item.setNoGravity(true);
            item.hurtMarked = true;

            if (item.tickCount % 5 == 0) {
                item.setNoGravity(false);
            }
        }
    }
}
