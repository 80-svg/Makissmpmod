package org.example.makismod.makissmpmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import org.example.makismod.makissmpmod.Makissmpmod;
import org.example.makismod.makissmpmod.ModAttachments;

import java.util.Objects;
import java.util.UUID;

public class CurseCommand {
    private CurseCommand() {}
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext) {
        Makissmpmod.LOGGER.info("Registering CurseCommand");
        dispatcher.register(Commands.literal("curse")
                .executes(context -> {
                    Objects.requireNonNull(context.getSource().getPlayer()).sendSystemMessage(Component.literal("Usage: /curse <player>"));
                    return 0;
                })
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("effects", ResourceArgument.resource(commandBuildContext, Registries.MOB_EFFECT))
                                .executes(CurseCommand::runCurseCommand))));
        dispatcher.register(Commands.literal("internal_curse")
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("effects", ResourceArgument.resource(commandBuildContext, Registries.MOB_EFFECT))
                                .then(Commands.argument("doesPlayerAccept", BoolArgumentType.bool())
                                        .then(Commands.argument("token", StringArgumentType.word())
                                                .executes(CurseCommand::runInternals))))));
    }
    public static int runCurseCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayer();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        Holder.Reference<MobEffect> effect = ResourceArgument.getMobEffect(context, "effects");
        assert player != null;
        if (player.getUUID().equals(target.getUUID())) {
            player.sendSystemMessage(Component.literal("You cannot curse yourself.").withStyle(ChatFormatting.RED));
            return 0;
        }

        String effectId = effectId(effect);
        String token = UUID.randomUUID().toString();
        Makissmpmod.pendingCurses.put(player.getUUID(), token);

        String yesCommand = "/internal_curse " + target.getScoreboardName() + " " + effectId + " true " + token;
        String noCommand = "/internal_curse " + target.getScoreboardName() + " " + effectId + " false " + token;

        player.sendSystemMessage(Component.literal("Are you sure you want to curse " + target.getName().getString() + " with " + effectId + "?")
                .withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(
                Component.literal("[Yes]")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)
                                .withClickEvent(new ClickEvent.RunCommand(yesCommand)))
                        .append(Component.literal(" [No]")
                                .withStyle(Style.EMPTY.withColor(ChatFormatting.RED)
                                        .withClickEvent(new ClickEvent.RunCommand(noCommand))))
        );
        return 1;
    }
    public static int runInternals(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayer();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        Holder.Reference<MobEffect> effect = ResourceArgument.getMobEffect(context, "effects");
        boolean doesPlayerAccept = BoolArgumentType.getBool(context, "doesPlayerAccept");
        String token = StringArgumentType.getString(context, "token");
        assert player != null;
        String expectedToken = Makissmpmod.pendingCurses.remove(player.getUUID());
        if (!token.equals(expectedToken)) {
            player.sendSystemMessage(Component.literal("Invalid curse confirmation.").withStyle(ChatFormatting.RED));
            return 0;
        }

        if (!doesPlayerAccept) {
            player.sendSystemMessage(Component.literal("Curse cancelled.").withStyle(ChatFormatting.RED));
            return 0;
        }

        target.setAttached(ModAttachments.CURSE_PLAYER, player.getUUID());
        target.setAttached(ModAttachments.CURSE_EFFECTS, effectId(effect));
        target.addEffect(new MobEffectInstance(effect, Integer.MAX_VALUE, 0, false, true, true));
        player.sendSystemMessage(Component.literal("Applied " + effectId(effect) + " to " + target.getName().getString() + ".")
                .withStyle(ChatFormatting.GREEN));
        return 1;
    }

    private static String effectId(Holder.Reference<MobEffect> effect) {
        return Objects.requireNonNull(BuiltInRegistries.MOB_EFFECT.getKey(effect.value())).toString();
    }

    public static Holder<MobEffect> effectFromId(String effectId) {
        MobEffect effect = BuiltInRegistries.MOB_EFFECT.getOptional(Identifier.parse(effectId))
                .orElseThrow(() -> new IllegalArgumentException("Unknown mob effect id: " + effectId));
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect);
    }
    public static void tickPermanentEffect(MinecraftServer server) {
        for (Player p : server.getPlayerList().getPlayers()) {
            if (p.hasAttached(ModAttachments.CURSE_EFFECTS)) {
                p.addEffect(new MobEffectInstance(effectFromId(p.getAttached(ModAttachments.CURSE_EFFECTS)), Integer.MAX_VALUE, 0, false, true, true));
            }
        }
    }

}
