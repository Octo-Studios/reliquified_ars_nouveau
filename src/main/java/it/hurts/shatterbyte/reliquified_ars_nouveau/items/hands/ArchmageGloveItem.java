package it.hurts.shatterbyte.reliquified_ars_nouveau.items.hands;

import com.hollingsworth.arsnouveau.api.event.EventQueue;
import com.hollingsworth.arsnouveau.api.event.ITimedEvent;
import com.hollingsworth.arsnouveau.api.event.SpellCastEvent;
import com.hollingsworth.arsnouveau.api.event.SpellCostCalcEvent;
import com.hollingsworth.arsnouveau.api.spell.AbstractCastMethod;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import com.hollingsworth.arsnouveau.api.spell.SpellResolver;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster;
import com.hollingsworth.arsnouveau.api.util.SpellUtil;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentSensitive;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.base.RANWearableRelicItem;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.base.loot.LootEntries;
import it.hurts.shatterbyte.reliquified_ars_nouveau.spell.augment.AugmentMulticast;
import it.hurts.sskirillss.relics.api.relics.AbilityMetricTemplate;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class ArchmageGloveItem extends RANWearableRelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("multicasted")
                                .rankModifier(1, "multicast_resistance")
                                .rankModifier(3, "adaptive_multicast")
                                .rankModifier(5, "mana_refund")
                                .stat(AbilityStatTemplate.builder("multicast_chance")
                                        .initialValue(0.05D, 0.1D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.18571D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("max_multicasts")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.06667D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("multicast_damage_reduction")
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.08571D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("failed_multicast_bonus")
                                        .initialValue(0.01D, 0.05D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.02857D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("mana_refund_chance")
                                        .initialValue(0.05D, 0.1D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.04286D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("mana_refund_percent")
                                        .initialValue(0.05D, 0.1D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.11429D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("multicast_trigger").build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("triggers")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("extra_casts")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("blocked_damage")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .rankModifierVisibilityState("multicast_resistance", it.hurts.sskirillss.relics.api.relics.VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("mana_restored")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .rankModifierVisibilityState("mana_refund", it.hurts.sskirillss.relics.api.relics.VisibilityState.OBFUSCATED)
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
        private static final String MULTICAST_CHILD_TAG = "ran_archmage_glove_multicast_child";
        private static final String MULTICAST_PROCESSED_TAG = "ran_archmage_glove_multicast_processed";

        @SubscribeEvent
        public static void onSpellCostCalcPre(SpellCostCalcEvent event) {
            if (event.context == null)
                return;

            if (!event.context.tag.getBoolean(MULTICAST_CHILD_TAG))
                return;

            event.currentCost = 0;
        }

        @SubscribeEvent
        public static void onSpellCast(SpellCastEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide())
                return;

            if (event.context.getPreviousContext() != null || event.context.tag.getBoolean(MULTICAST_CHILD_TAG) || event.context.tag.getBoolean(MULTICAST_PROCESSED_TAG))
                return;

            if (event.spell.getInstanceCount(AugmentMulticast.INSTANCE) != 1)
                return;

            var multicastIndex = event.spell.indexOf(AugmentMulticast.INSTANCE);

            if (multicastIndex < 0)
                return;

            var multicastAfterForm = multicastIndex == 1
                    && event.spell.size() > 0
                    && event.spell.get(0) instanceof AbstractCastMethod;

            if (!multicastAfterForm)
                return;

            var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.ARCHMAGE_GLOVE.get()).stream()
                    .filter(relicStack -> relicStack.getItem() instanceof ArchmageGloveItem)
                    .toList();

            if (stacks.isEmpty())
                return;

            event.context.tag.putBoolean(MULTICAST_PROCESSED_TAG, true);

            for (var stack : stacks) {
                if (!(stack.getItem() instanceof ArchmageGloveItem item))
                    continue;

                var relicData = item.getRelicData(player, stack);
                var ability = relicData.getAbilitiesData().getAbilityData("multicasted");

                if (!ability.canPlayerUse(player))
                    continue;

                var chance = ability.getStatData("multicast_chance").getValue();

                if (ability.isRankModifierUnlocked("adaptive_multicast")) {
                    var failedMulticasts = Math.max(0, stack.getOrDefault(RANDataComponentRegistry.ARCHMAGE_GLOVE_FAILED_MULTICASTS.get(), 0));
                    var failedBonus = Math.max(0D, ability.getStatData("failed_multicast_bonus").getValue());

                    chance += failedMulticasts * failedBonus;
                }

                chance = Mth.clamp(chance, 0D, 1D);
                var maxMulticasts = Math.max(1, (int) Math.round(Math.max(0D, ability.getStatData("max_multicasts").getValue())));
                var multicasts = MathUtils.multicast(player.getRandom(), chance, maxMulticasts);

                if (multicasts <= 0) {
                    if (ability.isRankModifierUnlocked("adaptive_multicast")) {
                        var failedMulticasts = Math.max(0, stack.getOrDefault(RANDataComponentRegistry.ARCHMAGE_GLOVE_FAILED_MULTICASTS.get(), 0));

                        stack.set(RANDataComponentRegistry.ARCHMAGE_GLOVE_FAILED_MULTICASTS.get(), failedMulticasts + 1);
                    }

                    continue;
                }

                if (ability.isRankModifierUnlocked("adaptive_multicast"))
                    stack.set(RANDataComponentRegistry.ARCHMAGE_GLOVE_FAILED_MULTICASTS.get(), 0);

                if (ability.isRankModifierUnlocked("multicast_resistance")) {
                    var currentTick = player.level().getGameTime();
                    var currentActiveUntil = stack.getOrDefault(RANDataComponentRegistry.ARCHMAGE_GLOVE_MULTICAST_ACTIVE_UNTIL.get(), 0L);
                    var activeTicks = Math.max(5L, multicasts * 5L);

                    stack.set(RANDataComponentRegistry.ARCHMAGE_GLOVE_MULTICAST_ACTIVE_UNTIL.get(), Math.max(currentActiveUntil, currentTick + activeTicks));
                }

                ability.getStatisticData().getMetricData("triggers").addValue(1D);
                var originalSpellCost = Math.max(0, new SpellResolver(event.context).withSilent(true).getExpendedCost());

                for (var i = 0; i < multicasts; i++) {
                    var spell = event.spell;
                    var casterTool = event.context.getCasterTool().copy();
                    var hand = ItemStack.isSameItemSameComponents(player.getOffhandItem(), casterTool)
                            ? InteractionHand.OFF_HAND
                            : InteractionHand.MAIN_HAND;
                    var delay = (i + 1) * 5;
                    var castXRot = player.getXRot();
                    var castYRot = player.getYRot();
                    var castYHeadRot = player.getYHeadRot();

                    EventQueue.getServerInstance().addEvent(new ITimedEvent() {
                        private int duration = delay;

                        @Override
                        public void tick(boolean serverSide) {
                            duration--;

                            if (!serverSide || duration > 0)
                                return;

                            if (!player.isAlive() || player.isRemoved()) {
                                if (ability.isRankModifierUnlocked("multicast_resistance")) {
                                    var currentTick = player.level().getGameTime();
                                    var activeUntil = stack.getOrDefault(RANDataComponentRegistry.ARCHMAGE_GLOVE_MULTICAST_ACTIVE_UNTIL.get(), 0L);

                                    stack.set(RANDataComponentRegistry.ARCHMAGE_GLOVE_MULTICAST_ACTIVE_UNTIL.get(), Math.min(activeUntil, currentTick));
                                }

                                return;
                            }

                            var previousXRot = player.getXRot();
                            var previousYRot = player.getYRot();
                            var previousYHeadRot = player.getYHeadRot();

                            try {
                                player.setXRot(castXRot);
                                player.setYRot(castYRot);
                                player.setYHeadRot(castYHeadRot);

                                var context = new SpellContext(player.serverLevel(), spell, player, new PlayerCaster(player), casterTool);
                                context.tag.putBoolean(MULTICAST_CHILD_TAG, true);
                                context.tag.putBoolean(MULTICAST_PROCESSED_TAG, true);
                                var resolver = new SpellResolver(context).withSilent(true);
                                var sensitive = resolver.spell.getBuffsAtIndex(0, player, AugmentSensitive.INSTANCE) > 0;
                                var interactionRange = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
                                var range = 0.5D + (interactionRange == null ? 4.5D : interactionRange.getValue());
                                var result = SpellUtil.rayTrace(player, range, 1F, sensitive);
                                var casted = false;

                                if (result instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof LivingEntity)
                                    casted = resolver.onCastOnEntity(casterTool, entityHitResult.getEntity(), hand);
                                else if (result instanceof BlockHitResult blockHitResult && (result.getType() == HitResult.Type.BLOCK || sensitive))
                                    casted = resolver.onCastOnBlock(new UseOnContext(player, hand, blockHitResult));
                                else
                                    casted = resolver.onCast(casterTool, player.serverLevel());

                                if (casted) {
                                    relicData.getLevelingData().addExperience("multicasted", "multicast_trigger", 1D);
                                    ability.getStatisticData().getMetricData("extra_casts").addValue(1D);

                                    if (ability.isRankModifierUnlocked("mana_refund") && originalSpellCost > 0) {
                                        var refundChance = Mth.clamp(ability.getStatData("mana_refund_chance").getValue(), 0D, 1D);

                                        if (refundChance > 0D && player.getRandom().nextDouble() < refundChance) {
                                            var manaCap = CapabilityRegistry.getMana(player);

                                            if (manaCap != null) {
                                                var refundFraction = Mth.clamp(ability.getStatData("mana_refund_percent").getValue(), 0D, 1D);
                                                var manaToRestore = Math.max(0D, originalSpellCost * refundFraction);

                                                if (manaToRestore > 0D) {
                                                    var manaBefore = manaCap.getCurrentMana();
                                                    manaCap.addMana(manaToRestore);
                                                    var restoredMana = Math.max(0D, manaCap.getCurrentMana() - manaBefore);
                                                    manaCap.syncToClient(player);

                                                    if (restoredMana > 0D)
                                                        ability.getStatisticData().getMetricData("mana_restored").addValue(restoredMana);
                                                }
                                            }
                                        }
                                    }
                                }
                            } finally {
                                player.setXRot(previousXRot);
                                player.setYRot(previousYRot);
                                player.setYHeadRot(previousYHeadRot);
                            }
                        }

                        @Override
                        public boolean isExpired() {
                            return duration <= 0;
                        }
                    });
                }
            }
        }

        @SubscribeEvent
        public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || event.getNewDamage() <= 0F)
                return;

            var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.ARCHMAGE_GLOVE.get()).stream()
                    .filter(relicStack -> relicStack.getItem() instanceof ArchmageGloveItem)
                    .toList();

            if (stacks.isEmpty())
                return;

            for (var stack : stacks) {
                if (event.getNewDamage() <= 0F)
                    break;

                if (!(stack.getItem() instanceof ArchmageGloveItem item))
                    continue;

                var ability = item.getRelicData(player, stack).getAbilitiesData().getAbilityData("multicasted");

                if (!ability.canPlayerUse(player) || !ability.isRankModifierUnlocked("multicast_resistance"))
                    continue;

                var currentTick = player.level().getGameTime();
                var activeUntil = stack.getOrDefault(RANDataComponentRegistry.ARCHMAGE_GLOVE_MULTICAST_ACTIVE_UNTIL.get(), 0L);

                if (currentTick >= activeUntil)
                    continue;

                var reductionFraction = Mth.clamp(ability.getStatData("multicast_damage_reduction").getValue(), 0D, 1D);

                if (reductionFraction <= 0D)
                    continue;

                var oldDamage = Math.max(0D, event.getNewDamage());
                var newDamage = Math.max(0D, oldDamage * (1D - reductionFraction));
                var blockedDamage = Math.max(0D, oldDamage - newDamage);

                if (blockedDamage <= 0D)
                    continue;

                event.setNewDamage((float) newDamage);
                ability.getStatisticData().getMetricData("blocked_damage").addValue(blockedDamage);
            }
        }
    }
}
