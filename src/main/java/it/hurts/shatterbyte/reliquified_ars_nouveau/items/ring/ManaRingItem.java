package it.hurts.shatterbyte.reliquified_ars_nouveau.items.ring;

import com.hollingsworth.arsnouveau.api.event.ManaRegenCalcEvent;
import com.hollingsworth.arsnouveau.api.event.MaxManaCalcEvent;
import com.hollingsworth.arsnouveau.api.event.SpellCostCalcEvent;
import com.hollingsworth.arsnouveau.api.util.ManaUtil;
import com.hollingsworth.arsnouveau.common.event.ManaCapEvents;
import com.hollingsworth.arsnouveau.common.capability.ManaCap;
import com.hollingsworth.arsnouveau.setup.config.ServerConfig;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class ManaRingItem extends RANWearableRelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("resonance")
                                .rankModifier(1, "mana_regen")
                                .rankModifier(3, "healing_to_mana")
                                .rankModifier(5, "mana_debt")
                                .stat(AbilityStatTemplate.builder("max_mana_bonus")
                                        .initialValue(0.1D, 0.25D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("mana_regen_bonus")
                                        .initialValue(0.05D, 0.15D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.5D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("mana_per_health")
                                        .initialValue(1D, 5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 25D)
                                        .formatValue(value -> MathUtils.round(value, 2))
                                        .build())
                                .stat(AbilityStatTemplate.builder("debt_cost_increase")
                                        .initialValue(1D, 0.75D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.25D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("healing_to_mana")
                                                .rankModifierVisibilityState("healing_to_mana", VisibilityState.OBFUSCATED)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("mana_borrowed")
                                                .rankModifierVisibilityState("mana_debt", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("mana_from_healing")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .rankModifierVisibilityState("healing_to_mana", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("mana_borrowed")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .rankModifierVisibilityState("mana_debt", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("mana_debt_repaid")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .rankModifierVisibilityState("mana_debt", VisibilityState.OBFUSCATED)
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
        public static void onMaxManaCalc(MaxManaCalcEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide())
                return;

            var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.MANA_RING.get()).stream()
                    .filter(relicStack -> relicStack.getItem() instanceof ManaRingItem)
                    .toList();

            if (stacks.isEmpty())
                return;

            var bonus = 0D;

            for (var stack : stacks) {
                if (!(stack.getItem() instanceof ManaRingItem item))
                    continue;

                var ability = item.getRelicData(player, stack).getAbilitiesData().getAbilityData("resonance");

                bonus += Math.max(0D, ability.getStatData("max_mana_bonus").getValue());
            }

            if (bonus > 0D)
                event.setMax((int) Math.round(event.getMax() * (1D + bonus)));
        }

        @SubscribeEvent
        public static void onManaRegenCalc(ManaRegenCalcEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide())
                return;

            var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.MANA_RING.get()).stream()
                    .filter(relicStack -> relicStack.getItem() instanceof ManaRingItem)
                    .toList();

            if (stacks.isEmpty())
                return;

            var bonus = 0D;

            for (var stack : stacks) {
                if (!(stack.getItem() instanceof ManaRingItem item))
                    continue;

                var ability = item.getRelicData(player, stack).getAbilitiesData().getAbilityData("resonance");

                if (!ability.getRankModifierData("mana_regen").isEnabled())
                    continue;

                bonus += Math.max(0D, ability.getStatData("mana_regen_bonus").getValue());
            }

            if (bonus > 0D)
                event.setRegen(event.getRegen() * (1D + bonus));
        }

        @SubscribeEvent
        public static void onLivingHeal(LivingHealEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || event.getAmount() <= 0F)
                return;

            var manaCap = CapabilityRegistry.getMana(player);

            if (manaCap == null)
                return;

            var synced = false;
            var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.MANA_RING.get()).stream()
                    .filter(relicStack -> relicStack.getItem() instanceof ManaRingItem)
                    .toList();

            if (stacks.isEmpty())
                return;

            for (var stack : stacks) {
                if (!(stack.getItem() instanceof ManaRingItem item))
                    continue;

                var relicData = item.getRelicData(player, stack);
                var ability = relicData.getAbilitiesData().getAbilityData("resonance");

                if (!ability.getRankModifierData("healing_to_mana").isEnabled())
                    continue;

                var manaPerHealth = Math.max(0D, ability.getStatData("mana_per_health").getValue());

                if (manaPerHealth <= 0D)
                    continue;

                var manaBefore = manaCap.getCurrentMana();

                manaCap.addMana(event.getAmount() * manaPerHealth);

                var restoredMana = Math.max(0D, manaCap.getCurrentMana() - manaBefore);

                if (restoredMana <= 0D)
                    continue;

                ability.getStatisticData().getMetricData("mana_from_healing").addValue(restoredMana);
                relicData.getLevelingData().addExperience("resonance", "healing_to_mana", 1D);
                synced = true;
            }

            if (synced)
                manaCap.syncToClient(player);
        }

        @SubscribeEvent
        public static void onSpellCostCalcPre(SpellCostCalcEvent.Pre event) {
            if (!(event.context.getCaster() instanceof com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster livingCaster)
                    || !(livingCaster.livingEntity instanceof ServerPlayer player) || player.level().isClientSide())
                return;

            var manaCap = CapabilityRegistry.getMana(player);

            if (manaCap == null || event.currentCost <= 0)
                return;

            var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.MANA_RING.get()).stream()
                    .filter(relicStack -> relicStack.getItem() instanceof ManaRingItem)
                    .toList();

            if (stacks.isEmpty())
                return;

            var debtStacks = stacks.stream()
                    .filter(stack -> {
                        if (!(stack.getItem() instanceof ManaRingItem item))
                            return false;

                        var ability = item.getRelicData(player, stack).getAbilitiesData().getAbilityData("resonance");

                        return ability.getRankModifierData("mana_debt").isEnabled();
                    })
                    .toList();

            if (debtStacks.isEmpty())
                return;

            var availableManaCost = Math.max(0, (int) Math.floor(Math.max(0D, manaCap.getCurrentMana())));

            if (availableManaCost >= event.currentCost)
                return;

            var borrowedMana = Math.max(0D, event.currentCost - availableManaCost);
            var debtPenalty = 0D;

            for (var stack : debtStacks) {
                var item = (ManaRingItem) stack.getItem();
                var ability = item.getRelicData(player, stack).getAbilitiesData().getAbilityData("resonance");

                debtPenalty += Math.max(0D, ability.getStatData("debt_cost_increase").getValue());
            }

            var debtToAdd = borrowedMana * (1D + debtPenalty);
            var perStackDebt = debtToAdd / (double) debtStacks.size();

            event.currentCost = Math.max(0, availableManaCost);

            for (var stack : debtStacks) {
                var item = (ManaRingItem) stack.getItem();
                var relicData = item.getRelicData(player, stack);
                var ability = relicData.getAbilitiesData().getAbilityData("resonance");

                stack.set(RANDataComponentRegistry.MANA_RING_DEBT.get(), Math.max(0D, stack.getOrDefault(RANDataComponentRegistry.MANA_RING_DEBT.get(), 0D) + perStackDebt));
                ability.getStatisticData().getMetricData("mana_borrowed").addValue(perStackDebt);
                relicData.getLevelingData().addExperience("resonance", "mana_borrowed", 1D);
            }
        }

        @SubscribeEvent
        public static void onPlayerTickPost(PlayerTickEvent.Post event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide())
                return;

            if (player.level().getGameTime() % ServerConfig.REGEN_INTERVAL.get() != 0)
                return;

            var manaCap = CapabilityRegistry.getMana(player);

            if (manaCap == null)
                return;

            var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.MANA_RING.get()).stream()
                    .filter(relicStack -> relicStack.getItem() instanceof ManaRingItem)
                    .toList();

            if (stacks.isEmpty())
                return;

            var regenBudget = ManaUtil.getManaRegen(player) / Math.max(1, ((int) ManaCapEvents.MEAN_TPS / ServerConfig.REGEN_INTERVAL.get()));
            var manaAvailable = Math.max(0D, manaCap.getCurrentMana());
            var repayBudget = Math.min(Math.max(0D, regenBudget), manaAvailable);

            if (repayBudget <= 0D)
                return;

            var repaidTotal = 0D;

            for (var stack : stacks) {
                if (!(stack.getItem() instanceof ManaRingItem item))
                    continue;

                var relicData = item.getRelicData(player, stack);
                var ability = relicData.getAbilitiesData().getAbilityData("resonance");
                var debt = Math.max(0D, stack.getOrDefault(RANDataComponentRegistry.MANA_RING_DEBT.get(), 0D));

                if (!ability.getRankModifierData("mana_debt").isEnabled() || debt <= 0D || repayBudget <= 0D)
                    continue;

                var repaid = Math.min(debt, repayBudget);

                stack.set(RANDataComponentRegistry.MANA_RING_DEBT.get(), Math.max(0D, debt - repaid));
                ability.getStatisticData().getMetricData("mana_debt_repaid").addValue(repaid);

                repaidTotal += repaid;
                repayBudget -= repaid;
            }

            if (repaidTotal > 0D) {
                manaCap.removeMana(repaidTotal);
                manaCap.syncToClient(player);
            }
        }
    }
}

