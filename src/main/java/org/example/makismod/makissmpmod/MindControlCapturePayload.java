package org.example.makismod.makissmpmod;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public final class MindControlCapturePayload {
    private MindControlCapturePayload() {}

    public record CaptureStateS2CPayload(boolean capturing, int targetEntityId) implements CustomPacketPayload {
        public static final Type<CaptureStateS2CPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(Makissmpmod.MOD_ID, "mindcontrol_capture_state"));
        public static final StreamCodec<ByteBuf, CaptureStateS2CPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, CaptureStateS2CPayload::capturing,
                ByteBufCodecs.INT, CaptureStateS2CPayload::targetEntityId,
                CaptureStateS2CPayload::new
        );

        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }
}
