package org.example.makismod.makissmpmod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.example.makismod.makissmpmod.ModListPayload;

import java.util.List;
import java.util.stream.Collectors;

public class MakissmpmodClient implements ClientModInitializer {
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
    }
}
