package org.example.makismod.makissmpmod;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class FeatherItem extends Item {
    public static final boolean DAMAGE_WHILE_HELD = true;
    public static final int MAX_DURABILITY = 300;
    private static final int DURABILITY_TICK_INTERVAL = 20;
    private static final Set<UUID> featherFlightPlayers = new LinkedHashSet<>();

    public FeatherItem(Properties properties) {
        super(properties);
    }

    public static void tickFeatherFlight(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ItemStack offhandItem = player.getOffhandItem();
            Level level = server.getLevel(Level.OVERWORLD);
            if (!offhandItem.is(ModItems.FEATHER_OF_FLIGHT)) {
                continue;
            }

            featherFlightPlayers.add(player.getUUID());
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }

            if (!DAMAGE_WHILE_HELD || player.tickCount % DURABILITY_TICK_INTERVAL != 0 || !offhandItem.isDamageableItem()) {
                continue;
            }
//            assert level != null;
//            if (level.isClientSide()) {
//                level.addParticle(
//                        ParticleTypes.END_ROD,
//                        player.getX(),
//                        player.getY() + 1,
//                        player.getZ(),
//                        0, 0.1, 0
//                );
//            }
            offhandItem.hurtAndBreak(1, player, EquipmentSlot.OFFHAND);
        }

        Iterator<UUID> iterator = featherFlightPlayers.iterator();
        while (iterator.hasNext()) {
            UUID playerId = iterator.next();
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (player == null) {
                iterator.remove();
                continue;
            }

            if (player.getOffhandItem().is(ModItems.FEATHER_OF_FLIGHT)) {
                continue;
            }

            if (!player.isCreative() && !player.isSpectator() && !player.hasEffect(ModEffects.FLIGHT)) {
                player.getAbilities().mayfly = false;
            }
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
            iterator.remove();
        }
    }
}
