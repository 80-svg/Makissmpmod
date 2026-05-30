package org.example.makismod.makissmpmod.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.example.makismod.makissmpmod.ModAttachments;
import org.example.makismod.makissmpmod.ModEffects;
import org.example.makismod.makissmpmod.commands.CurseCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class RemoveEffectsOnMilk {
    @Unique
    private MobEffectInstance makissmpmod$preservedWaxCoated;

    @Inject(method = "removeAllEffects", at = @At("HEAD"))
    private void preserveWaxCoatedBeforeMilk(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        ItemStack useItem = livingEntity.getUseItem();
        Holder<MobEffect> effect = CurseCommand.effectFromId(livingEntity.getAttachedOrElse(ModAttachments.CURSE_EFFECTS, null));
        if (!useItem.is(Items.MILK_BUCKET) || !livingEntity.hasEffect(effect)) {
            this.makissmpmod$preservedWaxCoated = null;
            return;
        }

        MobEffectInstance waxCoated = livingEntity.getEffect(ModEffects.WAX_COATED);
        this.makissmpmod$preservedWaxCoated = waxCoated == null ? null : new MobEffectInstance(waxCoated);
    }

    @Inject(method = "removeAllEffects", at = @At("RETURN"))
    private void restoreWaxCoatedAfterMilk(CallbackInfoReturnable<Boolean> cir) {
        if (this.makissmpmod$preservedWaxCoated == null) {
            return;
        }

        LivingEntity livingEntity = (LivingEntity) (Object) this;
        livingEntity.addEffect(this.makissmpmod$preservedWaxCoated);
        this.makissmpmod$preservedWaxCoated = null;
    }
}
