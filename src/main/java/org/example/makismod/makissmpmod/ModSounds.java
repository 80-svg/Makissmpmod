package org.example.makismod.makissmpmod;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    private ModSounds() {
    }
    public static final SoundEvent DIMITRI_PLAY_HK = registerSound("dimitri_play_hk");
    private static SoundEvent registerSound(String id) {
        Identifier identifier = Identifier.fromNamespaceAndPath(Makissmpmod.MOD_ID, id);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, identifier, SoundEvent.createVariableRangeEvent(identifier));
    }
    public static void Initialize() {
        Makissmpmod.LOGGER.info("Initializing ModSounds");
    }
}
