package it.hurts.shatterbyte.reliquified_ars_nouveau.items.head;

import com.hollingsworth.arsnouveau.common.entity.BubbleEntity;
import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.base.RANWearableRelicItem;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.base.loot.LootEntries;
import it.hurts.sskirillss.relics.api.relics.AbilityMetricTemplate;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.VisibilityState;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourceTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class QuantumBubbleItem extends RANWearableRelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("stasis")
                                .rankModifier(1, "blinding_burst")
                                .rankModifier(3, "projectile_direction")
                                .rankModifier(5, "bubble_guard")
                                .stat(AbilityStatTemplate.builder("max_charges")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.06667D)
                                        .formatValue(value -> Math.max(1, (int) MathUtils.round(value, 0)))
                                        .build())
                                .stat(AbilityStatTemplate.builder("charge_regen_time")
                                        .initialValue(30D, 25D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), -0.02286D)
                                        .formatValue(value -> MathUtils.round(value, 2))
                                        .build())
                                .stat(AbilityStatTemplate.builder("protection_radius")
                                        .initialValue(5D, 10D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.04286D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("blinding_radius")
                                        .initialValue(3D, 5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.02857D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("blinding_duration")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.06667D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("projectile_capture").build())
                                        .source(ExperienceSourceTemplate.builder("damage_negated")
                                                .rankModifierVisibilityState("bubble_guard", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("projectiles_captured")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("projectiles_released")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .rankModifierVisibilityState("projectile_direction", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("damage_negated_triggers")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .rankModifierVisibilityState("bubble_guard", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("damage_negated")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .rankModifierVisibilityState("bubble_guard", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .build())
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.ARS_NOUVEAU)
                        .entry(LootEntries.ARS_NOUVEAU_LIKE)
                        .build())
                .build();
    }

    @EventBusSubscriber(modid = ReliquifiedArsNouveau.MODID)
    public static class CommonEvents {
        private static final String PROJECTILE_CAPTURED_TAG = "ran_quantum_bubble_captured";
        private static final String PROJECTILE_RELEASE_TICK_TAG = "ran_quantum_bubble_release_tick";
        private static final String PROJECTILE_RELEASED_TAG = "ran_quantum_bubble_released";
        private static final String PROJECTILE_ORIGINAL_OWNER_TAG = "ran_quantum_bubble_original_owner";
        private static final String PROJECTILE_CAPTURE_PLAYER_TAG = "ran_quantum_bubble_capture_player";
        private static final String PROJECTILE_CAPTURED_SPEED_TAG = "ran_quantum_bubble_captured_speed";

        @SubscribeEvent
        public static void onPlayerTickPost(PlayerTickEvent.Post event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide())
                return;

            var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.QUANTUM_BUBBLE.get()).stream()
                    .filter(relicStack -> relicStack.getItem() instanceof QuantumBubbleItem)
                    .toList();

            if (stacks.isEmpty())
                return;

            var level = player.serverLevel();
            for (var stack : stacks) {
                if (!(stack.getItem() instanceof QuantumBubbleItem item))
                    continue;

                var relicData = item.getRelicData(player, stack);
                var ability = relicData.getAbilitiesData().getAbilityData("stasis");

                var maxCharges = Math.max(1, (int) Math.round(Math.max(0D, ability.getStatData("max_charges").getValue())));
                var charges = Mth.clamp(stack.getOrDefault(RANDataComponentRegistry.QUANTUM_BUBBLE_CHARGES.get(), maxCharges), 0, maxCharges);
                var regenIntervalTicks = Math.max(1, (int) Math.round(Math.max(0.05D, ability.getStatData("charge_regen_time").getValue()) * 20D));
                var regenTicks = Math.max(0, stack.getOrDefault(RANDataComponentRegistry.QUANTUM_BUBBLE_REGEN_TICKS.get(), 0));

                if (charges < maxCharges) {
                    regenTicks++;

                    while (regenTicks >= regenIntervalTicks && charges < maxCharges) {
                        charges++;
                        regenTicks -= regenIntervalTicks;
                    }
                } else {
                    regenTicks = 0;
                }

                var speedBonusMultiplier = 2D;
                var bubbleCheckRadius = Math.max(2.75D, Math.max(0D, ability.getStatData("protection_radius").getValue())) + 6D;

                for (var bubble : level.getEntitiesOfClass(BubbleEntity.class, player.getBoundingBox().inflate(bubbleCheckRadius),
                        target -> {
                            if (!(target.getFirstPassenger() instanceof Projectile projectile))
                                return false;

                            var projectileData = projectile.getPersistentData();

                            return projectileData.contains(PROJECTILE_CAPTURE_PLAYER_TAG) && projectileData.getUUID(PROJECTILE_CAPTURE_PLAYER_TAG).equals(player.getUUID());
                        })) {
                    if (!(bubble.getFirstPassenger() instanceof Projectile projectile))
                        continue;

                    var projectileData = projectile.getPersistentData();

                    if (!projectileData.getBoolean(PROJECTILE_CAPTURED_TAG))
                        continue;

                    if (projectileData.getBoolean(PROJECTILE_RELEASED_TAG))
                        continue;

                    var shouldRelease = bubble.getEntityData().get(BubbleEntity.HAS_POPPED)
                            || projectileData.contains(PROJECTILE_RELEASE_TICK_TAG) && level.getGameTime() >= projectileData.getLong(PROJECTILE_RELEASE_TICK_TAG);

                    if (!shouldRelease)
                        continue;

                    projectileData.putBoolean(PROJECTILE_RELEASED_TAG, true);
                    projectile.stopRiding();

                    var releaseDirection = Vec3.ZERO;
                    var redirected = false;

                    if (!ability.getRankModifierData("projectile_direction").isEnabled()) {
                        releaseDirection = new Vec3(0D, -1D, 0D);
                    } else {
                        var originalOwner = projectileData.contains(PROJECTILE_ORIGINAL_OWNER_TAG)
                                ? level.getEntity(projectileData.getUUID(PROJECTILE_ORIGINAL_OWNER_TAG))
                                : projectile.getOwner();

                        if (originalOwner != null && originalOwner.isAlive()) {
                            releaseDirection = originalOwner.getEyePosition().subtract(projectile.position());
                            redirected = releaseDirection.lengthSqr() > 1.0E-6D;
                        }

                        if (!redirected)
                            releaseDirection = new Vec3(0D, -1D, 0D);
                    }

                    var capturedSpeed = projectileData.contains(PROJECTILE_CAPTURED_SPEED_TAG)
                            ? projectileData.getDouble(PROJECTILE_CAPTURED_SPEED_TAG)
                            : projectile.getDeltaMovement().length();

                    if (ability.getRankModifierData("projectile_direction").isEnabled())
                        projectile.setOwner(player);

                    projectile.setDeltaMovement(releaseDirection.normalize().scale(Math.max(0.2D, capturedSpeed) * speedBonusMultiplier));
                    projectile.hurtMarked = true;

                    if (ability.getRankModifierData("blinding_burst").isEnabled()) {
                        var blindingRadius = Math.max(0D, ability.getStatData("blinding_radius").getValue());
                        var blindingDurationTicks = Math.max(1, (int) Math.round(Math.max(0D, ability.getStatData("blinding_duration").getValue()) * 20D));

                        if (blindingRadius > 0D && blindingDurationTicks > 0) {
                            for (var target : level.getEntitiesOfClass(LivingEntity.class, bubble.getBoundingBox().inflate(blindingRadius),
                                    target -> target.isAlive() && target != player)) {
                                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, blindingDurationTicks), player);

                                if (target instanceof Mob mob && mob.getTarget() == player)
                                    mob.setTarget(null);
                            }
                        }
                    }

                    bubble.pop();

                    if (redirected)
                        ability.getStatisticData().getMetricData("projectiles_released").addValue(1D);
                }

                if (charges > 0) {
                    var knownMovement = player.getKnownMovement();
                    var searchBox = player.getBoundingBox().inflate(2.75D);

                    if (knownMovement.lengthSqr() > 1.0E-6D)
                        searchBox = searchBox.expandTowards(knownMovement.normalize().scale(1.5D));

                    for (var projectile : level.getEntitiesOfClass(Projectile.class, searchBox, target -> {
                        if (target instanceof BubbleEntity || target.getVehicle() != null)
                            return false;

                        if (target.getPersistentData().getBoolean(PROJECTILE_CAPTURED_TAG))
                            return false;

                        if (target.getOwner() != null && target.getOwner().getUUID().equals(player.getUUID()))
                            return false;

                        if (target instanceof AbstractArrow arrow && arrow.inGround)
                            return false;

                        return target.isAlive();
                    })) {
                        if (charges <= 0)
                            break;

                        var toPlayer = player.position().subtract(projectile.position());
                        var motion = projectile.getDeltaMovement();

                        if (toPlayer.lengthSqr() > 1.0E-4D && motion.lengthSqr() > 1.0E-6D && motion.normalize().dot(toPlayer.normalize()) < 0.1D)
                            continue;

                        var bubble = new BubbleEntity(level, 80 + 20, 0F);
                        var projectileData = projectile.getPersistentData();
                        var captureMotion = projectile.getDeltaMovement();
                        var originalOwner = projectile.getOwner();

                        bubble.setPos(projectile.getX(), projectile.getY(), projectile.getZ());

                        if (originalOwner != null)
                            projectileData.putUUID(PROJECTILE_ORIGINAL_OWNER_TAG, originalOwner.getUUID());
                        else
                            projectileData.remove(PROJECTILE_ORIGINAL_OWNER_TAG);

                        projectileData.putDouble(PROJECTILE_CAPTURED_SPEED_TAG, captureMotion.length());
                        projectileData.putUUID(PROJECTILE_CAPTURE_PLAYER_TAG, player.getUUID());

                        if (ability.getRankModifierData("projectile_direction").isEnabled())
                            projectile.setOwner(player);

                        projectile.setPos(bubble.position());
                        projectile.startRiding(bubble, true);
                        projectileData.putBoolean(PROJECTILE_CAPTURED_TAG, true);
                        projectileData.putLong(PROJECTILE_RELEASE_TICK_TAG, level.getGameTime() + 80);
                        projectileData.putBoolean(PROJECTILE_RELEASED_TAG, false);

                        level.addFreshEntity(bubble);

                        charges--;
                        regenTicks = 0;
                        relicData.getLevelingData().addExperience("stasis", "projectile_capture", 1D);
                        ability.getStatisticData().getMetricData("projectiles_captured").addValue(1D);
                    }
                }

                stack.set(RANDataComponentRegistry.QUANTUM_BUBBLE_CHARGES.get(), charges);
                stack.set(RANDataComponentRegistry.QUANTUM_BUBBLE_REGEN_TICKS.get(), regenTicks);
            }
        }

        @SubscribeEvent
        public static void onLivingDamagePre(LivingIncomingDamageEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || event.getAmount() <= 0F)
                return;

            var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.QUANTUM_BUBBLE.get()).stream()
                    .filter(relicStack -> relicStack.getItem() instanceof QuantumBubbleItem)
                    .toList();

            if (stacks.isEmpty())
                return;

            for (var stack : stacks) {
                if (!(stack.getItem() instanceof QuantumBubbleItem item))
                    continue;

                var relicData = item.getRelicData(player, stack);
                var ability = relicData.getAbilitiesData().getAbilityData("stasis");

                if (!ability.getRankModifierData("bubble_guard").isEnabled())
                    continue;

                var protectionRadius = Math.max(0D, ability.getStatData("protection_radius").getValue());

                if (protectionRadius <= 0D)
                    continue;

                var bubble = player.serverLevel().getEntitiesOfClass(BubbleEntity.class, player.getBoundingBox().inflate(protectionRadius),
                                target -> {
                                    if (target.getEntityData().get(BubbleEntity.HAS_POPPED))
                                        return false;

                                    if (!(target.getFirstPassenger() instanceof Projectile projectile))
                                        return false;

                                    var projectileData = projectile.getPersistentData();
                                    var matchesTag = projectileData.contains(PROJECTILE_CAPTURE_PLAYER_TAG) && projectileData.getUUID(PROJECTILE_CAPTURE_PLAYER_TAG).equals(player.getUUID());

                                    return projectileData.getBoolean(PROJECTILE_CAPTURED_TAG) && matchesTag;
                                })
                        .stream()
                        .findFirst()
                        .orElse(null);

                if (bubble == null)
                    continue;

                var negatedDamage = Math.max(0D, event.getAmount());

                event.setCanceled(true);
                bubble.pop();
                relicData.getLevelingData().addExperience("stasis", "damage_negated", 1D);
                ability.getStatisticData().getMetricData("damage_negated").addValue(negatedDamage);
                ability.getStatisticData().getMetricData("damage_negated_triggers").addValue(1D);
                return;
            }
        }
    }

}

