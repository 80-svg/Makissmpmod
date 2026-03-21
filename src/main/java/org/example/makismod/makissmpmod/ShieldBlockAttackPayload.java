package org.example.makismod.makissmpmod;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record ShieldBlockAttackPayload(boolean trigger) implements CustomPacketPayload {
    public static final Type<ShieldBlockAttackPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(Makissmpmod.MOD_ID, "shield_block_attack"));
    public static final StreamCodec<ByteBuf, ShieldBlockAttackPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ShieldBlockAttackPayload::trigger,
            ShieldBlockAttackPayload::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
