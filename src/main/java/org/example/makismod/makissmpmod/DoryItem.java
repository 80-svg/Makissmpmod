package org.example.makismod.makissmpmod;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.AttackRange;

public class DoryItem extends Item {
    private static final float EXTRA_SWING_DURATION = 0.15F;
    private static final AttackRange DORY_ATTACK_RANGE = new AttackRange(2.0F, 5.5F, 2.0F, 7.5F, 0.125F, 0.5F);

    public DoryItem(ToolMaterial material, Item.Properties properties) {
        super(applyDoryProperties(material, properties));
    }

    private static Item.Properties applyDoryProperties(ToolMaterial material, Item.Properties properties) {
        SpearStats stats = spearStats(material);
        return properties
                .spear(material,
                        stats.swingDurationSeconds() + EXTRA_SWING_DURATION,
                        stats.damageMultiplier(),
                        stats.damageDelaySeconds(),
                        stats.dismountWindowSeconds(),
                        stats.dismountMinSpeed(),
                        stats.knockbackWindowSeconds(),
                        stats.knockbackMinSpeed(),
                        stats.damageWindowSeconds(),
                        stats.damageMinRelativeSpeed())
                .component(DataComponents.ATTACK_RANGE, DORY_ATTACK_RANGE);
    }

    private static SpearStats spearStats(ToolMaterial material) {
        if (material == ToolMaterial.WOOD) {
            return new SpearStats(0.65F, 0.7F, 0.75F, 5.0F, 14.0F, 10.0F, 5.1F, 15.0F, 4.6F);
        }
        if (material == ToolMaterial.STONE) {
            return new SpearStats(0.75F, 0.82F, 0.7F, 4.5F, 10.0F, 9.0F, 5.1F, 13.75F, 4.6F);
        }
        if (material == ToolMaterial.IRON) {
            return new SpearStats(0.95F, 0.95F, 0.6F, 2.5F, 8.0F, 6.75F, 5.1F, 11.25F, 4.6F);
        }
        if (material == ToolMaterial.GOLD) {
            return new SpearStats(0.95F, 0.7F, 0.7F, 3.5F, 10.0F, 8.5F, 5.1F, 13.75F, 4.6F);
        }
        if (material == ToolMaterial.DIAMOND) {
            return new SpearStats(1.05F, 1.075F, 0.5F, 3.0F, 7.5F, 6.5F, 5.1F, 10.0F, 4.6F);
        }
        if (material == ToolMaterial.NETHERITE) {
            return new SpearStats(1.15F, 1.2F, 0.4F, 2.5F, 7.0F, 5.5F, 5.1F, 8.75F, 4.6F);
        }
        throw new IllegalArgumentException("Unsupported dory material: " + material);
    }

    private record SpearStats(
            float swingDurationSeconds,
            float damageMultiplier,
            float damageDelaySeconds,
            float dismountWindowSeconds,
            float dismountMinSpeed,
            float knockbackWindowSeconds,
            float knockbackMinSpeed,
            float damageWindowSeconds,
            float damageMinRelativeSpeed) {
    }
}
