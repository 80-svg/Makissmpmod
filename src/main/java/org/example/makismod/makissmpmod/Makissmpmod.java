package org.example.makismod.makissmpmod;

import net.fabricmc.api.ModInitializer;
//import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
//import net.minecraft.resources.Identifier;
//import net.minecraft.util.datafix.fixes.GameRuleRegistryFix;
//import net.minecraft.world.level.gamerules.GameRule;
//import net.minecraft.world.level.gamerules.GameRuleCategory;
//import net.minecraft.world.level.gamerules.GameRules;

public class Makissmpmod implements ModInitializer {
    public static final String MOD_ID = "makissmpmod";
    public static final Set<UUID> payloadPlayers = new HashSet<>();
    @Override
    public void onInitialize() {
        ModItems.initialize();
        ServerMessageEvents.ALLOW_GAME_MESSAGE.register(((minecraftServer, component, b) -> {
            String msg = component.getString();
            if (msg.contains("joined the game") || msg.contains("left the game")) {
                return false;
            }
            return true;
        }));
        ServerPlayConnectionEvents.JOIN.register(new ConnectionMessages());
        ServerPlayConnectionEvents.DISCONNECT.register(new ConnectionMessages());
        PayloadTypeRegistry.playC2S().register(ModListPayload.MyPayLoad.ID, ModListPayload.MyPayLoad.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ModListPayload.MyPayLoad.ID, ((myPayLoad, context) -> {
            context.server().execute(() -> {
                List<String> received = myPayLoad.message();
                ServerPlayer player = context.player();
                String adminL = "Makis1445";
                ServerPlayer adminPlayer = context.server().getPlayerList().getPlayer(adminL);
                Makissmpmod.payloadPlayers.remove(player.getUUID());
                List<String> badMods = List.of("freecam", "meteor-client", "replaymod", "xaerominimap");
                    if (!Collections.disjoint(received, badMods)) {
                        player.connection.disconnect(Component.literal("You are a bad boy for installing unallowed mods"));
                    }
                List<String> boringMods = List.of("fabric", "org_", "com_");
                Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                    if (adminPlayer != null) {
                        for (String s : received) {
                            boolean isBoring = boringMods.stream().anyMatch(s::startsWith);
                            if (!isBoring) {
                                adminPlayer.sendSystemMessage(Component.literal(s));
                            }
                        }
                    }
                }, 1, TimeUnit.SECONDS);
            });
        }));
    }
}
