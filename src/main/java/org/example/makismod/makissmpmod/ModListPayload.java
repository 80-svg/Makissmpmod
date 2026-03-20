package org.example.makismod.makissmpmod;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class ModListPayload {
    public record MyPayLoad(List<String> message) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<MyPayLoad> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("makissmpmod", "my_payload"));

        public static final StreamCodec<ByteBuf, MyPayLoad> CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(256)), MyPayLoad::message,
                MyPayLoad::new
        );

        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }
}
