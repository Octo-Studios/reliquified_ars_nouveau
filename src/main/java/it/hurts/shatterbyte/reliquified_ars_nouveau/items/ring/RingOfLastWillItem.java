package it.hurts.shatterbyte.reliquified_ars_nouveau.items.ring;

import com.hollingsworth.arsnouveau.api.event.EventQueue;
import com.hollingsworth.arsnouveau.common.event.timed.RewindEvent;
import com.hollingsworth.arsnouveau.common.entity.EntityDummy;
import com.hollingsworth.arsnouveau.common.network.Networking;
import com.hollingsworth.arsnouveau.common.network.PacketClientRewindEffect;
import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.back.IllusionistsMantleItem;
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
import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import it.hurts.sskirillss.relics.init.RelicsRelicContainers;
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
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.UUID;

public class RingOfLastWillItem extends RANWearableRelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("last_will")
                                .rankModifier(1, "vengeful_strike")
                                .rankModifier(3, "death_paralysis")
                                .rankModifier(5, "immortal_aftershock")
                                .stat(AbilityStatTemplate.builder("revenge_damage_bonus")
                                        .initialValue(0.25D, 0.5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 1.5D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("paralysis_duration")
                                        .initialValue(1D, 3D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 5D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("immortality_duration")
                                        .initialValue(1D, 3D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 10D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("cooldown")
                                        .initialValue(100D, 75D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 25D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("fatal_rewind").build())
                                        .source(ExperienceSourceTemplate.builder("revenge_hit")
                                                .rankModifierVisibilityState("vengeful_strike", VisibilityState.OBFUSCATED)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("killer_paralyzed")
                                                .rankModifierVisibilityState("death_paralysis", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("fatal_triggers")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("revenge_bonus_damage")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .rankModifierVisibilityState("vengeful_strike", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("paralysis_time")
                                                .formatValue(value -> MathUtils.formatTime((int) MathUtils.round(value, 0)))
                                                .rankModifierVisibilityState("death_paralysis", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("immortality_time")
                                                .formatValue(value -> MathUtils.formatTime((int) MathUtils.round(value, 0)))
                                                .rankModifierVisibilityState("immortal_aftershock", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .build())
                        .synergy(SynergyTemplate.builder("illusionary_rewind")
                                .stat(SynergyStatTemplate.builder("taunt_radius")
                                        .thresholdValue(5D, 15D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .condition(RelicConditionTemplate.builder(() -> (IllusionistsMantleItem) ItemRegistry.ILLUSIONISTS_MANTLE.get())
                                        .container(RelicsRelicContainers.CURIOS.get())
                                        .condition(AbilityConditionTemplate.builder("deception").build())
                                        .build())
                                .condition(RelicConditionTemplate.builder(() -> (RingOfLastWillItem) ItemRegistry.RING_OF_LAST_WILL.get())
                                        .container(RelicsRelicContainers.CURIOS.get())
                                        .condition(AbilityConditionTemplate.builder("last_will").build())
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

            if (event.getAmount() < player.getHealth())
                return;

            var attacker = event.getSource().getEntity();
            LivingEntity livingAttacker = attacker instanceof LivingEntity living && living.isAlive() && attacker != player ? living : null;

            var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.RING_OF_LAST_WILL.get()).stream()
                    .filter(relicStack -> relicStack.getItem() instanceof RingOfLastWillItem)
                    .toList();

            if (stacks.isEmpty())
                return;

            for (var stack : stacks) {
                if (!(stack.getItem() instanceof RingOfLastWillItem item))
                    continue;

                var relicData = item.getRelicData(player, stack);
                var ability = relicData.getAbilitiesData().getAbilityData("last_will");

                if (player.level().getGameTime() < stack.getOrDefault(RANDataComponentRegistry.RING_OF_LAST_WILL_COOLDOWN_UNTIL.get(), 0L))
                    continue;

                var rewindTicks = 60;
                var startPos = player.position();
                var cooldownTicks = Math.max(1L, Math.round(Math.max(0D, ability.getStatData("cooldown").getValue() * 20D)));

                EventQueue.getServerInstance().addEvent(new RewindEvent(player, player.level().getGameTime(), rewindTicks));
                Networking.sendToNearbyClient(player.level(), player, new PacketClientRewindEffect(rewindTicks, player));

                event.setCanceled(true);

                stack.set(RANDataComponentRegistry.RING_OF_LAST_WILL_REWIND_UNTIL.get(), player.level().getGameTime() + rewindTicks);
                stack.set(RANDataComponentRegistry.RING_OF_LAST_WILL_COOLDOWN_UNTIL.get(), player.level().getGameTime() + cooldownTicks);
                stack.set(RANDataComponentRegistry.RING_OF_LAST_WILL_LAST_ELECTRICITY_ID.get(), -1);
                stack.set(RANDataComponentRegistry.RING_OF_LAST_WILL_ILLUSION_ID.get(), -1);

                relicData.getLevelingData().addExperience("last_will", "fatal_rewind", 1D);
                ability.getStatisticData().getMetricData("fatal_triggers").addValue(1D);

                if (livingAttacker != null && ability.getRankModifierData("vengeful_strike").isEnabled())
                    stack.set(RANDataComponentRegistry.RING_OF_LAST_WILL_REVENGE_TARGET.get(), livingAttacker.getUUID().toString());

                var synergy = relicData.getAbilitiesData().getSynergyData("illusionary_rewind");

                if (synergy.isUnlocked() && !synergy.getMode().equals("disabled")) {
                    var illusion = new EntityDummy(player.level());

                    illusion.setPos(startPos.x, startPos.y, startPos.z);
                    illusion.setOwnerID(player.getUUID());
                    illusion.ticksLeft = rewindTicks;
                    illusion.setHealth(1F);
                    illusion.getPersistentData().putBoolean("ran_illusionists_mantle", true);
                    if (livingAttacker != null)
                        illusion.getPersistentData().putUUID("ran_illusionists_mantle_target", livingAttacker.getUUID());
                    player.level().addFreshEntity(illusion);
                    stack.set(RANDataComponentRegistry.RING_OF_LAST_WILL_ILLUSION_ID.get(), illusion.getId());

                    player.addEffect(new MobEffectInstance(RelicsMobEffects.VANISHING, rewindTicks, 0, false, false, true), player);
                }

                if (livingAttacker != null && ability.getRankModifierData("death_paralysis").isEnabled()) {
                    var paralysisTicks = Math.max(1, (int) Math.round(Math.max(0D, ability.getStatData("paralysis_duration").getValue() * 20D)));

                    livingAttacker.addEffect(new MobEffectInstance(RelicsMobEffects.PARALYSIS, paralysisTicks, 0, false, true, true), player);
                    relicData.getLevelingData().addExperience("last_will", "killer_paralyzed", 1D);
                    ability.getStatisticData().getMetricData("paralysis_time").addValue(paralysisTicks / 20D);
                }

                if (ability.getRankModifierData("immortal_aftershock").isEnabled()) {
                    var immortalityTicks = Math.max(1, (int) Math.round(Math.max(0D, ability.getStatData("immortality_duration").getValue() * 20D)));

                    player.addEffect(new MobEffectInstance(RelicsMobEffects.IMMORTALITY, immortalityTicks, 0, false, false, true), player);
                    ability.getStatisticData().getMetricData("immortality_time").addValue(immortalityTicks / 20D);
                }

                return;
            }
        }

        @SubscribeEvent
        public static void onPlayerTickPost(PlayerTickEvent.Post event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide())
                return;

            var gameTime = player.level().getGameTime();

            for (var stack : EntityUtils.findEquippedCurios(player, ItemRegistry.RING_OF_LAST_WILL.get())) {
                if (!(stack.getItem() instanceof RingOfLastWillItem item))
                    continue;

                var relicData = item.getRelicData(player, stack);
                var rewindUntil = stack.getOrDefault(RANDataComponentRegistry.RING_OF_LAST_WILL_REWIND_UNTIL.get(), 0L);
                var synergy = relicData.getAbilitiesData().getSynergyData("illusionary_rewind");
                var illusionId = stack.getOrDefault(RANDataComponentRegistry.RING_OF_LAST_WILL_ILLUSION_ID.get(), -1);

                if (gameTime > rewindUntil) {
                    stack.set(RANDataComponentRegistry.RING_OF_LAST_WILL_LAST_ELECTRICITY_ID.get(), -1);
                    stack.set(RANDataComponentRegistry.RING_OF_LAST_WILL_ILLUSION_ID.get(), -1);
                    continue;
                }

                if (!synergy.isUnlocked() || synergy.getMode().equals("disabled") || illusionId < 0)
                    continue;

                var rawEntity = player.level().getEntity(illusionId);

                if (!(rawEntity instanceof EntityDummy illusion) || !illusion.isAlive()) {
                    stack.set(RANDataComponentRegistry.RING_OF_LAST_WILL_ILLUSION_ID.get(), -1);
                    continue;
                }

                var radius = Math.max(0D, synergy.getStatData("taunt_radius").getValue());

                if (radius <= 0D)
                    continue;

                for (var mob : player.level().getEntitiesOfClass(Mob.class, illusion.getBoundingBox().inflate(radius),
                        mob -> mob.isAlive() && mob != illusion)) {
                    mob.setTarget(illusion);
                }
            }
        }

        @SubscribeEvent
        public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
            if (!(event.getSource().getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || event.getEntity() == player || event.getNewDamage() <= 0F
                    || event.getSource().getDirectEntity() != player || !(event.getEntity() instanceof LivingEntity target))
                return;

            var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.RING_OF_LAST_WILL.get()).stream()
                    .filter(relicStack -> relicStack.getItem() instanceof RingOfLastWillItem)
                    .toList();

            if (stacks.isEmpty())
                return;

            var totalBonus = 0D;
            var targetUuid = target.getUUID();
            var baseDamage = Math.max(0D, event.getNewDamage());

            for (var stack : stacks) {
                if (!(stack.getItem() instanceof RingOfLastWillItem item))
                    continue;

                var relicData = item.getRelicData(player, stack);
                var ability = relicData.getAbilitiesData().getAbilityData("last_will");

                if (!ability.getRankModifierData("vengeful_strike").isEnabled())
                    continue;

                var revengeTarget = stack.get(RANDataComponentRegistry.RING_OF_LAST_WILL_REVENGE_TARGET.get());

                if (revengeTarget == null)
                    continue;

                UUID revengeUuid;

                try {
                    revengeUuid = UUID.fromString(revengeTarget);
                } catch (IllegalArgumentException ignored) {
                    stack.remove(RANDataComponentRegistry.RING_OF_LAST_WILL_REVENGE_TARGET.get());
                    continue;
                }

                if (!targetUuid.equals(revengeUuid))
                    continue;

                var bonus = Mth.clamp(ability.getStatData("revenge_damage_bonus").getValue(), 0D, 10D);

                if (bonus <= 0D) {
                    stack.remove(RANDataComponentRegistry.RING_OF_LAST_WILL_REVENGE_TARGET.get());
                    continue;
                }

                totalBonus += bonus;
                stack.remove(RANDataComponentRegistry.RING_OF_LAST_WILL_REVENGE_TARGET.get());
                relicData.getLevelingData().addExperience("last_will", "revenge_hit", 1D);
                ability.getStatisticData().getMetricData("revenge_bonus_damage").addValue(baseDamage * bonus);
            }

            if (totalBonus > 0D)
                event.setNewDamage((float) Math.max(0D, baseDamage * (1D + totalBonus)));
        }
    }
}

