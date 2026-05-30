package org.example.makismod.makissmpmod;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public final class ModAttachments {
    public static final AttachmentType<UUID> CURSE_PLAYER = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(Makissmpmod.MOD_ID, "curse_player"));
    public static final AttachmentType<String> CURSE_EFFECTS = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(Makissmpmod.MOD_ID, "curse_effects"));
    // Not needed because I can probably do hasAttached() on CURSE_EFFECTS and removeAttached() accordingly.
//    public static final AttachmentType<Boolean> IS_CURSED = AttachmentRegistry.create(
//            Identifier.fromNamespaceAndPath(Makissmpmod.MOD_ID, "is_cursed"));
    private ModAttachments() {}

    public static void initialize() {
        Makissmpmod.LOGGER.info("Initializing ModAttachments");
    }
}
