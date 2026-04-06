package org.example.makismod.makissmpmod;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public final class MindControlInputPayload {
    private MindControlInputPayload() {}

    public record ControllerInputC2SPayload(
            float strafe,
            float vertical,
            float forward,
            boolean jumping,
            boolean sprinting,
            boolean sneaking,
            boolean attacking,
            boolean using,
            float yaw,
            float pitch
    ) implements CustomPacketPayload {
        public static final Type<ControllerInputC2SPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(Makissmpmod.MOD_ID, "mindcontrol_input_c2s"));
        public static final StreamCodec<ByteBuf, ControllerInputC2SPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.FLOAT, ControllerInputC2SPayload::strafe,
                ByteBufCodecs.FLOAT, ControllerInputC2SPayload::vertical,
                ByteBufCodecs.FLOAT, ControllerInputC2SPayload::forward,
                ByteBufCodecs.BOOL, ControllerInputC2SPayload::jumping,
                ByteBufCodecs.BOOL, ControllerInputC2SPayload::sprinting,
                ByteBufCodecs.BOOL, ControllerInputC2SPayload::sneaking,
                ByteBufCodecs.BOOL, ControllerInputC2SPayload::attacking,
                ByteBufCodecs.BOOL, ControllerInputC2SPayload::using,
                ByteBufCodecs.FLOAT, ControllerInputC2SPayload::yaw,
                ByteBufCodecs.FLOAT, ControllerInputC2SPayload::pitch,
                ControllerInputC2SPayload::new
        );

        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record TargetInputS2CPayload(
            float strafe,
            float vertical,
            float forward,
            boolean jumping,
            boolean sprinting,
            boolean sneaking,
            boolean attacking,
            boolean using,
            float yaw,
            float pitch
    ) implements CustomPacketPayload {
        public static final Type<TargetInputS2CPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(Makissmpmod.MOD_ID, "mindcontrol_input_s2c"));
        public static final StreamCodec<ByteBuf, TargetInputS2CPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.FLOAT, TargetInputS2CPayload::strafe,
                ByteBufCodecs.FLOAT, TargetInputS2CPayload::vertical,
                ByteBufCodecs.FLOAT, TargetInputS2CPayload::forward,
                ByteBufCodecs.BOOL, TargetInputS2CPayload::jumping,
                ByteBufCodecs.BOOL, TargetInputS2CPayload::sprinting,
                ByteBufCodecs.BOOL, TargetInputS2CPayload::sneaking,
                ByteBufCodecs.BOOL, TargetInputS2CPayload::attacking,
                ByteBufCodecs.BOOL, TargetInputS2CPayload::using,
                ByteBufCodecs.FLOAT, TargetInputS2CPayload::yaw,
                ByteBufCodecs.FLOAT, TargetInputS2CPayload::pitch,
                TargetInputS2CPayload::new
        );

        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }
}
