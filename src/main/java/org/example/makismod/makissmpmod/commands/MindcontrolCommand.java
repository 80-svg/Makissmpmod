package org.example.makismod.makissmpmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.example.makismod.makissmpmod.LockoutPayload;
import org.example.makismod.makissmpmod.Makissmpmod;
import org.example.makismod.makissmpmod.MindControlCapturePayload;
import org.example.makismod.makissmpmod.MindControlManager;

import java.util.Objects;

public class MindcontrolCommand {
    private MindcontrolCommand() {}
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        Makissmpmod.LOGGER.info("Registering MindcontrolCommand");
        dispatcher.register(Commands.literal("mindcontrol")
                        .executes(context -> { return 1;})
                .requires(commandSourceStack -> commandSourceStack.isPlayer() && commandSourceStack.getServer().getPlayerList().isOp(Objects.requireNonNull(commandSourceStack.getPlayer()).nameAndId()))
                .then(Commands.argument("target", EntityArgument.player())
                .executes(MindcontrolCommand::runPossess)));
        dispatcher.register(Commands.literal("unpossess")
                        .executes(context -> { return 1;})
                .requires(commandSourceStack -> commandSourceStack.isPlayer() && commandSourceStack.getServer().getPlayerList().isOp(Objects.requireNonNull(commandSourceStack.getPlayer()).nameAndId()))
                .then(Commands.argument("target", EntityArgument.player())
                .executes(MindcontrolCommand::unPossess)));
    }
    public static int runPossess(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = Objects.requireNonNull(context.getSource()).getPlayer();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        if (player == target) {
            context.getSource().sendFailure(Component.literal("You cannot control yourself!!"));
            return 0;
        }
        ServerPlayer controller = (ServerPlayer) player;
        MindControlManager.possess(target, controller);
        MindControlManager.syncControllerToTarget(controller, target);
        ServerPlayNetworking.send(target, new LockoutPayload.lockoutoutpayload(true, player.getName().getString(), player.getId()));
        ServerPlayNetworking.send(controller, new MindControlCapturePayload.CaptureStateS2CPayload(true, target.getId()));
        Makissmpmod.lockoutPlayers.add(target.getUUID());
        return 1;
    }
    public static int unPossess(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player actor = Objects.requireNonNull(context.getSource()).getPlayer();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");

        ServerPlayer controller = (ServerPlayer) actor;
        MindControlManager.release(target);
        MindControlManager.restoreController(controller);
        ServerPlayNetworking.send(target, new LockoutPayload.lockoutoutpayload(false, actor.getName().getString(), target.getId()));
        ServerPlayNetworking.send(controller, new MindControlCapturePayload.CaptureStateS2CPayload(false, -1));
        Makissmpmod.lockoutPlayers.remove(target.getUUID());
        return 1;
    }
}
