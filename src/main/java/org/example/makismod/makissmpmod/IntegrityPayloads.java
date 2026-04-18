package org.example.makismod.makissmpmod;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class IntegrityPayloads {
    public record IntegrityChallengeS2C(String challenge) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<IntegrityChallengeS2C> ID =
                new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("makissmpmod", "integrity_challenge"));

        public static final StreamCodec<ByteBuf, IntegrityChallengeS2C> CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, IntegrityChallengeS2C::challenge,
                IntegrityChallengeS2C::new
        );

        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record IntegrityResponseC2S(String challenge, String hmacResponse, String modVersion) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<IntegrityResponseC2S> ID =
                new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("makissmpmod", "integrity_response"));

        public static final StreamCodec<ByteBuf, IntegrityResponseC2S> CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, IntegrityResponseC2S::challenge,
                ByteBufCodecs.STRING_UTF8, IntegrityResponseC2S::hmacResponse,
                ByteBufCodecs.STRING_UTF8, IntegrityResponseC2S::modVersion,
                IntegrityResponseC2S::new
        );

        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }
}