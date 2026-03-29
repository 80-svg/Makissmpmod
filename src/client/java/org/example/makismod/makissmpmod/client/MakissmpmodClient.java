package org.example.makismod.makissmpmod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.fabricmc.loader.api.FabricLoader;
import org.example.makismod.makissmpmod.CustomShieldItem;
import org.example.makismod.makissmpmod.ModEntityTypes;
import org.example.makismod.makissmpmod.ModListPayload;
import org.example.makismod.makissmpmod.ShieldBlockAttackPayload;

import java.util.List;
import java.util.stream.Collectors;

public class MakissmpmodClient implements ClientModInitializer {
    private static boolean sentBlockAttackThisPress;

    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            List<String> modIds = FabricLoader.getInstance()
                            .getAllMods()
                                    .stream()
                                            .map(modContainer -> modContainer.getMetadata().getId())
                                                    .toList();
            sender.sendPacket(new ModListPayload.MyPayLoad(modIds));
        });
        EntityModelLayerRegistry.registerModelLayer(EpsteinModelLayers.EPSTEIN, EpsteinEntityModel::createBodyLayer);
        EntityRenderers.register(ModEntityTypes.EPSTEIN_ENTITY, EpsteinEntityRenderer::new);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                sentBlockAttackThisPress = false;
                EffectVisualState.tick(client);
                return;
            }

            boolean shouldTrigger = CustomShieldItem.isBlockingComboReady(client.player)
                    && client.options.keyAttack.isDown();

            if (shouldTrigger && !sentBlockAttackThisPress) {
                ClientPlayNetworking.send(new ShieldBlockAttackPayload(true));
            }

            sentBlockAttackThisPress = shouldTrigger;
            EffectVisualState.tick(client);
        });
    }
}
