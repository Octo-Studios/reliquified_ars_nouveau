package it.hurts.shatterbyte.reliquified_ars_nouveau.items.bracelet;

import com.hollingsworth.arsnouveau.api.util.DamageUtil;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import com.hollingsworth.arsnouveau.setup.registry.DamageTypesRegistry;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import top.theillusivec4.curios.api.SlotContext;

public class FlamingBracerItem extends RANWearableRelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("pyroclastic")
                                .rankModifier(1, "fire_immunity")
                                .rankModifier(3, "fire_regeneration")
                                .rankModifier(5, "fire_exploitation")
                                .stat(AbilityStatTemplate.builder("paralysis_chance")
                                        .initialValue(0.1D, 0.15D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.25001D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("paralysis_duration")
                                        .initialValue(1D, 3D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 5.00025D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("magic_fire_damage")
                                        .initialValue(2.5D, 5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 9.99975D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("fire_regeneration")
                                        .initialValue(1D, 2.5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 4.99988D)
                                        .formatValue(value -> MathUtils.round(value, 2))
                                        .build())
                                .stat(AbilityStatTemplate.builder("fire_damage_bonus")
                                        .initialValue(0.15D, 0.25D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.99996D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("ability_trigger").build())
                                        .source(ExperienceSourceTemplate.builder("healing_in_fire")
                                                .rankModifierVisibilityState("fire_regeneration", VisibilityState.OBFUSCATED)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("bonus_damage_hit")
                                                .rankModifierVisibilityState("fire_exploitation", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("ability_triggers")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("paralysis_time")
                                                .formatValue(value -> MathUtils.formatTime((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("magic_fire_damage_dealt")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("health_restored")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .rankModifierVisibilityState("fire_regeneration", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("bonus_damage_dealt")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .rankModifierVisibilityState("fire_exploitation", VisibilityState.OBFUSCATED)
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

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof ServerPlayer player) || player.level().isClientSide() || player.tickCount % 20 != 0)
            return;

        var relicData = this.getRelicData(player, stack);
        var ability = relicData.getAbilitiesData().getAbilityData("pyroclastic");

        if (!ability.getRankModifierData("fire_regeneration").isEnabled())
            return;

        var playerPos = player.blockPosition();
        var inMagicFire = player.level().getBlockState(playerPos).is(BlockRegistry.MAGIC_FIRE.get())
                || player.level().getBlockState(playerPos.above()).is(BlockRegistry.MAGIC_FIRE.get());

        if (!inMagicFire)
            return;

        var healPerSecond = (float) Math.max(0D, ability.getStatData("fire_regeneration").getValue());

        if (healPerSecond <= 0F)
            return;

        var before = player.getHealth();

        player.heal(healPerSecond);

        var restored = Math.max(0F, player.getHealth() - before);

        if (restored <= 0F)
            return;

        relicData.getLevelingData().addExperience("pyroclastic", "healing_in_fire", 1D);
        ability.getStatisticData().getMetricData("health_restored").addValue(restored);
    }

    @EventBusSubscriber(modid = ReliquifiedArsNouveau.MODID)
    public static class CommonEvents {
        @SubscribeEvent
        public static void onAttackEntity(AttackEntityEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide())
                return;

            if (!(event.getTarget() instanceof LivingEntity target) || !target.isAlive())
                return;

            if (Math.max(player.getAttackStrengthScale(0F), player.getAttackStrengthScale(0.5F)) < 0.9F)
                return;

            var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.FLAMING_BRACER.get()).stream()
                    .filter(relicStack -> relicStack.getItem() instanceof FlamingBracerItem)
                    .toList();

            if (stacks.isEmpty())
                return;

            for (var stack : stacks) {
                if (!(stack.getItem() instanceof FlamingBracerItem item))
                    continue;

                var relicData = item.getRelicData(player, stack);
                var ability = relicData.getAbilitiesData().getAbilityData("pyroclastic");

                var chance = Mth.clamp(ability.getStatData("paralysis_chance").getValue(), 0D, 1D);

                if (chance <= 0D || player.getRandom().nextDouble() >= chance)
                    continue;

                var firePos = target.blockPosition();

                if (!player.level().getBlockState(firePos).canBeReplaced() || !player.level().getFluidState(firePos).isEmpty())
                    continue;

                player.level().setBlockAndUpdate(firePos, BlockRegistry.MAGIC_FIRE.get().getStateForPlacement(player.level(), firePos));

                var paralysisTicks = Math.max(1, (int) Math.round(Math.max(0D, ability.getStatData("paralysis_duration").getValue()) * 20D));

                target.addEffect(new MobEffectInstance(RelicsMobEffects.PARALYSIS, paralysisTicks, 0, false, true, true), player);

                var fireDamage = (float) Math.max(0D, ability.getStatData("magic_fire_damage").getValue());

                if (fireDamage > 0F && EntityUtils.hurt(target, DamageUtil.source(player.level(), DamageTypesRegistry.FLARE, player), fireDamage))
                    ability.getStatisticData().getMetricData("magic_fire_damage_dealt").addValue(fireDamage);

                relicData.getLevelingData().addExperience("pyroclastic", "ability_trigger", 1D);
                ability.getStatisticData().getMetricData("ability_triggers").addValue(1D);
                ability.getStatisticData().getMetricData("paralysis_time").addValue(paralysisTicks / 20D);
            }
        }

        @SubscribeEvent
        public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
            if (event.getNewDamage() <= 0F)
                return;

            if (!(event.getSource().getEntity() instanceof ServerPlayer attacker) || attacker.level().isClientSide()
                    || event.getEntity() == attacker || event.getSource().getDirectEntity() != attacker)
                return;

            var stacks = EntityUtils.findEquippedCurios(attacker, ItemRegistry.FLAMING_BRACER.get()).stream()
                    .filter(relicStack -> relicStack.getItem() instanceof FlamingBracerItem)
                    .toList();

            if (stacks.isEmpty())
                return;

            var targetPos = event.getEntity().blockPosition();
            var targetInMagicFire = attacker.level().getBlockState(targetPos).is(BlockRegistry.MAGIC_FIRE.get())
                    || attacker.level().getBlockState(targetPos.above()).is(BlockRegistry.MAGIC_FIRE.get());

            if (!targetInMagicFire)
                return;

            for (var stack : stacks) {
                if (!(stack.getItem() instanceof FlamingBracerItem item))
                    continue;

                var relicData = item.getRelicData(attacker, stack);
                var ability = relicData.getAbilitiesData().getAbilityData("pyroclastic");

                if (!ability.getRankModifierData("fire_exploitation").isEnabled())
                    continue;

                var bonusFraction = Mth.clamp(ability.getStatData("fire_damage_bonus").getValue(), 0D, 1D);

                if (bonusFraction <= 0D)
                    continue;

                var oldDamage = Math.max(0D, event.getNewDamage());
                var newDamage = oldDamage * (1D + bonusFraction);
                var bonusDamage = Math.max(0D, newDamage - oldDamage);

                if (bonusDamage <= 0D)
                    continue;

                event.setNewDamage((float) newDamage);
                relicData.getLevelingData().addExperience("pyroclastic", "bonus_damage_hit", 1D);
                ability.getStatisticData().getMetricData("bonus_damage_dealt").addValue(bonusDamage);
            }
        }
    }
}

