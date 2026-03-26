package org.example.makismod.makissmpmod.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import org.example.makismod.makissmpmod.NickManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerNickMixin {
    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private void getName(CallbackInfoReturnable<Component> cir) {
        Component nickComponent = getEffectiveNick();
        if (nickComponent != null) {
            cir.setReturnValue(nickComponent);
        }
    }

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    private void getDisplayName(CallbackInfoReturnable<Component> cir) {
        Player player = (Player) (Object) this;
        Component nickComponent = getEffectiveNick();
        if (nickComponent == null) {
            return;
        }

        MutableComponent displayName = PlayerTeam.formatNameForTeam(player.getTeam(), nickComponent.copy());
        cir.setReturnValue(displayName);
    }

    @Unique
    private Component getEffectiveNick() {
        ServerPlayer serverPlayer = asServerPlayer();
        if (serverPlayer != null) {
            Component nickComponent = NickManager.getNickComponent(serverPlayer);
            if (nickComponent != null) {
                return nickComponent;
            }
        }

        return ((Player) (Object) this).getCustomName();
    }

    @Unique
    private ServerPlayer asServerPlayer() {
        Object self = this;
        return self instanceof ServerPlayer serverPlayer ? serverPlayer : null;
    }
}
