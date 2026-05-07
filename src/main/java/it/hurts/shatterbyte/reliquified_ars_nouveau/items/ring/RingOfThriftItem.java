package it.hurts.shatterbyte.reliquified_ars_nouveau.items.ring;

import com.hollingsworth.arsnouveau.api.event.ManaRegenCalcEvent;
import com.hollingsworth.arsnouveau.api.event.SpellCastEvent;
import com.hollingsworth.arsnouveau.api.event.SpellCostCalcEvent;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.base.RANRelicItem;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;

public class RingOfThriftItem extends RANWearableRelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("thrift")
                                .rankModifier(1, "delayed_refund")
                                .rankModifier(3, "casting_momentum")
                                .rankModifier(5, "free_cast")
                                .stat(AbilityStatTemplate.builder("mana_cost_reduction")
                                        .initialValue(0.05D, 0.15D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("refund_percent")
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("refund_delay")
                                        .initialValue(3D, 1.5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), -0.05714D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("combo_duration")
                                        .initialValue(4D, 6D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("combo_regen_bonus_per_cast")
                                        .initialValue(0.03D, 0.08D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("combo_max_stacks")
                                        .initialValue(3D, 6D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> Math.max(1, (int) MathUtils.round(value, 0)))
                                        .build())
                                .stat(AbilityStatTemplate.builder("free_cast_chance")
                                        .initialValue(0.03D, 0.08D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("mana_refunded")
                                                .rankModifierVisibilityState("delayed_refund", VisibilityState.OBFUSCATED)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("combo_cast")
                                                .rankModifierVisibilityState("casting_momentum", VisibilityState.OBFUSCATED)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("free_cast")
                                                .rankModifierVisibilityState("free_cast", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("mana_saved")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("mana_refunded")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .rankModifierVisibilityState("delayed_refund", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("free_casts")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .rankModifierVisibilityState("free_cast", VisibilityState.OBFUSCATED)
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
        public static void onSpellCostCalcPre(SpellCostCalcEvent.Pre event) {
            if (!(event.context.getCaster() instanceof com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster livingCaster)
                    || !(livingCaster.livingEntity instanceof ServerPlayer player) || player.level().isClientSide() || event.currentCost <= 0)
                return;

            var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.RING_OF_THRIFT.get()).stream()
                    .filter(relicStack -> relicStack.getItem() instanceof RingOfThriftItem)
                    .toList();

            if (stacks.isEmpty())
                return;

            var originalCost = Math.max(0, event.currentCost);
            var totalReduction = 0D;

            for (var stack : stacks) {
                if (!(stack.getItem() instanceof RingOfThriftItem item))
                    continue;

                var ability = item.getRelicData(player, stack).getAbilitiesData().getAbilityData("thrift");

                totalReduction += Math.max(0D, ability.getStatData("mana_cost_reduction").getValue());
            }

            var reducedCost = Math.max(0, (int) Math.round(originalCost * (1D - totalReduction)));
            event.currentCost = reducedCost;

            for (var stack : stacks) {
                if (!(stack.getItem() instanceof RingOfThriftItem item))
                    continue;

                var ability = item.getRelicData(player, stack).getAbilitiesData().getAbilityData("thrift");

                if (!ability.getRankModifierData("free_cast").isEnabled())
                    continue;

                var freeCastChance = Mth.clamp(ability.getStatData("free_cast_chance").getValue(), 0D, 1D);

                if (freeCastChance <= 0D || player.getRandom().nextDouble() >= freeCastChance)
                    continue;

                event.currentCost = 0;
                player.getPersistentData().putInt("ran_ring_of_thrift_pending_free_casts",
                        Math.max(0, player.getPersistentData().getInt("ran_ring_of_thrift_pending_free_casts")) + 1);
                break;
            }
        }

        @SubscribeEvent
        public static void onSpellCostCalcPost(SpellCostCalcEvent.Post event) {
            if (!(event.context.getCaster() instanceof com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster livingCaster)
                    || !(livingCaster.livingEntity instanceof ServerPlayer player) || player.level().isClientSide() || event.currentCost <= 0)
                return;

            var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.RING_OF_THRIFT.get()).stream()
                    .filter(relicStack -> relicStack.getItem() instanceof RingOfThriftItem)
                    .toList();

            if (stacks.isEmpty())
                return;

            var originalCost = Math.max(0, event.currentCost);
            var totalReduction = 0D;
            for (var stack : stacks) {
                if (!(stack.getItem() instanceof RingOfThriftItem item))
                    continue;

                var ability = item.getRelicData(player, stack).getAbilitiesData().getAbilityData("thrift");

                totalReduction += Math.max(0D, ability.getStatData("mana_cost_reduction").getValue());
            }

            var reducedCost = Math.max(0, (int) Math.round(originalCost * (1D - totalReduction)));
            event.currentCost = reducedCost;

            var saved = Math.max(0D, originalCost - reducedCost);

            if (saved > 0D) {
                for (var stack : stacks) {
                    if (!(stack.getItem() instanceof RingOfThriftItem item))
                        continue;

                    var ability = item.getRelicData(player, stack).getAbilitiesData().getAbilityData("thrift");

                    if (true)
                        ability.getStatisticData().getMetricData("mana_saved").addValue(saved / (double) stacks.size());
                }
            }

            var pendingFreeCasts = Math.max(0, player.getPersistentData().getInt("ran_ring_of_thrift_pending_free_casts"));

            if (pendingFreeCasts > 0) {
                event.currentCost = 0;
                player.getPersistentData().putInt("ran_ring_of_thrift_pending_free_casts", pendingFreeCasts - 1);

                for (var stack : stacks) {
                    if (!(stack.getItem() instanceof RingOfThriftItem item))
                        continue;

                    var relicData = item.getRelicData(player, stack);
                    var ability = relicData.getAbilitiesData().getAbilityData("thrift");

                    if (!ability.getRankModifierData("free_cast").isEnabled())
                        continue;

                    ability.getStatisticData().getMetricData("free_casts").addValue(1D);
                    relicData.getLevelingData().addExperience("thrift", "free_cast", 1D);
                    break;
                }
            }

            if (event.currentCost <= 0)
                return;

            var now = player.level().getGameTime();
            for (var stack : stacks) {
                if (!(stack.getItem() instanceof RingOfThriftItem item))
                    continue;

                var ability = item.getRelicData(player, stack).getAbilitiesData().getAbilityData("thrift");

                if (!ability.getRankModifierData("delayed_refund").isEnabled())
                    continue;

                var refundPercent = Mth.clamp(ability.getStatData("refund_percent").getValue(), 0D, 1D);
                var refundAmount = Math.max(0D, event.currentCost * refundPercent);

                if (refundAmount <= 0D)
                    continue;

                var delayTicks = Math.max(1L, Math.round(Math.max(0D, ability.getStatData("refund_delay").getValue()) * 20D));
                var amounts = new ArrayList<>(stack.getOrDefault(RANDataComponentRegistry.RING_OF_THRIFT_REFUND_AMOUNTS.get(), List.of()));
                var times = new ArrayList<>(stack.getOrDefault(RANDataComponentRegistry.RING_OF_THRIFT_REFUND_TIMES.get(), List.of()));
                var pendingRefund = 0D;

                for (var amount : amounts)
                    pendingRefund += Math.max(0D, amount);

                amounts.clear();
                times.clear();
                amounts.add(pendingRefund + refundAmount);
                times.add(now + delayTicks);

                stack.set(RANDataComponentRegistry.RING_OF_THRIFT_REFUND_AMOUNTS.get(), amounts);
                stack.set(RANDataComponentRegistry.RING_OF_THRIFT_REFUND_TIMES.get(), times);
            }
        }

        @SubscribeEvent
        public static void onSpellCast(SpellCastEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide())
                return;

            var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.RING_OF_THRIFT.get()).stream()
                    .filter(relicStack -> relicStack.getItem() instanceof RingOfThriftItem)
                    .toList();

            if (stacks.isEmpty())
                return;

            var now = player.level().getGameTime();
            for (var stack : stacks) {
                if (!(stack.getItem() instanceof RingOfThriftItem item))
                    continue;

                var relicData = item.getRelicData(player, stack);
                var ability = relicData.getAbilitiesData().getAbilityData("thrift");

                if (!ability.getRankModifierData("casting_momentum").isEnabled())
                    continue;

                var maxStacks = Math.max(1, (int) Math.round(Math.max(0D, ability.getStatData("combo_max_stacks").getValue())));
                var durationTicks = Math.max(1L, Math.round(Math.max(0D, ability.getStatData("combo_duration").getValue()) * 20D));
                var activeUntil = stack.getOrDefault(RANDataComponentRegistry.RING_OF_THRIFT_COMBO_UNTIL.get(), 0L);
                var comboStacks = Math.max(0, stack.getOrDefault(RANDataComponentRegistry.RING_OF_THRIFT_COMBO_STACKS.get(), 0));

                comboStacks = activeUntil > now ? Math.min(maxStacks, comboStacks + 1) : 1;

                stack.set(RANDataComponentRegistry.RING_OF_THRIFT_COMBO_STACKS.get(), comboStacks);
                stack.set(RANDataComponentRegistry.RING_OF_THRIFT_COMBO_UNTIL.get(), now + durationTicks);
                relicData.getLevelingData().addExperience("thrift", "combo_cast", 1D);
            }
        }

        @SubscribeEvent
        public static void onManaRegenCalc(ManaRegenCalcEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide())
                return;

            var bonus = 0D;
            var now = player.level().getGameTime();
            var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.RING_OF_THRIFT.get()).stream()
                    .filter(relicStack -> relicStack.getItem() instanceof RingOfThriftItem)
                    .toList();

            if (stacks.isEmpty())
                return;

            for (var stack : stacks) {
                if (!(stack.getItem() instanceof RingOfThriftItem item))
                    continue;

                var ability = item.getRelicData(player, stack).getAbilitiesData().getAbilityData("thrift");

                if (!ability.getRankModifierData("casting_momentum").isEnabled())
                    continue;

                if (stack.getOrDefault(RANDataComponentRegistry.RING_OF_THRIFT_COMBO_UNTIL.get(), 0L) <= now)
                    continue;

                var comboStacks = Math.max(0, stack.getOrDefault(RANDataComponentRegistry.RING_OF_THRIFT_COMBO_STACKS.get(), 0));

                if (comboStacks <= 0)
                    continue;

                bonus += comboStacks * Math.max(0D, ability.getStatData("combo_regen_bonus_per_cast").getValue());
            }

            if (bonus > 0D)
                event.setRegen(event.getRegen() * (1D + bonus));
        }

        @SubscribeEvent
        public static void onPlayerTickPost(PlayerTickEvent.Post event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide())
                return;

            var manaCap = CapabilityRegistry.getMana(player);

            if (manaCap == null)
                return;

            var now = player.level().getGameTime();
            var refunded = 0D;
            var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.RING_OF_THRIFT.get()).stream()
                    .filter(relicStack -> relicStack.getItem() instanceof RingOfThriftItem)
                    .toList();

            if (stacks.isEmpty())
                return;

            for (var stack : stacks) {
                if (!(stack.getItem() instanceof RingOfThriftItem item))
                    continue;

                var relicData = item.getRelicData(player, stack);
                var ability = relicData.getAbilitiesData().getAbilityData("thrift");
                var amounts = new ArrayList<>(stack.getOrDefault(RANDataComponentRegistry.RING_OF_THRIFT_REFUND_AMOUNTS.get(), List.of()));
                var times = new ArrayList<>(stack.getOrDefault(RANDataComponentRegistry.RING_OF_THRIFT_REFUND_TIMES.get(), List.of()));

                if (amounts.isEmpty() || times.isEmpty() || amounts.size() != times.size()) {
                    if (!amounts.isEmpty() || !times.isEmpty()) {
                        stack.remove(RANDataComponentRegistry.RING_OF_THRIFT_REFUND_AMOUNTS.get());
                        stack.remove(RANDataComponentRegistry.RING_OF_THRIFT_REFUND_TIMES.get());
                    }
                } else {
                    var keptAmounts = new ArrayList<Double>();
                    var keptTimes = new ArrayList<Long>();

                    for (var i = 0; i < amounts.size(); i++) {
                        if (times.get(i) <= now) {
                            refunded += Math.max(0D, amounts.get(i));
                            ability.getStatisticData().getMetricData("mana_refunded").addValue(Math.max(0D, amounts.get(i)));
                            relicData.getLevelingData().addExperience("thrift", "mana_refunded", 1D);
                        } else {
                            keptAmounts.add(amounts.get(i));
                            keptTimes.add(times.get(i));
                        }
                    }

                    if (keptAmounts.isEmpty()) {
                        stack.remove(RANDataComponentRegistry.RING_OF_THRIFT_REFUND_AMOUNTS.get());
                        stack.remove(RANDataComponentRegistry.RING_OF_THRIFT_REFUND_TIMES.get());
                    } else {
                        stack.set(RANDataComponentRegistry.RING_OF_THRIFT_REFUND_AMOUNTS.get(), keptAmounts);
                        stack.set(RANDataComponentRegistry.RING_OF_THRIFT_REFUND_TIMES.get(), keptTimes);
                    }
                }

                if (stack.getOrDefault(RANDataComponentRegistry.RING_OF_THRIFT_COMBO_UNTIL.get(), 0L) <= now)
                    stack.set(RANDataComponentRegistry.RING_OF_THRIFT_COMBO_STACKS.get(), 0);
            }

            if (refunded > 0D) {
                manaCap.addMana(refunded);
                manaCap.syncToClient(player);
            }
        }
    }
}

