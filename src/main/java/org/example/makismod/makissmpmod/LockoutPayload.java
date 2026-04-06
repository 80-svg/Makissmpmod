package org.example.makismod.makissmpmod;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NonNull;

public class LockoutPayload {
    public record lockoutoutpayload(Boolean playerLockedInput, String actorPlayerName, int entityID) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<lockoutoutpayload> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Makissmpmod.MOD_ID, "lockout_payload"));

        public static final StreamCodec<ByteBuf, lockoutoutpayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, lockoutoutpayload::playerLockedInput,
                ByteBufCodecs.PLAYER_NAME, lockoutoutpayload::actorPlayerName,
                ByteBufCodecs.INT, lockoutoutpayload::entityID,
                lockoutoutpayload::new
        );

        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }
}
