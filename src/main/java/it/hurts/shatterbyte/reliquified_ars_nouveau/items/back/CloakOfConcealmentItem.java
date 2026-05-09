package it.hurts.shatterbyte.reliquified_ars_nouveau.items.back;

import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.base.RANRelicItem;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.base.RANWearableRelicItem;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.base.loot.LootEntries;
import it.hurts.shatterbyte.reliquified_ars_nouveau.network.NetworkHandler;
import it.hurts.shatterbyte.reliquified_ars_nouveau.network.packets.S2CCloakAbsorptionSpherePacket;
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
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class CloakOfConcealmentItem extends RANWearableRelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("absorption")
                                .rankModifier(1, "damage_reflection")
                                .rankModifier(3, "damage_to_healing")
                                .rankModifier(5, "damage_distribution")
                                .stat(AbilityStatTemplate.builder("mana_per_damage")
                                        .initialValue(75D, 50D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 24.99250D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("chain_damage_reduction")
                                        .initialValue(0.05D, 0.1D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.25001D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("reflect_window")
                                        .initialValue(1D, 2.5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 4.99988D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("healing_conversion")
                                        .initialValue(0.05D, 0.1D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.25001D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("distribution_radius")
                                        .initialValue(2.5D, 5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 9.99975D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("absorption").build())
                                        .source(ExperienceSourceTemplate.builder("damage_distribution_target")
                                                .rankModifierVisibilityState("damage_distribution", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("damage_absorbed")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("mana_spent")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("damage_reflected")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .rankModifierVisibilityState("damage_reflection", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("health_restored")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .rankModifierVisibilityState("damage_to_healing", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("damage_distributed")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .rankModifierVisibilityState("damage_distribution", VisibilityState.OBFUSCATED)
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
        public static void onLivingDamagePre(LivingIncomingDamageEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || event.getAmount() <= 0F)
                return;

            var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.CLOAK_OF_CONCEALMENT.get()).stream()
                    .filter(relicStack -> relicStack.getItem() instanceof CloakOfConcealmentItem)
                    .toList();

            if (stacks.isEmpty())
                return;

            var manaCap = CapabilityRegistry.getMana(player);

            if (manaCap == null)
                return;

            for (var stack : stacks) {
                if (!(stack.getItem() instanceof CloakOfConcealmentItem item))
                    continue;

                var relicData = item.getRelicData(player, stack);
                var ability = relicData.getAbilitiesData().getAbilityData("absorption");

                var incomingDamage = Math.max(0D, event.getAmount());

                if (incomingDamage <= 0D)
                    break;

                var statistics = ability.getStatisticData();
                var manaPerDamage = Math.max(0D, ability.getStatData("mana_per_damage").getValue());
                var currentMana = Math.max(0D, manaCap.getCurrentMana());
                var currentTick = player.level().getGameTime();
                var reducedDamage = 0D;
                var canAbsorbNow = manaPerDamage <= 0D ? incomingDamage > 0D : currentMana > 0D;

                if (ability.getRankModifierData("damage_reflection").isEnabled() && canAbsorbNow) {
                    var chainedReductionValue = Mth.clamp(ability.getStatData("chain_damage_reduction").getValue(), 0D, 1D);
                    var chainWindowSeconds = Math.max(0D, ability.getStatData("reflect_window").getValue());
                    var previousTriggerTick = stack.get(RANDataComponentRegistry.CLOAK_OF_CONCEALMENT_LAST_TRIGGER_TICK.get());
                    var chainWindowTicks = Math.max(0L, Math.round(chainWindowSeconds * 20D));

                    if (previousTriggerTick != null && chainWindowTicks > 0L && currentTick - previousTriggerTick <= chainWindowTicks) {
                        reducedDamage = incomingDamage * chainedReductionValue;
                        incomingDamage = Math.max(0D, incomingDamage - reducedDamage);
                    }
                }

                if (incomingDamage <= 0D) {
                    if (reducedDamage > 0D)
                        statistics.getMetricData("damage_reflected").addValue(reducedDamage);

                    event.setCanceled(true);

                    if (ability.getRankModifierData("damage_reflection").isEnabled())
                        stack.set(RANDataComponentRegistry.CLOAK_OF_CONCEALMENT_LAST_TRIGGER_TICK.get(), currentTick);

                    return;
                }

                var absorbedDamage = manaPerDamage <= 0D ? incomingDamage : Math.min(incomingDamage, currentMana / manaPerDamage);

                if (absorbedDamage <= 0D)
                    continue;

                var manaSpent = manaPerDamage <= 0D ? 0D : absorbedDamage * manaPerDamage;

                if (reducedDamage > 0D)
                    statistics.getMetricData("damage_reflected").addValue(reducedDamage);

                if (manaSpent > 0D) {
                    manaCap.removeMana(manaSpent);
                    manaCap.syncToClient(player);
                }

                var remainingDamage = Math.max(0D, incomingDamage - absorbedDamage);

                if (remainingDamage <= 0D)
                    event.setCanceled(true);
                else
                    event.setAmount((float) remainingDamage);

                if (absorbedDamage > 0D)
                    relicData.getLevelingData().addExperience("absorption", "absorption", 1D);

                if (ability.getRankModifierData("damage_reflection").isEnabled())
                    stack.set(RANDataComponentRegistry.CLOAK_OF_CONCEALMENT_LAST_TRIGGER_TICK.get(), currentTick);

                NetworkHandler.sendToClientsTrackingEntityAndSelf(new S2CCloakAbsorptionSpherePacket(player.getId(), remainingDamage > 0D), player);

                statistics.getMetricData("damage_absorbed").addValue(absorbedDamage);
                statistics.getMetricData("mana_spent").addValue(manaSpent);

                if (ability.getRankModifierData("damage_to_healing").isEnabled()) {
                    var healingFraction = Mth.clamp(ability.getStatData("healing_conversion").getValue(), 0D, 1D);
                    var healingAmount = (float) Math.max(0D, absorbedDamage * healingFraction);

                    if (healingAmount > 0F) {
                        var healthBefore = player.getHealth();

                        player.heal(healingAmount);

                        var restored = Math.max(0F, player.getHealth() - healthBefore);

                        if (restored > 0F)
                            statistics.getMetricData("health_restored").addValue(restored);
                    }
                }

                if (ability.getRankModifierData("damage_distribution").isEnabled()) {
                    var radius = Math.max(0D, ability.getStatData("distribution_radius").getValue());

                    if (radius > 0D) {
                        var targets = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius),
                                target -> target != player && target.isAlive() && !EntityUtils.isAlliedTo(player, target));

                        if (!targets.isEmpty()) {
                            var distributedDamage = 0F;
                            var damagePerTarget = (float) (absorbedDamage / (double) targets.size());

                            if (damagePerTarget > 0F) {
                                var damagedTargets = 0;

                                for (var target : targets) {
                                    if (EntityUtils.hurt(target, player.damageSources().thorns(player), damagePerTarget)) {
                                        distributedDamage += damagePerTarget;
                                        damagedTargets++;
                                    }
                                }

                                if (damagedTargets > 0)
                                    relicData.getLevelingData().addExperience("absorption", "damage_distribution_target", damagedTargets);
                            }

                            if (distributedDamage > 0F)
                                statistics.getMetricData("damage_distributed").addValue(distributedDamage);
                        }
                    }
                }

                if (remainingDamage <= 0D)
                    return;
            }
        }
    }
}

