package org.example.makismod.makissmpmod;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CustomShieldItem extends ShieldItem {
    private static final int BLOCK_ATTACK_COOLDOWN_TICKS = 200;
    private static final int HOTUP_COOLDOWN_REDUCTION_PER_LEVEL = 40;
    private static final int MIN_BLOCK_ATTACK_COOLDOWN_TICKS = 80;
    private static final int DASH_DURATION_TICKS = 6;
    private static final int SHIELD_DURABILITY_COST = 2;
    private static final int SPEAR_DURABILITY_COST = 2;
    private static final float DASH_DAMAGE = 16.0F;
    private static final double DASH_SPEED = 3.25D;
    private static final double DASH_VERTICAL_BOOST = 0.18D;
    private static final double DASH_REACH = 1.75D;
    private static final double MAX_UPWARD_DISTANCE_SCALE = 0.25D;
    private static final Map<UUID, ActiveDash> ACTIVE_DASHES = new ConcurrentHashMap<>();

    public CustomShieldItem(Properties properties) {
        super(properties);
    }

    public static boolean hasRequiredComboItems(Player player) {
        ItemStack offhandStack = player.getOffhandItem();
        ItemStack mainHandStack = player.getMainHandItem();

        return offhandStack.getItem() instanceof CustomShieldItem
                && mainHandStack.getItem() instanceof DoryItem;
    }

    public static boolean isBlockingComboReady(Player player) {
        return hasRequiredComboItems(player)
                && player.isUsingItem()
                && player.getUseItem().getItem() instanceof CustomShieldItem
                && player.getUsedItemHand() == InteractionHand.OFF_HAND;
    }

    public static void tryStartDash(ServerPlayer player) {
        if (!isBlockingComboReady(player)) {
            return;
        }

        ItemStack shieldStack = player.getUseItem();
        if (player.getCooldowns().isOnCooldown(shieldStack)) {
            return;
        }

        ((CustomShieldItem) shieldStack.getItem()).startDash(player.level(), player, shieldStack);
    }

    private void startDash(ServerLevel level, ServerPlayer player, ItemStack shieldStack) {
        ItemStack spearStack = player.getMainHandItem();
        int cooldownTicks = getDashCooldownTicks(level, shieldStack);

        player.getCooldowns().addCooldown(shieldStack, cooldownTicks);
        shieldStack.hurtAndBreak(SHIELD_DURABILITY_COST, player, EquipmentSlot.OFFHAND);
        spearStack.hurtAndBreak(SPEAR_DURABILITY_COST, player, EquipmentSlot.MAINHAND);

        Vec3 look = player.getLookAngle().normalize();
        Vec3 dashVelocity = createDashVelocity(look, 1.0D);
        applyDashVelocity(player, dashVelocity);
        ACTIVE_DASHES.put(player.getUUID(), new ActiveDash(look, DASH_DURATION_TICKS, new HashSet<>()));
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SPEAR_ATTACK,
                SoundSource.PLAYERS, 1.0F, 1.0F);
        player.sendSystemMessage(Component.literal("Shield dash triggered."));
    }

    private static int getDashCooldownTicks(ServerLevel level, ItemStack shieldStack) {
        Holder<Enchantment> hotup = level.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(ModEnchantments.HOTUP);
        int hotupLevel = EnchantmentHelper.getItemEnchantmentLevel(hotup, shieldStack);
        return Math.max(MIN_BLOCK_ATTACK_COOLDOWN_TICKS,
                BLOCK_ATTACK_COOLDOWN_TICKS - hotupLevel * HOTUP_COOLDOWN_REDUCTION_PER_LEVEL);
    }

    public static void tickActiveDashes(MinecraftServer server) {
        Iterator<Map.Entry<UUID, ActiveDash>> iterator = ACTIVE_DASHES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, ActiveDash> entry = iterator.next();
            UUID playerId = entry.getKey();
            ActiveDash activeDash = entry.getValue();
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);

            if (player == null || !player.isAlive() || !hasRequiredComboItems(player)) {
                iterator.remove();
                continue;
            }

            Vec3 dashVelocity = createDashVelocity(activeDash.direction(), 0.35D);
            applyDashVelocity(player, dashVelocity);
            damageDashTargets(player.level(), player, activeDash);
            activeDash.ticksRemaining--;

            if (activeDash.ticksRemaining <= 0) {
                iterator.remove();
            }
        }
    }

    private static void damageDashTargets(ServerLevel level, Player player, ActiveDash activeDash) {
        Vec3 look = activeDash.direction();
        AABB hitbox = player.getBoundingBox().expandTowards(look.scale(DASH_REACH)).inflate(1.0D);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, hitbox,
                entity -> entity != player
                        && entity.isAlive()
                        && entity.attackable()
                        && !activeDash.hitEntities().contains(entity.getUUID()));

        targets.stream()
                .sorted(Comparator.comparingDouble(player::distanceToSqr))
                .forEach(target -> {
                    target.hurtServer(level, player.damageSources().playerAttack(player), DASH_DAMAGE);
                    target.knockback(1.5D, -look.x, -look.z);
                    activeDash.hitEntities().add(target.getUUID());
                });
    }

    private static void applyDashVelocity(ServerPlayer player, Vec3 dashVelocity) {
        player.setDeltaMovement(dashVelocity);
        player.lerpMotion(dashVelocity);
        player.hurtMarked = true;
        player.connection.send(new ClientboundSetEntityMotionPacket(player));
    }

    private static Vec3 createDashVelocity(Vec3 look, double verticalBoostScale) {
        double upwardAim = Math.max(look.y, 0.0D);
        double distanceScale = 1.0D - ((1.0D - MAX_UPWARD_DISTANCE_SCALE) * upwardAim);
        return look.scale(DASH_SPEED * distanceScale).add(0.0D, DASH_VERTICAL_BOOST * verticalBoostScale, 0.0D);
    }

    private static final class ActiveDash {
        private final Vec3 direction;
        private int ticksRemaining;
        private final Set<UUID> hitEntities;

        private ActiveDash(Vec3 direction, int ticksRemaining, Set<UUID> hitEntities) {
            this.direction = direction;
            this.ticksRemaining = ticksRemaining;
            this.hitEntities = hitEntities;
        }

        private Vec3 direction() {
            return this.direction;
        }

        private Set<UUID> hitEntities() {
            return this.hitEntities;
        }
    }
}
