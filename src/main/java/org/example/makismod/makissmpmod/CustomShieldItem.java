package org.example.makismod.makissmpmod;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;

public class CustomShieldItem extends ShieldItem {
    private static final int BLOCK_ATTACK_COOLDOWN_TICKS = 200;

    public CustomShieldItem(Properties properties) {
        super(properties);
    }

    public void onBlockAttackCombination(Level level, Player player, ItemStack stack, InteractionHand hand) {
        if (level.isClientSide()) {
            return;
        }

        player.getCooldowns().addCooldown(stack, BLOCK_ATTACK_COOLDOWN_TICKS);

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.literal("Custom shield combo triggered."));
        }
    }
}
