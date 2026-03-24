package org.example.makismod.makissmpmod;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

public final class ModPotions {
    public static final Holder<Potion> DROWSYNESS = register("drowsyness",
            new Potion("drowsyness", new MobEffectInstance(ModEffects.DROWSYNESS, 20 * 30)));
    public static final Holder<Potion> LONG_DROWSYNESS = register("long_drowsyness",
            new Potion("drowsyness", new MobEffectInstance(ModEffects.DROWSYNESS, 20 * 90)));

    private ModPotions() {
    }

    public static void initialize() {
        addPotionVariantsToCreativeTab(DROWSYNESS);
        addPotionVariantsToCreativeTab(LONG_DROWSYNESS);
    }

    private static Holder<Potion> register(String name, Potion potion) {
        return Registry.registerForHolder(
                BuiltInRegistries.POTION,
                Identifier.fromNamespaceAndPath(Makissmpmod.MOD_ID, name),
                potion
        );
    }

    private static void addPotionVariantsToCreativeTab(Holder<Potion> potion) {
        addPotionStackToCreativeTab(Items.POTION, potion);
        addPotionStackToCreativeTab(Items.SPLASH_POTION, potion);
        addPotionStackToCreativeTab(Items.LINGERING_POTION, potion);
    }

    private static void addPotionStackToCreativeTab(Item item, Holder<Potion> potion) {
        ItemStack stack = PotionContents.createItemStack(item, potion);
        ModItems.addItemStackToCreativeModTab(ModItems.MAKIS_TAB_KEY, stack);
    }
}
