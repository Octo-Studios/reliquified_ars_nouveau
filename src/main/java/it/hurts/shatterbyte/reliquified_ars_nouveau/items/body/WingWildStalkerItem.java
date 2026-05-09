package it.hurts.shatterbyte.reliquified_ars_nouveau.items.body;

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
import it.hurts.sskirillss.relics.api.relics.synergies.SynergyTemplate;
import it.hurts.sskirillss.relics.api.relics.synergies.conditions.AbilityConditionTemplate;
import it.hurts.sskirillss.relics.api.relics.synergies.conditions.RelicConditionTemplate;
import it.hurts.sskirillss.relics.api.relics.synergies.stats.SynergyStatTemplate;
import it.hurts.sskirillss.relics.init.*;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.entities.ChainedElectricityEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class WingWildStalkerItem extends RANWearableRelicItem {
    public ChainedElectricityEntity getLastElectricityEntity(ServerPlayer owner, net.minecraft.world.item.ItemStack stack) {
        var entityId = stack.getOrDefault(RANDataComponentRegistry.WING_WILD_STALKER_LAST_ELECTRICITY_ID.get(), -1);

        if (entityId < 0)
            return null;

        var raw = owner.level().getEntity(entityId);

        if (raw instanceof ChainedElectricityEntity electricity && electricity.isAlive())
            return electricity;

        stack.set(RANDataComponentRegistry.WING_WILD_STALKER_LAST_ELECTRICITY_ID.get(), -1);
        return null;
    }


    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("wild_flight")
                                .rankModifier(1, "flight_guard")
                                .rankModifier(3, "predator_strike")
                                .rankModifier(5, "aerial_dash")
                                .stat(AbilityStatTemplate.builder("flight_duration")
                                        .initialValue(5D, 10D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 30D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("flight_damage_reduction")
                                        .initialValue(0.1D, 0.25D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("flight_attack_bonus")
                                        .initialValue(0.25D, 0.5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 1.5D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("dash_strength")
                                        .initialValue(0.1D, 0.25D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 1D)
                                        .formatValue(value -> MathUtils.round(value, 2))
                                        .build())
                                .stat(AbilityStatTemplate.builder("dash_cooldown")
                                        .initialValue(10D, 7.5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 1D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("flight_time").build())
                                        .source(ExperienceSourceTemplate.builder("damage_reduced")
                                                .rankModifierVisibilityState("flight_guard", VisibilityState.OBFUSCATED)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("flight_strike")
                                                .rankModifierVisibilityState("predator_strike", VisibilityState.OBFUSCATED)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("dash")
                                                .rankModifierVisibilityState("aerial_dash", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("distance_flown")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("damage_reduced")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .rankModifierVisibilityState("flight_guard", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("bonus_damage_dealt")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .rankModifierVisibilityState("predator_strike", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("dashes_used")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .rankModifierVisibilityState("aerial_dash", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .build())
                        .synergy(SynergyTemplate.builder("electricity")
                                .modes("enabled", "disabled")
                                .stat(SynergyStatTemplate.builder("damage")
                                        .thresholdValue(1, 5)
                                        .formatValue(value -> value)
                                        .build())
                                .stat(SynergyStatTemplate.builder("lifetime")
                                        .thresholdValue(3, 10)
                                        .formatValue(value -> value)
                                        .build())
                                .condition(RelicConditionTemplate.builder(() -> (WingWildStalkerItem) ItemRegistry.WING_OF_TH_WILD_STALKER.get())
                                        .container(RelicsRelicContainers.CURIOS.get())
                                        .condition(AbilityConditionTemplate.builder("wild_flight").build())
                                        .build())
                                .condition(RelicConditionTemplate.builder(() -> RelicsItems.JELLYFISH_NECKLACE.get())
                                        .container(RelicsRelicContainers.CURIOS.get())
                                        .condition(AbilityConditionTemplate.builder("shock").build())
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
        public static void onPlayerTickPost(PlayerTickEvent.Post event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide())
                return;

            var isFlying = player.isFallFlying();

            for (var stack : EntityUtils.findEquippedCurios(player, ItemRegistry.WING_OF_TH_WILD_STALKER.get())) {
                if (!(stack.getItem() instanceof WingWildStalkerItem relic))
                    continue;

                var relicData = relic.getRelicData(player, stack);
                var ability = relicData.getAbilitiesData().getAbilityData("wild_flight");
                if (!isFlying) {
                    stack.set(RANDataComponentRegistry.WING_WILD_STALKER_LAST_ELECTRICITY_ID.get(), -1);
                    continue;
                }

                ability.getStatisticData().getMetricData("distance_flown").addValue(player.getDeltaMovement().length());

                if (player.tickCount % 20 == 0)
                    relicData.getLevelingData().addExperience("wild_flight", "flight_time", 1D);

                if (!relicData.getAbilitiesData().getSynergyData("electricity").isUnlocked() || relicData.getAbilitiesData().getSynergyData("electricity").getMode().equals("disabled"))
                    stack.set(RANDataComponentRegistry.WING_WILD_STALKER_LAST_ELECTRICITY_ID.get(), -1);
            }
        }

        @SubscribeEvent
        public static void onLivingIncomingDamagePre(LivingDamageEvent.Pre event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || event.getNewDamage() <= 0F || !player.isFallFlying())
                return;

            var baseDamage = Math.max(0D, event.getNewDamage());
            var totalReduction = 0D;

            for (var stack : EntityUtils.findEquippedCurios(player, ItemRegistry.WING_OF_TH_WILD_STALKER.get())) {
                if (!(stack.getItem() instanceof WingWildStalkerItem item))
                    continue;

                var relicData = item.getRelicData(player, stack);
                var ability = relicData.getAbilitiesData().getAbilityData("wild_flight");

                if (!ability.getRankModifierData("flight_guard").isEnabled())
                    continue;

                var reduction = Mth.clamp(ability.getStatData("flight_damage_reduction").getValue(), 0D, 1D);

                if (reduction <= 0D)
                    continue;

                totalReduction += reduction;
                relicData.getLevelingData().addExperience("wild_flight", "damage_reduced", 1D);
                ability.getStatisticData().getMetricData("damage_reduced").addValue(baseDamage * reduction);
            }

            if (totalReduction > 0D)
                event.setNewDamage((float) Math.max(0D, baseDamage * (1D - Mth.clamp(totalReduction, 0D, 0.95D))));
        }

        @SubscribeEvent
        public static void onLivingDamageOutPre(LivingDamageEvent.Pre event) {
            if (!(event.getSource().getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || event.getEntity() == player || event.getNewDamage() <= 0F
                    || !player.isFallFlying() || event.getSource().getDirectEntity() != player || !(event.getEntity() instanceof LivingEntity))
                return;

            var baseDamage = Math.max(0D, event.getNewDamage());
            var totalBonus = 0D;

            for (var stack : EntityUtils.findEquippedCurios(player, ItemRegistry.WING_OF_TH_WILD_STALKER.get())) {
                if (!(stack.getItem() instanceof WingWildStalkerItem item))
                    continue;

                var relicData = item.getRelicData(player, stack);
                var ability = relicData.getAbilitiesData().getAbilityData("wild_flight");

                if (!ability.getRankModifierData("predator_strike").isEnabled())
                    continue;

                var bonus = Math.max(0D, ability.getStatData("flight_attack_bonus").getValue());

                if (bonus <= 0D)
                    continue;

                totalBonus += bonus;
                relicData.getLevelingData().addExperience("wild_flight", "flight_strike", 1D);
                ability.getStatisticData().getMetricData("bonus_damage_dealt").addValue(baseDamage * bonus);
            }

            if (totalBonus > 0D)
                event.setNewDamage((float) Math.max(0D, baseDamage * (1D + totalBonus)));
        }
    }
}

