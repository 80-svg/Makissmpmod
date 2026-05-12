package org.example.makismod.makissmpmod;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConnectionMessages implements ServerPlayConnectionEvents.Join, ServerPlayConnectionEvents.Disconnect {
    private void changeMessage(MinecraftServer server, ServerPlayer player, String symbol, boolean delayed) {
    String name = player.getName().getString();
    Component message = Component.literal("§7[§" + symbol + "§7] §f" + name);
    if (delayed) {
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            server.execute(() -> server.getPlayerList().broadcastSystemMessage(message, false));
        }, 200, TimeUnit.MILLISECONDS);
    } else {
        server.getPlayerList().broadcastSystemMessage(message, false);
    }
    }
    @Override
    public void onPlayDisconnect(ServerGamePacketListenerImpl serverGamePacketListener, MinecraftServer minecraftServer) {
    ServerPlayer player =  (ServerPlayer) serverGamePacketListener.getPlayer();
    changeMessage(minecraftServer, player, "c-", false);
    }

    @Override
    public void onPlayReady(ServerGamePacketListenerImpl serverGamePacketListener, PacketSender packetSender, MinecraftServer minecraftServer) {
    ServerPlayer player =  (ServerPlayer) serverGamePacketListener.getPlayer();
    changeMessage(minecraftServer, player, "a+", true);
    Makissmpmod.ModlistPlayers.add(player.getUUID());
    minecraftServer.execute(() -> {
        HmacManager.sendIntegrityChallenge(player);
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            minecraftServer.execute(() -> {
                if (Makissmpmod.pendingChallenges.containsKey(player.getUUID())) {
                    player.connection.disconnect(Component.literal("Integrity verification timed out - please rejoin"));
                }
            });
        }, Makissmpmod.VERIFICATION_TIMEOUT, TimeUnit.SECONDS);
        new Thread(() -> {
           try {
               Thread.sleep(1000);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
            minecraftServer.execute(() -> {
                if (Makissmpmod.ModlistPlayers.contains(player.getUUID())) {
                    player.connection.disconnect(Component.literal("Please install makissmpmod"));
                }
            });
        }).start();
    });
    }
}
