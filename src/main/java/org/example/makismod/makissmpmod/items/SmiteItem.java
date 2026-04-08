package org.example.makismod.makissmpmod.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.example.makismod.makissmpmod.UtilClass;
import org.jspecify.annotations.NonNull;

public class SmiteItem extends Item {
    public SmiteItem(Properties properties) {
        super(properties);
    }
    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand interactionHand) {
        if (UtilClass.getLookedAtBlock(player, 50, false) != null && !level.isClientSide()) {
            ItemStack stack = player.getItemInHand(interactionHand);
            player.getCooldowns().addCooldown(new ItemStack(stack.getItem()), UtilClass.SECOND * 5);
            Vec3 vec3 = player.pick(50.0, 0, false).getLocation();
            LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.COMMAND);
            assert lightningBolt != null;
            lightningBolt.move(MoverType.SELF, vec3);
            level.addFreshEntity(lightningBolt);
            stack.shrink(1);
        }
        return InteractionResult.PASS;
    }
}
