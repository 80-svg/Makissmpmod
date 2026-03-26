package org.example.makismod.makissmpmod.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.example.makismod.makissmpmod.NickManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerNickMixin {
    @Inject(method = "getTabListDisplayName", at = @At("HEAD"), cancellable = true)
    private void getTabListDisplayName(CallbackInfoReturnable<Component> cir) {
        Component nickComponent = NickManager.getNickComponent((ServerPlayer) (Object) this);
        if (nickComponent != null) {
            cir.setReturnValue(nickComponent);
        }
    }
}
