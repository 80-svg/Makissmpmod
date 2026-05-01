package org.example.makismod.makissmpmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.example.makismod.makissmpmod.Makissmpmod;

public class CurseCommand {
    private CurseCommand() {}
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        Makissmpmod.LOGGER.info("Registering CurseCommand");
        dispatcher.register(Commands.literal("curse")
                .executes(context -> {
                    context.getSource().getPlayer().sendSystemMessage(Component.literal("Usage: /curse <player>"));
                    return 0;
                })
                .then(Commands.argument("target", EntityArgument.player()))
                .executes(CurseCommand::runCurseCommand));
    }
    public static int runCurseCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayer();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        player.sendSystemMessage(Component.literal("Are you sure you want to curse" + target.getName().getString()).withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("[Yes]").withStyle(ChatFormatting.GREEN).append(" [No]").withStyle(ChatFormatting.RED));
        return 1;
    }
}
