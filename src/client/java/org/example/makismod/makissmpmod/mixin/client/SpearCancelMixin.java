package org.example.makismod.makissmpmod.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.example.makismod.makissmpmod.CustomShieldItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class SpearCancelMixin {
    @Shadow private LocalPlayer player;
    @Shadow public MultiPlayerGameMode gameMode;

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void redirectSpearUseToOffhandShield(CallbackInfo ci) {
        if (this.player == null || this.gameMode == null) {
            return;
        }

        if (!CustomShieldItem.hasRequiredComboItems(this.player)) {
            return;
        }

        this.gameMode.useItem(this.player, InteractionHand.OFF_HAND);
        ci.cancel();
    }
}
