package org.example.makismod.makissmpmod.mixin.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerTabOverlay.class)
public class DisableSpectatorTabGreyMixin {

    @Unique
    private PlayerInfo makissmpmod$cachedPlayerInfo = null;

    /**
     * Changes the spectator grey color (-1862270977) to white (-1) in the tab list.
     * The color -1862270977 is 0x6FFFFFFF which is a semi-transparent grey used for spectators.
     */
    @ModifyConstant(
        method = "render",
        constant = @Constant(intValue = -1862270977)
    )
    private int disableSpectatorGrey(int original) {
        return -1; // Return white instead of grey
    }

    /**
     * Cache the PlayerInfo when entering getNameForDisplay.
     */
    @ModifyVariable(
        method = "getNameForDisplay",
        at = @At("HEAD"),
        argsOnly = true
    )
    private PlayerInfo cachePlayerInfo(PlayerInfo playerInfo) {
        makissmpmod$cachedPlayerInfo = playerInfo;
        return playerInfo;
    }

    /**
     * Removes italic formatting from the returned component for spectator players.
     */
    @Inject(
        method = "getNameForDisplay",
        at = @At("RETURN"),
        cancellable = true
    )
    private void removeSpectatorItalic(PlayerInfo playerInfo, CallbackInfoReturnable<Component> cir) {
        if (playerInfo.getGameMode() == GameType.SPECTATOR) {
            Component original = cir.getReturnValue();
            // Create a new style without italic
            Style newStyle = original.getStyle().withItalic(false);
            // Return the component with the new style
            cir.setReturnValue(original.copy().setStyle(newStyle));
        }
        makissmpmod$cachedPlayerInfo = null;
    }
}
