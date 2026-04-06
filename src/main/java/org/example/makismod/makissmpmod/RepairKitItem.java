package org.example.makismod.makissmpmod;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public class RepairKitItem extends Item {
    public RepairKitItem(Properties properties) {
        super(properties);
    }
    public @NonNull InteractionResult use(@NonNull Level level, Player player, @NonNull InteractionHand interactionHand) {
        ItemStack offhand = player.getOffhandItem();
        ItemStack mainHand = player.getMainHandItem();
        if (offhand.is(ModItems.REPAIR_KIT) && mainHand.isDamageableItem()) {
            mainHand.setDamageValue(0);
        }
        offhand.shrink(1);
        return InteractionResult.PASS;
    }
}
