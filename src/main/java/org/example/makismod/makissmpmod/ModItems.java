package org.example.makismod.makissmpmod;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import org.spongepowered.include.com.google.common.base.Function;

import java.util.*;
import java.util.function.BiFunction;

public class ModItems {
    private static final Set<Item> GENERATED_FLAT_ITEMS = new LinkedHashSet<>();
    public static final ResourceKey<CreativeModeTab> MAKIS_TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(Makissmpmod.MOD_ID, "makis_tab"));
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

    public static <T extends Item> Map<String, T> registerMaterialVariants(ResourceKey<CreativeModeTab> cmt, String baseName,
            BiFunction<ToolMaterial, Item.Properties, T> itemFactory) {
        return registerMaterialVariants(cmt, baseName, itemFactory, true);
    }

    public static <T extends Item> Map<String, T> registerMaterialVariants(ResourceKey<CreativeModeTab> cmt,
            String baseName, BiFunction<ToolMaterial, Item.Properties, T> itemFactory, boolean generateFlatModel) {
        Map<String, T> variants = new LinkedHashMap<>();

        for (MaterialVariant variant : DEFAULT_MATERIAL_VARIANTS) {
            String itemName = variant.prefix() + "_" + baseName;
            T item = register(itemName, properties -> itemFactory.apply(variant.tier(), properties),
                    createTieredItemProperties(variant.tier()));
            variants.put(variant.prefix(), item);
            if (generateFlatModel) {
                GENERATED_FLAT_ITEMS.add(item);
            }
            addItemsToCreativeModTab(cmt, item);
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

    public static void addItemsToCreativeModTab(ResourceKey<CreativeModeTab> tab, Item item) {
        ItemGroupEvents.modifyEntriesEvent(tab)
                .register(fabricItemGroupEntries -> fabricItemGroupEntries.accept(item));
    }

    private record MaterialVariant(String prefix, ToolMaterial tier) {
    }

    public static void initialize() {
        addItemsToCreativeModTab(MAKIS_TAB_KEY, ModItems.CUSTOM_SHIELD);
        addItemsToCreativeModTab(MAKIS_TAB_KEY, ModItems.JUDGE_GAVEL);
        addItemsToCreativeModTab(MAKIS_TAB_KEY, ModItems.BRONZE_COIN);
        addItemsToCreativeModTab(MAKIS_TAB_KEY, ModItems.SILVER_COIN);
        addItemsToCreativeModTab(MAKIS_TAB_KEY, ModItems.GOLD_COIN);
        addItemsToCreativeModTab(MAKIS_TAB_KEY, ModItems.DIAMOND_COIN);
        addItemsToCreativeModTab(MAKIS_TAB_KEY, ModItems.ICARUS_WINGS);
    }
    public static final DataComponentType<Float> ICARUS_PERCENT = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath("makissmpmod", "icarus_percent"),
            DataComponentType.<Float>builder()
                    .persistent(Codec.floatRange(0, 1))
                    .build()
    );
    public static final Item JUDGE_GAVEL = register("judges_gavel",
            properties -> properties != null ? new Item(properties) : null, new Item.Properties());
    public static final Item CUSTOM_SHIELD = register("custom_shield",
            properties -> properties != null ? new CustomShieldItem(properties) : null,
            new Item.Properties()
                    .durability(500)
                    .enchantable(15)
                    .component(DataComponents.BLOCKS_ATTACKS,
                            Objects.requireNonNull(Items.SHIELD.components()
                                    .get(DataComponents.BLOCKS_ATTACKS)))
                    .component(DataComponents.EQUIPPABLE,
                            Objects.requireNonNull(Items.SHIELD.components()
                                    .get(DataComponents.EQUIPPABLE)))
                    .component(DataComponents.USE_EFFECTS,
                            Objects.requireNonNull(Items.SHIELD.components()
                                    .get(DataComponents.USE_EFFECTS)))
                    .component(DataComponents.REPAIRABLE,
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
            registerMaterialVariants(MAKIS_TAB_KEY, "dory", DoryItem::new, false);
    public static final Item ICARUS_WINGS = register("icarus_wings",
            properties -> properties != null ? new Item(properties) : null,
            new Item.Properties()
                    .durability(432)
                    .component(DataComponents.EQUIPPABLE,
                            Objects.requireNonNull(Items.ELYTRA.components()
                                    .get(DataComponents.EQUIPPABLE)))
                    .component(DataComponents.GLIDER,
                            Objects.requireNonNull(Items.ELYTRA.components()
                                    .get(DataComponents.GLIDER)))
                    .component(DataComponents.REPAIRABLE,
                            Objects.requireNonNull(Items.ELYTRA.components()
                                    .get(DataComponents.REPAIRABLE))));
    public static final CreativeModeTab MAKIS_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, MAKIS_TAB_KEY,
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup." + Makissmpmod.MOD_ID))
                    .icon(() -> {
                        assert ModItems.CUSTOM_SHIELD != null;
                        return new ItemStack(ModItems.CUSTOM_SHIELD);
                    })
                    .displayItems((parameters, output) -> {
                    })
                    .build());

}
