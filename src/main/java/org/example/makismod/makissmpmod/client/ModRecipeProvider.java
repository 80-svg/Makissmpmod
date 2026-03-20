package org.example.makismod.makissmpmod.client;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.example.makismod.makissmpmod.ModItems;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registryLookup, RecipeOutput exporter) {
        return new RecipeProvider(registryLookup, exporter) {
            @Override
            public void buildRecipes() {
                HolderGetter<Item> itemGetter = registryLookup.lookupOrThrow(Registries.ITEM);
                offerCoinConversion(itemGetter, ModItems.BRONZE_COIN, ModItems.SILVER_COIN);
                offerCoinConversion(itemGetter, ModItems.SILVER_COIN, ModItems.GOLD_COIN);
                offerCoinConversion(itemGetter, ModItems.GOLD_COIN, ModItems.DIAMOND_COIN);
            }

            private void offerCoinConversion(HolderGetter<Item> itemGetter, Item lowerCoin, Item higherCoin) {
                // Lower to Higher
                ShapelessRecipeBuilder.shapeless(itemGetter, RecipeCategory.MISC, higherCoin)
                        .requires(lowerCoin, 2)
                        .unlockedBy(getHasName(lowerCoin), has(lowerCoin))
                        .save(exporter, "makissmpmod:" + getItemName(higherCoin) + "_from_" + getItemName(lowerCoin));

                // Higher to Lower
                ShapelessRecipeBuilder.shapeless(itemGetter, RecipeCategory.MISC, new ItemStack(lowerCoin, 2))
                        .requires(higherCoin, 1)
                        .unlockedBy(getHasName(higherCoin), has(higherCoin))
                        .save(exporter, "makissmpmod:" + getItemName(lowerCoin) + "_from_" + getItemName(higherCoin));
            }
        };
    }

    @Override
    public String getName() {
        return "Makissmpmod Recipes";
    }
}
