package org.example.makismod.makissmpmod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

public final class UtilClass {
    private UtilClass() {}

    @Nullable
    public static BlockHitResult getLookedAtBlock(Player player, double reachDistance, boolean includeFluids) {
        HitResult hitResult = player.pick(reachDistance, 0.0F, includeFluids);
        return hitResult.getType() == HitResult.Type.BLOCK ? (BlockHitResult) hitResult : null;
    }

    @Nullable
    public static BlockPos getLookedAtBlockPos(Player player, double reachDistance, boolean includeFluids) {
        BlockHitResult blockHitResult = getLookedAtBlock(player, reachDistance, includeFluids);
        return blockHitResult != null ? blockHitResult.getBlockPos() : null;
    }
    public static int SECOND = 20;
}
