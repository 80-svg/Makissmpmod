package org.example.makismod.makissmpmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.example.makismod.makissmpmod.NickManager;

import java.util.Objects;

public final class NickCommand {
    private NickCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nick")
            .then(Commands.argument("target", EntityArgument.player())
                .requires(source -> source.isPlayer() && source.getServer().getPlayerList().isOp(Objects.requireNonNull(source.getPlayer()).nameAndId()))
                .then(Commands.argument("nick", StringArgumentType.greedyString())
                    .executes(NickCommand::executeOtherNick))));
    }

//    private static int executeSelfNick(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
//        ServerPlayer player = context.getSource().getPlayerOrException();
//        return toggleNick(player, player, StringArgumentType.getString(context, "nick"));
//    }

    private static int executeOtherNick(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer sourcePlayer = context.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        return toggleNick(sourcePlayer, target, StringArgumentType.getString(context, "nick"));
    }

    private static int toggleNick(ServerPlayer actor, ServerPlayer target, String requestedNick) {
        String trimmedNick = requestedNick.trim();
        String currentNick = NickManager.getNick(target);
        boolean selfTarget = actor.getUUID().equals(target.getUUID());

        if (trimmedNick.isEmpty()) {
            NickManager.setNick(target, "");
            sendFeedback(actor, target, "Nick cleared.", selfTarget);
            return 1;
        }

        if (trimmedNick.equals(currentNick)) {
            NickManager.setNick(target, "");
            sendFeedback(actor, target, "Nick toggled off.", selfTarget);
            return 1;
        }

        NickManager.setNick(target, trimmedNick);
        sendFeedback(actor, target, "Nick set to " + trimmedNick + ".", selfTarget);
        return 1;
    }

    private static void sendFeedback(ServerPlayer actor, ServerPlayer target, String message, boolean selfTarget) {
        if (selfTarget) {
            actor.sendSystemMessage(Component.literal(message));
            return;
        }

        actor.sendSystemMessage(Component.literal(target.getGameProfile().name() + ": " + message));
        target.sendSystemMessage(Component.literal(message));
    }
}
