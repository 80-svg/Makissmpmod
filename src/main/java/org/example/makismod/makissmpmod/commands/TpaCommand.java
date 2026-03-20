package org.example.makismod.makissmpmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TpaCommand {

    public static class TpaRequest {
        public final UUID requesterId;
        public final boolean isTpaHere;

        public TpaRequest(UUID requesterId, boolean isTpaHere) {
            this.requesterId = requesterId;
            this.isTpaHere = isTpaHere;
        }
    }

    private static final Map<UUID, TpaRequest> pendingRequests = new HashMap<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tpa")
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(TpaCommand::executeTpa)));

        dispatcher.register(Commands.literal("tpahere")
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(TpaCommand::executeTpaHere)));

        dispatcher.register(Commands.literal("tpaccept")
                .executes(TpaCommand::executeTpAccept));

        dispatcher.register(Commands.literal("tpdeny")
                .executes(TpaCommand::executeTpDeny));

        dispatcher.register(Commands.literal("tpcancel")
                .executes(TpaCommand::executeTpCancel));
    }

    private static boolean payForTpa(ServerPlayer player) {
        if (player.isCreative()) return true;

        int coinsFound = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(org.example.makismod.makissmpmod.ModItems.SILVER_COIN)) {
                coinsFound += stack.getCount();
            }
        }

        if (coinsFound < 5) {
            return false;
        }

        int coinsToTake = 5;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(org.example.makismod.makissmpmod.ModItems.SILVER_COIN)) {
                int shrinkBy = Math.min(coinsToTake, stack.getCount());
                stack.shrink(shrinkBy);
                coinsToTake -= shrinkBy;
                if (coinsToTake <= 0) break;
            }
        }
        return true;
    }

    private static void refundForTpa(ServerPlayer player) {
        if (player.isCreative()) return;
        assert org.example.makismod.makissmpmod.ModItems.SILVER_COIN != null;
        player.getInventory().placeItemBackInInventory(new ItemStack(org.example.makismod.makissmpmod.ModItems.SILVER_COIN, 5));
    }

    private static void cancelOutgoingRequest(ServerPlayer requester) {
        UUID oldTarget = null;
        for (Map.Entry<UUID, TpaRequest> entry : pendingRequests.entrySet()) {
            if (entry.getValue().requesterId.equals(requester.getUUID())) {
                oldTarget = entry.getKey();
                break;
            }
        }
        if (oldTarget != null) {
            pendingRequests.remove(oldTarget);
            refundForTpa(requester);
            requester.sendSystemMessage(Component.literal("Your previous pending teleport request was cancelled and 5 Silver Coins were refunded."));
        }
    }

    private static void clearIncomingRequest(ServerPlayer target, CommandContext<CommandSourceStack> context) {
        if (pendingRequests.containsKey(target.getUUID())) {
            TpaRequest oldReq = pendingRequests.remove(target.getUUID());
            ServerPlayer oldRequester = context.getSource().getServer().getPlayerList().getPlayer(oldReq.requesterId);
            if (oldRequester != null) {
                refundForTpa(oldRequester);
                oldRequester.sendSystemMessage(Component.literal("Your teleport request was overwritten by a newer request. 5 Silver Coins were refunded."));
            }
        }
    }

    private static int executeTpa(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer requester = context.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");

        if (requester.getUUID().equals(target.getUUID())) {
            requester.sendSystemMessage(Component.literal("You cannot send a TPA request to yourself!"));
            return 0;
        }

        if (!payForTpa(requester)) {
            requester.sendSystemMessage(Component.literal("You need 5 Silver Coins to use this command!"));
            return 0;
        }

        cancelOutgoingRequest(requester);
        clearIncomingRequest(target, context);

        pendingRequests.put(target.getUUID(), new TpaRequest(requester.getUUID(), false));

        requester.sendSystemMessage(Component.literal("TPA request sent to " + target.getScoreboardName() + " (-5 Silver Coins)."));
        target.sendSystemMessage(Component.literal(requester.getScoreboardName() + " has requested to teleport to you. Type /tpaccept to accept or /tpdeny to deny."));

        return 1;
    }

    private static int executeTpaHere(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer requester = context.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");

        if (requester.getUUID().equals(target.getUUID())) {
            requester.sendSystemMessage(Component.literal("You cannot send a TPAHere request to yourself!"));
            return 0;
        }

        if (!payForTpa(requester)) {
            requester.sendSystemMessage(Component.literal("You need 5 Silver Coins to use this command!"));
            return 0;
        }

        cancelOutgoingRequest(requester);
        clearIncomingRequest(target, context);

        pendingRequests.put(target.getUUID(), new TpaRequest(requester.getUUID(), true));

        requester.sendSystemMessage(Component.literal("TPAHere request sent to " + target.getScoreboardName() + " (-5 Silver Coins)."));
        target.sendSystemMessage(Component.literal(requester.getScoreboardName() + " has requested you to teleport to them. Type /tpaccept to accept or /tpdeny to deny."));

        return 1;
    }

    private static int executeTpAccept(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = context.getSource().getPlayerOrException();

        if (!pendingRequests.containsKey(target.getUUID())) {
            target.sendSystemMessage(Component.literal("You do not have any pending TPA requests."));
            return 0;
        }

        TpaRequest request = pendingRequests.remove(target.getUUID());
        ServerPlayer requester = context.getSource().getServer().getPlayerList().getPlayer(request.requesterId);

        if (requester == null) {
            target.sendSystemMessage(Component.literal("The player who requested the teleport is no longer online."));
            return 0;
        }

        if (request.isTpaHere) {
            target.teleportTo(requester.level(), requester.getX(), requester.getY(), requester.getZ(), java.util.Set.of(), requester.getYRot(), requester.getXRot(), false);
            target.sendSystemMessage(Component.literal("TPAHere request accepted. Teleporting..."));
            requester.sendSystemMessage(Component.literal(target.getScoreboardName() + " accepted your TPAHere request."));
        } else {
            requester.teleportTo(target.level(), target.getX(), target.getY(), target.getZ(), java.util.Set.of(), target.getYRot(), target.getXRot(), false);
            target.sendSystemMessage(Component.literal("TPA request accepted."));
            requester.sendSystemMessage(Component.literal("TPA request accepted. Teleporting..."));
        }

        return 1;
    }

    private static int executeTpDeny(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = context.getSource().getPlayerOrException();

        if (!pendingRequests.containsKey(target.getUUID())) {
            target.sendSystemMessage(Component.literal("You do not have any pending TPA requests."));
            return 0;
        }

        TpaRequest request = pendingRequests.remove(target.getUUID());
        ServerPlayer requester = context.getSource().getServer().getPlayerList().getPlayer(request.requesterId);

        target.sendSystemMessage(Component.literal("TPA request denied."));
        if (requester != null) {
            refundForTpa(requester);
            requester.sendSystemMessage(Component.literal(target.getScoreboardName() + " denied your request. 5 Silver Coins were refunded."));
        }

        return 1;
    }

    private static int executeTpCancel(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer requester = context.getSource().getPlayerOrException();

        UUID targetToRemove = null;
        for (Map.Entry<UUID, TpaRequest> entry : pendingRequests.entrySet()) {
            if (entry.getValue().requesterId.equals(requester.getUUID())) {
                targetToRemove = entry.getKey();
                break;
            }
        }

        if (targetToRemove == null) {
            requester.sendSystemMessage(Component.literal("You do not have any outgoing TPA requests."));
            return 0;
        }

        pendingRequests.remove(targetToRemove);
        refundForTpa(requester);
        requester.sendSystemMessage(Component.literal("TPA request cancelled. 5 Silver Coins were refunded."));

        return 1;
    }
}
