package org.example.makismod.makissmpmod.entity.custom;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.example.makismod.makissmpmod.ModItems;

public class EpsteinEntity extends PathfinderMob {
//    public EpsteinEntity(Level level) {
//        this(ModEntitiesTypes)
//    }
    public EpsteinEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.TEMPT_RANGE, 100)
                .add(Attributes.MOVEMENT_SPEED, 5.0D);
    }
    protected void registerGoals() {
        assert ModItems.BABY_OIL != null;
        this.goalSelector.addGoal(0, new TemptGoal(this, 1, Ingredient.of(ModItems.BABY_OIL), false));
        this.goalSelector.addGoal(1, new RandomStrollGoal(this, 1));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }
}
