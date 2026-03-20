package org.example.makismod.makissmpmod.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class DisableFortune {
    @Inject(method = "getEnchantmentLevel", at = @At("HEAD"), cancellable = true)
    private static void getEnchantmentLevel(Holder<Enchantment> holder, LivingEntity livingEntity, CallbackInfoReturnable<Integer> cir) {

        if (holder.is(Enchantments.FORTUNE)) {
            cir.setReturnValue(0);
        }
    }
    @Inject(method = "getItemEnchantmentLevel", at = @At("HEAD"), cancellable = true)
    private static void getItemEnchantmentLevel(Holder<Enchantment> holder, ItemStack itemStack, CallbackInfoReturnable<Integer> cir) {
        if (holder.is(Enchantments.FORTUNE)) {
            cir.setReturnValue(0);
        }
    }
}
