package org.example.makismod.makissmpmod;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.example.makismod.makissmpmod.entity.custom.EpsteinEntity;

public class ModEntityTypes {
    public static final EntityType<EpsteinEntity> EPSTEIN_ENTITY = register(
            "epstein",
            EntityType.Builder.<EpsteinEntity>of(EpsteinEntity::new, MobCategory.CREATURE)
                    .sized(0.75f, 1.75f)
    );

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Makissmpmod.MOD_ID, name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }
    public static void initialize() {
        Makissmpmod.LOGGER.info("Registering entity types");
    }
    public static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(EPSTEIN_ENTITY, EpsteinEntity.createAttributes());
    }
}
