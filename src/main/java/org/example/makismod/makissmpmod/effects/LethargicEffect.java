package org.example.makismod.makissmpmod.effects;

import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.example.makismod.makissmpmod.Makissmpmod;

public class LethargicEffect extends MobEffect {
    private static final Identifier LETHARGY_SPEED_MODIFIER_ID =
            Identifier.fromNamespaceAndPath(Makissmpmod.MOD_ID, "lethargy_speed");
    public LethargicEffect() {
        super(MobEffectCategory.HARMFUL, 0x87CEFA);
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                LETHARGY_SPEED_MODIFIER_ID,
                -0.5D,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }
}
