package org.example.makismod.makissmpmod;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import org.spongepowered.include.com.google.common.base.Function;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

public class ModItems {
    private static final Set<Item> GENERATED_FLAT_ITEMS = new LinkedHashSet<>();
    private static final MaterialVariant[] DEFAULT_MATERIAL_VARIANTS = new MaterialVariant[] {
            new MaterialVariant("wooden", ToolMaterial.WOOD),
            new MaterialVariant("stone", ToolMaterial.STONE),
            new MaterialVariant("iron", ToolMaterial.IRON),
            new MaterialVariant("golden", ToolMaterial.GOLD),
            new MaterialVariant("diamond", ToolMaterial.DIAMOND),
            new MaterialVariant("netherite", ToolMaterial.NETHERITE)
    };

    public static <T extends Item> T register(String name, Function<Item.Properties, T> itemFactory,
            Item.Properties settings) {
        // Create the item key.
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(Makissmpmod.MOD_ID, name));

        // Create the item instance.
        T item = itemFactory.apply(settings.setId(itemKey));

        // Register the item.
        assert item != null;
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        return item;
    }

    public static <T extends Item> Map<String, T> registerMaterialVariants(String baseName,
            BiFunction<ToolMaterial, Item.Properties, T> itemFactory) {
        Map<String, T> variants = new LinkedHashMap<>();

        for (MaterialVariant variant : DEFAULT_MATERIAL_VARIANTS) {
            String itemName = variant.prefix() + "_" + baseName;
            T item = register(itemName, properties -> itemFactory.apply(variant.tier(), properties),
                    createTieredItemProperties(variant.tier()));
            variants.put(variant.prefix(), item);
            GENERATED_FLAT_ITEMS.add(item);
        }

        return Map.copyOf(variants);
    }

    public static Set<Item> generatedFlatItems() {
        return Set.copyOf(GENERATED_FLAT_ITEMS);
    }

    private static Item.Properties createTieredItemProperties(ToolMaterial tier) {
        Item.Properties properties = new Item.Properties();
        if (tier == ToolMaterial.NETHERITE) {
            properties = properties.fireResistant();
        }
        return properties;
    }

    private record MaterialVariant(String prefix, ToolMaterial tier) {
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                .register((fabricItemGroupEntries -> {
                    assert ModItems.CUSTOM_SHIELD != null;
                    fabricItemGroupEntries.accept(ModItems.CUSTOM_SHIELD);
                    assert ModItems.JUDGE_GAVEL != null;
                    fabricItemGroupEntries.accept(ModItems.JUDGE_GAVEL);
                    assert ModItems.BRONZE_COIN != null;
                    fabricItemGroupEntries.accept(ModItems.BRONZE_COIN);
                    assert ModItems.SILVER_COIN != null;
                    fabricItemGroupEntries.accept(ModItems.SILVER_COIN);
                    assert ModItems.GOLD_COIN != null;
                    fabricItemGroupEntries.accept(ModItems.GOLD_COIN);
                    assert ModItems.DIAMOND_COIN != null;
                    fabricItemGroupEntries.accept(ModItems.DIAMOND_COIN);
                }));
    }

    public static final Item JUDGE_GAVEL = register("judges_gavel",
            properties -> properties != null ? new Item(properties) : null, new Item.Properties());
    public static final Item CUSTOM_SHIELD = register("custom_shield",
            properties -> properties != null ? new CustomShieldItem(properties) : null,
            new Item.Properties()
                    .durability(500)
                    .enchantable(15)
                    .component(net.minecraft.core.component.DataComponents.BLOCKS_ATTACKS,
                            Objects.requireNonNull(Items.SHIELD.components()
                                    .get(DataComponents.BLOCKS_ATTACKS)))
                    .component(net.minecraft.core.component.DataComponents.EQUIPPABLE,
                            Objects.requireNonNull(Items.SHIELD.components()
                                    .get(DataComponents.EQUIPPABLE)))
                    .component(net.minecraft.core.component.DataComponents.USE_EFFECTS,
                            Objects.requireNonNull(Items.SHIELD.components()
                                    .get(DataComponents.USE_EFFECTS)))
                    .component(net.minecraft.core.component.DataComponents.REPAIRABLE,
                            Objects.requireNonNull(Items.SHIELD.components()
                                    .get(DataComponents.REPAIRABLE))));
    public static final Item BRONZE_COIN = register("bronze_coin",
            properties -> properties != null ? new Item(properties) : null, new Item.Properties());
    public static final Item SILVER_COIN = register("silver_coin",
            properties -> properties != null ? new Item(properties) : null, new Item.Properties());
    public static final Item GOLD_COIN = register("gold_coin",
            properties -> properties != null ? new Item(properties) : null, new Item.Properties());
    public static final Item DIAMOND_COIN = register("diamond_coin",
            properties -> properties != null ? new Item(properties) : null, new Item.Properties());
    public static final Map<String, DoryItem> DORYS =
            registerMaterialVariants("dory", DoryItem::new);
}
