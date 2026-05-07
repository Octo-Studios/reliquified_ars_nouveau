package it.hurts.shatterbyte.reliquified_ars_nouveau.items.back;

import com.hollingsworth.arsnouveau.common.entity.EntityChimeraProjectile;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class SpikedCloakItem extends RANWearableRelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("thorn_burst")
                                .rankModifier(1, "bleeding_spikes")
                                .rankModifier(3, "guardian_resilience")
                                .rankModifier(5, "spike_bloom")
                                .stat(AbilityStatTemplate.builder("damage_threshold_fraction")
                                        .initialValue(0.2D, 0.35D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.04762D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("trigger_chance")
                                        .initialValue(0.2D, 0.35D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("max_spikes")
                                        .initialValue(2D, 5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> Math.max(1, (int) MathUtils.round(value, 0)))
                                        .build())
                                .stat(AbilityStatTemplate.builder("spike_damage")
                                        .initialValue(3D, 6D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("bleeding_level")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.04762D)
                                        .formatValue(value -> Math.max(1, (int) MathUtils.round(value, 0)))
                                        .build())
                                .stat(AbilityStatTemplate.builder("bleeding_duration")
                                        .initialValue(2D, 5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("resistance_bonus")
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("resistance_duration")
                                        .initialValue(2D, 5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("chain_chance")
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("chain_max_spikes")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> Math.max(1, (int) MathUtils.round(value, 0)))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("burst_trigger").build())
                                        .source(ExperienceSourceTemplate.builder("spike_hit").build())
                                        .source(ExperienceSourceTemplate.builder("chain_trigger")
                                                .rankModifierVisibilityState("spike_bloom", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("spikes_fired")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("spike_hits")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("spike_damage_dealt")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("bleeding_time")
                                                .formatValue(value -> MathUtils.formatTime((int) MathUtils.round(value, 0)))
                                                .rankModifierVisibilityState("bleeding_spikes", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("damage_reduced")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .rankModifierVisibilityState("guardian_resilience", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("chain_triggers")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .rankModifierVisibilityState("spike_bloom", VisibilityState.OBFUSCATED)
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

            var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.SPIKED_CLOAK.get()).stream()
                    .filter(relicStack -> relicStack.getItem() instanceof SpikedCloakItem)
                    .toList();

            if (stacks.isEmpty())
                return;

            var gameTime = player.level().getGameTime();
            var incoming = Math.max(0D, event.getAmount());

            for (var stack : stacks) {
                if (!(stack.getItem() instanceof SpikedCloakItem item))
                    continue;

                var relicData = item.getRelicData(player, stack);
                var ability = relicData.getAbilitiesData().getAbilityData("thorn_burst");

                if (!ability.getRankModifierData("guardian_resilience").isEnabled())
                    continue;

                var resistUntil = stack.getOrDefault(RANDataComponentRegistry.SPIKED_CLOAK_RESISTANCE_UNTIL.get(), 0L);

                if (resistUntil <= gameTime)
                    continue;

                var reduction = Mth.clamp(ability.getStatData("resistance_bonus").getValue(), 0D, 0.95D);

                if (reduction <= 0D)
                    continue;

                var delta = incoming * reduction;
                incoming -= delta;
                ability.getStatisticData().getMetricData("damage_reduced").addValue(delta);
            }

            event.setAmount((float) Math.max(0D, incoming));

            var attacker = event.getSource().getEntity();
            var attackerLiving = attacker instanceof LivingEntity living && living.isAlive() && living != player ? living : null;
            var maxHealth = Math.max(1F, player.getMaxHealth());

            for (var stack : stacks) {
                if (!(stack.getItem() instanceof SpikedCloakItem item))
                    continue;

                var relicData = item.getRelicData(player, stack);
                var ability = relicData.getAbilitiesData().getAbilityData("thorn_burst");

                var threshold = Mth.clamp(ability.getStatData("damage_threshold_fraction").getValue(), 0D, 1D) * maxHealth;

                if (incoming < threshold)
                    continue;

                var triggerChance = Mth.clamp(ability.getStatData("trigger_chance").getValue(), 0D, 1D);
                var maxSpikes = Math.max(1, (int) Math.round(Math.max(0D, ability.getStatData("max_spikes").getValue())));
                var spikesToShoot = MathUtils.multicast(player.getRandom(), triggerChance, maxSpikes);
                var spikeDamage = Math.max(0D, ability.getStatData("spike_damage").getValue());

                if (spikesToShoot <= 0 || spikeDamage <= 0D)
                    continue;

                var origin = player.getEyePosition();
                var baseDirection = attackerLiving != null
                        ? attackerLiving.getEyePosition().subtract(origin)
                        : player.getLookAngle();
                var horizontalForward = new Vec3(baseDirection.x, 0D, baseDirection.z);

                if (horizontalForward.lengthSqr() < 1.0E-6D)
                    horizontalForward = new Vec3(0D, 0D, 1D);
                else
                    horizontalForward = horizontalForward.normalize();

                var right = new Vec3(-horizontalForward.z, 0D, horizontalForward.x);
                var circleOffset = player.getRandom().nextDouble() * (Math.PI * 2D);

                for (var i = 0; i < spikesToShoot; i++) {
                    var angle = circleOffset + (Math.PI * 2D * i) / spikesToShoot;
                    var direction = horizontalForward.scale(Math.cos(angle))
                            .add(right.scale(Math.sin(angle)))
                            .normalize();

                    var spike = new EntityChimeraProjectile(player.level());
                    var spawnPos = origin.add(direction.scale(0.65D));

                    spike.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
                    spike.setOwner(player);
                    spike.getPersistentData().putBoolean("ran_spiked_cloak_spike", true);
                    spike.getPersistentData().putDouble("ran_spiked_cloak_damage", spikeDamage);
                    spike.getPersistentData().putInt("ran_spiked_cloak_chain_depth", 0);
                    if (ability.getRankModifierData("spike_bloom").isEnabled()) {
                        spike.getPersistentData().putDouble("ran_spiked_cloak_chain_chance", Mth.clamp(ability.getStatData("chain_chance").getValue(), 0D, 1D));
                        spike.getPersistentData().putInt("ran_spiked_cloak_chain_max_spikes", Math.max(1, (int) Math.round(Math.max(0D, ability.getStatData("chain_max_spikes").getValue()))));
                    }
                    if (ability.getRankModifierData("bleeding_spikes").isEnabled()) {
                        var bleedingLevel = Math.max(1, (int) Math.round(Math.max(0D, ability.getStatData("bleeding_level").getValue())));
                        var bleedingDurationTicks = Math.max(1, (int) Math.round(Math.max(0D, ability.getStatData("bleeding_duration").getValue()) * 20D));
                        spike.getPersistentData().putInt("ran_spiked_cloak_bleeding_level", Math.max(0, bleedingLevel - 1));
                        spike.getPersistentData().putInt("ran_spiked_cloak_bleeding_duration", bleedingDurationTicks);
                    }
                    spike.shoot(direction.x, direction.y, direction.z, 2.1F, 0F);
                    player.level().addFreshEntity(spike);
                }

                relicData.getLevelingData().addExperience("thorn_burst", "burst_trigger", 1D);
                ability.getStatisticData().getMetricData("spikes_fired").addValue(spikesToShoot);

                if (ability.getRankModifierData("guardian_resilience").isEnabled()) {
                    var resistDurationTicks = Math.max(1L, Math.round(Math.max(0D, ability.getStatData("resistance_duration").getValue()) * 20D));
                    stack.set(RANDataComponentRegistry.SPIKED_CLOAK_RESISTANCE_UNTIL.get(),
                            Math.max(gameTime + resistDurationTicks, stack.getOrDefault(RANDataComponentRegistry.SPIKED_CLOAK_RESISTANCE_UNTIL.get(), 0L)));
                }
            }
        }
    }
}

