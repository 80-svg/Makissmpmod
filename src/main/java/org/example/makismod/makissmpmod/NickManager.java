package org.example.makismod.makissmpmod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NickManager {
    private static final Map<UUID, Component> NICKS = new ConcurrentHashMap<>();

    private NickManager() {
    }

    public static void setNick(ServerPlayer player, String nick) {
        String trimmedNick = nick.trim();

        if (trimmedNick.isEmpty()) {
            NICKS.remove(player.getUUID());
            player.setCustomName(null);
            player.setCustomNameVisible(false);
        } else {
            Component nickComponent = Component.literal(trimmedNick);
            NICKS.put(player.getUUID(), nickComponent);
            player.setCustomName(nickComponent);
            player.setCustomNameVisible(true);
        }

        player.level()
            .getServer()
            .getPlayerList()
            .broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player));
    }

    @Nullable
    public static String getNick(ServerPlayer player) {
        Component nickComponent = NICKS.get(player.getUUID());
        return nickComponent == null ? null : nickComponent.getString();
    }

    public static Component getNickComponent(ServerPlayer player) {
        return NICKS.get(player.getUUID());
    }
}
