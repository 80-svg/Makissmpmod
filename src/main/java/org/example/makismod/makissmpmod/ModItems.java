package org.example.makismod.makissmpmod;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import org.spongepowered.include.com.google.common.base.Function;

public class ModItems {
    public static <T extends Item> T register(String name, Function<Item.Properties, T> itemFactory, Item.Properties settings) {
        // Create the item key.
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Makissmpmod.MOD_ID, name));

        // Create the item instance.
        T item = itemFactory.apply(settings.setId(itemKey));

        // Register the item.
        assert item != null;
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        return item;
    }
    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                .register((fabricItemGroupEntries -> {
                    assert ModItems.JUDGE_GAVEL != null;
                    fabricItemGroupEntries.accept(ModItems.JUDGE_GAVEL);
                    assert ModItems.CUSTOM_SHIELD != null;
                    fabricItemGroupEntries.accept(ModItems.CUSTOM_SHIELD);
                }));
    }
    public static final Item JUDGE_GAVEL = register("judges_gavel", properties -> properties != null ? new Item(properties) : null, new Item.Properties());
    public static final Item CUSTOM_SHIELD = register("custom_shield", 
        properties -> properties != null ? new net.minecraft.world.item.ShieldItem(properties) : null, 
        new Item.Properties()
            .durability(500)
            .component(net.minecraft.core.component.DataComponents.BLOCKS_ATTACKS, net.minecraft.world.item.Items.SHIELD.components().get(net.minecraft.core.component.DataComponents.BLOCKS_ATTACKS))
            .component(net.minecraft.core.component.DataComponents.EQUIPPABLE, net.minecraft.world.item.Items.SHIELD.components().get(net.minecraft.core.component.DataComponents.EQUIPPABLE))
            .component(net.minecraft.core.component.DataComponents.USE_EFFECTS, net.minecraft.world.item.Items.SHIELD.components().get(net.minecraft.core.component.DataComponents.USE_EFFECTS))
            .component(net.minecraft.core.component.DataComponents.REPAIRABLE, net.minecraft.world.item.Items.SHIELD.components().get(net.minecraft.core.component.DataComponents.REPAIRABLE))
    );
}
