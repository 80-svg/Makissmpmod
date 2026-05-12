package org.example.makismod.makissmpmod;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MindControlManager {
    private static final Map<UUID, UUID> controllerToTarget = new HashMap<>();
    private static final Map<UUID, UUID> targetToController = new HashMap<>();
    private static final Map<UUID, ControllerReturnState> controllerReturnStates = new HashMap<>();

    private MindControlManager() {}

    public static void possess(ServerPlayer target, ServerPlayer controller) {
        releaseController(controller);
        releaseTarget(target);
        controllerReturnStates.put(controller.getUUID(), new ControllerReturnState(
                controller.gameMode(),
                controller.noPhysics,
                controller.level(),
                controller.getX(),
                controller.getY(),
                controller.getZ(),
                controller.getYRot(),
                controller.getXRot()
        ));
        controller.setGameMode(GameType.SPECTATOR);
        controller.noPhysics = true;
        controllerToTarget.put(controller.getUUID(), target.getUUID());
        targetToController.put(target.getUUID(), controller.getUUID());
    }

    public static void release(ServerPlayer target) {
        UUID controllerId = targetToController.remove(target.getUUID());
        if (controllerId != null) {
            controllerToTarget.remove(controllerId);
            controllerReturnStates.remove(controllerId);
        }
    }

    public static void releaseController(Player controller) {
        UUID targetId = controllerToTarget.remove(controller.getUUID());
        if (targetId != null) {
            targetToController.remove(targetId);
        }
        controllerReturnStates.remove(controller.getUUID());
    }

    public static void releaseTarget(Player target) {
        UUID controllerId = targetToController.remove(target.getUUID());
        if (controllerId != null) {
            controllerToTarget.remove(controllerId);
        }
    }

    public static UUID getTargetId(Player controller) {
        return controllerToTarget.get(controller.getUUID());
    }

    public static UUID getControllerId(Player target) {
        return targetToController.get(target.getUUID());
    }

    public static void syncControllerToTarget(ServerPlayer controller, ServerPlayer target) {
        controller.teleportTo(
                target.level(),
                target.getX(),
                target.getY(),
                target.getZ(),
                Set.of(),
                target.getYRot(),
                target.getXRot(),
                true
        );
    }

    public static void restoreController(ServerPlayer controller) {
        ControllerReturnState state = controllerReturnStates.remove(controller.getUUID());
        if (state == null) {
            return;
        }

        ServerLevel level = state.level();
        if (level == null) {
            return;
        }

        controller.teleportTo(
                level,
                state.x(),
                state.y(),
                state.z(),
                Set.of(),
                state.yaw(),
                state.pitch(),
                true
        );
        if (state.gameType() != GameType.SPECTATOR) {
            controller.setGameMode(state.gameType());
        } else {
            controller.setGameMode(GameType.SURVIVAL);
        }
        controller.noPhysics = state.noPhysics();
    }

    public static void tickMindControl(MinecraftServer server) {
        for (ServerPlayer controller : server.getPlayerList().getPlayers()) {
            UUID targetId = MindControlManager.getTargetId(controller);
            if (targetId == null) continue;

            ServerPlayer target = server.getPlayerList().getPlayer(targetId);
            if (target == null) {
                MindControlManager.releaseController(controller);
                continue;
            }
            MindControlManager.syncControllerToTarget(controller, target);
        }
    }

    private record ControllerReturnState(
            GameType gameType,
            boolean noPhysics,
            ServerLevel level,
            double x,
            double y,
            double z,
            float yaw,
            float pitch
    ) {}
}
