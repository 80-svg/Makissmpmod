package org.example.makismod.makissmpmod.items;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class CigarItem extends Item {
    private static final int USE_DURATION = 60;
    private static final int COOLDOWN_TICKS = 100;

    public CigarItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);

            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                Vec3 lookVec = player.getLookAngle();
                Vec3 headPos = player.getEyePosition(1.0F);

                Vec3 smokePos = headPos.add(lookVec.scale(0.8));

                for (int i = 0; i < 8; i++) {
                    double offsetX = (level.random.nextDouble() - 0.5) * 0.3;
                    double offsetY = (level.random.nextDouble() - 0.5) * 0.3;
                    double offsetZ = (level.random.nextDouble() - 0.5) * 0.3;

                    serverLevel.sendParticles(
                            ParticleTypes.SMOKE,
                            smokePos.x + offsetX,
                            smokePos.y + offsetY,
                            smokePos.z + offsetZ,
                            1,
                            0.02, 0.05, 0.02,
                            0.01
                    );
                }

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.3F, 1.5F);
            }

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }
}
