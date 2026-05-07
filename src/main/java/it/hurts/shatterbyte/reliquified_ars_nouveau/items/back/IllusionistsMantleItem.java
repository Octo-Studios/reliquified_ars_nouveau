package it.hurts.shatterbyte.reliquified_ars_nouveau.items.back;

import com.hollingsworth.arsnouveau.common.entity.EntityDummy;
import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.ItemRegistry;
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
import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.Comparator;
import java.util.UUID;

public class IllusionistsMantleItem extends RANWearableRelicItem {
    private static final String MANTLE_ILLUSION_TAG = "ran_illusionists_mantle";
    private static final String MANTLE_TARGET_TAG = "ran_illusionists_mantle_target";

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("deception")
                                .rankModifier(1, "illusion_barrier")
                                .rankModifier(3, "paralyzing_revenge")
                                .rankModifier(5, "fatal_exchange")
                                .stat(AbilityStatTemplate.builder("trigger_chance")
                                        .initialValue(0.08D, 0.18D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("illusion_duration")
                                        .initialValue(5D, 10D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("illusion_radius")
                                        .initialValue(6D, 10D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.04762D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("damage_reduction_per_illusion")
                                        .initialValue(0.03D, 0.08D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.04762D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("paralysis_duration")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.04762D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("illusion_spawned").build())
                                        .source(ExperienceSourceTemplate.builder("damage_reduced")
                                                .rankModifierVisibilityState("illusion_barrier", VisibilityState.OBFUSCATED)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("illusion_killed")
                                                .rankModifierVisibilityState("paralyzing_revenge", VisibilityState.OBFUSCATED)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("fatal_saved")
                                                .rankModifierVisibilityState("fatal_exchange", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("illusions_spawned")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("damage_reduced")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .rankModifierVisibilityState("illusion_barrier", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("paralysis_time")
                                                .formatValue(value -> MathUtils.formatTime((int) MathUtils.round(value, 0)))
                                                .rankModifierVisibilityState("paralyzing_revenge", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("fatal_saves")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .rankModifierVisibilityState("fatal_exchange", VisibilityState.OBFUSCATED)
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
        @SubscribeEvent
        public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || event.getAmount() <= 0F)
                return;

            var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.ILLUSIONISTS_MANTLE.get()).stream()
                    .filter(relicStack -> relicStack.getItem() instanceof IllusionistsMantleItem)
                    .toList();

            if (stacks.isEmpty())
                return;

            var incomingDamage = Math.max(0D, event.getAmount());
            var attacker = event.getSource().getEntity();

            for (var stack : stacks) {
                if (!(stack.getItem() instanceof IllusionistsMantleItem item))
                    continue;

                var relicData = item.getRelicData(player, stack);
                var ability = relicData.getAbilitiesData().getAbilityData("deception");

                if (ability.getRankModifierData("illusion_barrier").isEnabled()) {
                    var radius = Math.max(0D, ability.getStatData("illusion_radius").getValue());

                    if (radius > 0D) {
                        var illusionCount = player.level().getEntitiesOfClass(EntityDummy.class, player.getBoundingBox().inflate(radius),
                                dummy -> dummy.isAlive()
                                        && dummy.getPersistentData().getBoolean(MANTLE_ILLUSION_TAG)
                                        && player.getUUID().equals(dummy.getOwnerUUID()))
                                .size();

                        if (illusionCount > 0) {
                            var reductionPerIllusion = Math.max(0D, ability.getStatData("damage_reduction_per_illusion").getValue());
                            var reduction = Mth.clamp(reductionPerIllusion * illusionCount, 0D, 0.95D);
                            var reduced = incomingDamage * reduction;

                            if (reduced > 0D) {
                                incomingDamage = Math.max(0D, incomingDamage - reduced);
                                relicData.getLevelingData().addExperience("deception", "damage_reduced", reduced);
                                ability.getStatisticData().getMetricData("damage_reduced").addValue(reduced);
                            }
                        }
                    }
                }

                if (incomingDamage > 0D && attacker instanceof LivingEntity livingAttacker && livingAttacker.isAlive() && livingAttacker != player) {
                    var chance = Mth.clamp(ability.getStatData("trigger_chance").getValue(), 0D, 1D);

                    if (chance > 0D && player.getRandom().nextDouble() < chance) {
                        var durationTicks = Math.max(20, (int) Math.round(Math.max(0D, ability.getStatData("illusion_duration").getValue()) * 20D));
                        var backDir = livingAttacker.getLookAngle().normalize().scale(-1.5D);
                        var illusion = new EntityDummy(player.level());

                        illusion.setPos(livingAttacker.getX() + backDir.x, livingAttacker.getY(), livingAttacker.getZ() + backDir.z);
                        illusion.setOwnerID(player.getUUID());
                        illusion.ticksLeft = durationTicks;
                        illusion.setHealth(1F);
                        illusion.getPersistentData().putBoolean(MANTLE_ILLUSION_TAG, true);
                        illusion.getPersistentData().putUUID(MANTLE_TARGET_TAG, livingAttacker.getUUID());
                        player.level().addFreshEntity(illusion);
                        player.level().getEntitiesOfClass(Mob.class, illusion.getBoundingBox().inflate(20D, 10D, 20D),
                                mob -> mob.isAlive() && mob.getTarget() == player).forEach(mob -> mob.setTarget(illusion));

                        relicData.getLevelingData().addExperience("deception", "illusion_spawned", 1D);
                        ability.getStatisticData().getMetricData("illusions_spawned").addValue(1D);
                    }
                }
            }

            event.setAmount((float) Math.max(0D, incomingDamage));
        }

        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            if (event.getEntity() instanceof EntityDummy dummy && !dummy.level().isClientSide() && dummy.getPersistentData().getBoolean(MANTLE_ILLUSION_TAG) && dummy.getPersistentData().hasUUID(MANTLE_TARGET_TAG)) {
                var target = ((net.minecraft.server.level.ServerLevel) dummy.level()).getEntity(dummy.getPersistentData().getUUID(MANTLE_TARGET_TAG));

                if (!(target instanceof LivingEntity livingTarget) || !livingTarget.isAlive())
                    return;

                var owner = dummy.getOwnerUUID() == null ? null : dummy.level().getPlayerByUUID(dummy.getOwnerUUID());

                if (!(owner instanceof ServerPlayer player))
                    return;

                var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.ILLUSIONISTS_MANTLE.get()).stream()
                        .filter(relicStack -> relicStack.getItem() instanceof IllusionistsMantleItem)
                        .toList();

                if (stacks.isEmpty())
                    return;

                for (var stack : stacks) {
                    if (!(stack.getItem() instanceof IllusionistsMantleItem item))
                        continue;

                    var relicData = item.getRelicData(player, stack);
                    var ability = relicData.getAbilitiesData().getAbilityData("deception");

                    if (!ability.getRankModifierData("paralyzing_revenge").isEnabled())
                        continue;

                    var paralysisTicks = Math.max(1, (int) Math.round(Math.max(0D, ability.getStatData("paralysis_duration").getValue()) * 20D));

                    livingTarget.addEffect(new MobEffectInstance(RelicsMobEffects.PARALYSIS, paralysisTicks, 0, false, true, true), player);
                    relicData.getLevelingData().addExperience("deception", "illusion_killed", 1D);
                    ability.getStatisticData().getMetricData("paralysis_time").addValue(paralysisTicks / 20D);
                }

                return;
            }

            if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide())
                return;

            var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.ILLUSIONISTS_MANTLE.get()).stream()
                    .filter(relicStack -> relicStack.getItem() instanceof IllusionistsMantleItem)
                    .toList();

            if (stacks.isEmpty())
                return;

            for (var stack : stacks) {
                if (!(stack.getItem() instanceof IllusionistsMantleItem item))
                    continue;

                var relicData = item.getRelicData(player, stack);
                var ability = relicData.getAbilitiesData().getAbilityData("deception");

                if (!ability.getRankModifierData("fatal_exchange").isEnabled())
                    continue;

                UUID killerUuid = null;
                var killer = event.getSource().getEntity();

                if (killer != null)
                    killerUuid = killer.getUUID();

                final UUID finalKillerUuid = killerUuid;
                var nearestIllusion = player.level().getEntitiesOfClass(EntityDummy.class, player.getBoundingBox().inflate(64D),
                                dummy -> dummy.isAlive()
                                        && dummy.getPersistentData().getBoolean(MANTLE_ILLUSION_TAG)
                                        && player.getUUID().equals(dummy.getOwnerUUID())
                                        && finalKillerUuid != null
                                        && dummy.getPersistentData().hasUUID(MANTLE_TARGET_TAG)
                                        && finalKillerUuid.equals(dummy.getPersistentData().getUUID(MANTLE_TARGET_TAG)))
                        .stream()
                        .min(Comparator.comparingDouble(player::distanceToSqr))
                        .orElse(null);

                if (nearestIllusion == null) {
                    nearestIllusion = player.level().getEntitiesOfClass(EntityDummy.class, player.getBoundingBox().inflate(64D),
                                    dummy -> dummy.isAlive()
                                            && dummy.getPersistentData().getBoolean(MANTLE_ILLUSION_TAG)
                                            && player.getUUID().equals(dummy.getOwnerUUID()))
                            .stream()
                            .min(Comparator.comparingDouble(player::distanceToSqr))
                            .orElse(null);
                }

                if (nearestIllusion == null)
                    continue;

                var playerPos = player.position();
                var illusionPos = nearestIllusion.position();
                var deathIllusion = new EntityDummy(player.level());

                deathIllusion.setPos(playerPos.x, playerPos.y, playerPos.z);
                deathIllusion.setOwnerID(player.getUUID());
                deathIllusion.ticksLeft = 20;
                deathIllusion.setHealth(1F);
                deathIllusion.getPersistentData().putBoolean(MANTLE_ILLUSION_TAG, true);
                player.level().addFreshEntity(deathIllusion);
                deathIllusion.hurt(player.damageSources().genericKill(), 9999F);

                player.setHealth(1F);
                player.teleportTo(illusionPos.x, illusionPos.y, illusionPos.z);
                nearestIllusion.remove(Entity.RemovalReason.DISCARDED);
                event.setCanceled(true);

                relicData.getLevelingData().addExperience("deception", "fatal_saved", 1D);
                ability.getStatisticData().getMetricData("fatal_saves").addValue(1D);
                return;
            }
        }
    }
}

