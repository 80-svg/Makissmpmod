package org.example.makismod.makissmpmod.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.example.makismod.makissmpmod.SimpleConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LightsCommand {
    private static final String SWITCH_ENTITY_ID = "switch.cozylife_007f";
    private static final String HA_BASE_URL = "http://192.168.1.12:8123";

    private LightsCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("lights")
                .executes(LightsCommand::runToggleLightsCommand)
                .then(Commands.argument("state", BoolArgumentType.bool())
                        .executes(LightsCommand::runLightsCommand)));
        dispatcher.register(Commands.literal("getlights")
                .executes(LightsCommand::runGetLightsCommand));
    }

    public static int runLightsCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        boolean argument = BoolArgumentType.getBool(ctx, "state");
        return setLightState(ctx, argument);
    }

    public static int runToggleLightsCommand(CommandContext<CommandSourceStack> ctx) {
        String state = getLightStatus();
        if ("on".equalsIgnoreCase(state)) {
            return setLightState(ctx, false);
        }
        if ("off".equalsIgnoreCase(state)) {
            return setLightState(ctx, true);
        }

        ctx.getSource().sendFailure(Component.literal("Unable to read light state: " + state));
        return 0;
    }

    private static int setLightState(CommandContext<CommandSourceStack> ctx, boolean argument) {
        HttpClient client = HttpClient.newHttpClient();
        String token = SimpleConfig.ha_token;

        if (token == null || token.isBlank()) {
            ctx.getSource().sendFailure(Component.literal("Home Assistant token is not configured."));
            return 0;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HA_BASE_URL + "/api/services/switch/turn_" + (argument ? "on" : "off")))
                .POST(HttpRequest.BodyPublishers.ofString("{\"entity_id\": \"" + SWITCH_ENTITY_ID + "\"}"))
                .setHeader("Authorization", "Bearer " + token)
                .setHeader("Content-Type", "application/json")
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                ctx.getSource().sendSuccess(() -> Component.literal("Lights turned " + (argument ? "on" : "off") + "."), false);
                return 1;
            }

            ctx.getSource().sendFailure(Component.literal("Lights request failed: HTTP " + response.statusCode()));
            return 0;
        } catch (IOException e) {
            ctx.getSource().sendFailure(Component.literal("Lights request failed: " + e.getMessage()));
            return 0;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ctx.getSource().sendFailure(Component.literal("Lights request interrupted."));
            return 0;
        }
    }
    public static int runGetLightsCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ctx.getSource().getPlayer().sendSystemMessage(Component.literal(getLightStatus()));
        return 0;
    }
    public static String getLightStatus() {
        HttpClient client = HttpClient.newHttpClient();
        String token = SimpleConfig.ha_token;
        if (token == null || token.isBlank()) {
            return "Home Assistant token is not configured.";
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HA_BASE_URL + "/api/states/" + SWITCH_ENTITY_ID))
                .GET()
                .setHeader("Authorization", "Bearer " + token)
                .setHeader("Content-Type", "application/json")
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return "HTTP " + response.statusCode();
            }
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            String state = json.get("state").getAsString();
            return state;
        } catch (IOException e) {
            return "Lights request failed: " + e.getMessage();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Lights request interrupted.";
        }
    }
}
