package org.example.makismod.makissmpmod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

public final class ModEnchantments {
    public static final ResourceKey<Enchantment> HOTUP = ResourceKey.create(Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(Makissmpmod.MOD_ID, "hotup"));
    public static final ResourceKey<Enchantment> BOUNCE_BACK = ResourceKey.create(Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(Makissmpmod.MOD_ID, "bounce_back"));
    private ModEnchantments() {
    }
}
