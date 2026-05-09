package it.hurts.shatterbyte.reliquified_ars_nouveau.items.charm;

import com.hollingsworth.arsnouveau.api.item.ICasterTool;
import com.hollingsworth.arsnouveau.api.spell.Spell;
import com.hollingsworth.arsnouveau.api.spell.SpellCaster;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import com.hollingsworth.arsnouveau.api.spell.SpellResolver;
import com.hollingsworth.arsnouveau.api.event.SpellCostCalcEvent;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster;
import com.hollingsworth.arsnouveau.client.gui.SpellTooltip;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import com.hollingsworth.arsnouveau.setup.config.Config;
import com.hollingsworth.arsnouveau.setup.registry.DataComponentRegistry;
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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class EmblemOfAssaultItem extends RANWearableRelicItem implements ICasterTool {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("onslaught")
                                .rankModifier(1, "vampiric_burst")
                                .rankModifier(3, "war_frenzy")
                                .rankModifier(5, "spell_propagation")
                                .stat(AbilityStatTemplate.builder("max_glyphs")
                                        .initialValue(1D, 3D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 8.99550D)
                                        .formatValue(value -> Math.max(1, (int) MathUtils.round(value, 0)))
                                        .build())
                                .stat(AbilityStatTemplate.builder("trigger_chance")
                                        .initialValue(0.1D, 0.25D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.74963D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("heal_on_trigger")
                                        .initialValue(1D, 2.5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 9.99875D)
                                        .formatValue(value -> MathUtils.round(value, 2))
                                        .build())
                                .stat(AbilityStatTemplate.builder("damage_boost_duration")
                                        .initialValue(2.5D, 5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 10.00500D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("damage_boost")
                                        .initialValue(0.05D, 0.15D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.50017D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("propagation_chance")
                                        .initialValue(0.1D, 0.25D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.74963D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("propagation_targets")
                                        .initialValue(1D, 3D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 10.00350D)
                                        .formatValue(value -> Math.max(1, (int) MathUtils.round(value, 0)))
                                        .build())
                                .stat(AbilityStatTemplate.builder("propagation_radius")
                                        .initialValue(2.5D, 5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 10.00500D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("spell_trigger").build())
                                        .source(ExperienceSourceTemplate.builder("healed_self")
                                                .rankModifierVisibilityState("vampiric_burst", VisibilityState.OBFUSCATED)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("chain_hit")
                                                .rankModifierVisibilityState("spell_propagation", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("spell_triggers")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("health_restored")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .rankModifierVisibilityState("vampiric_burst", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("bonus_damage_dealt")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .rankModifierVisibilityState("war_frenzy", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("extra_targets_hit")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .rankModifierVisibilityState("spell_propagation", VisibilityState.OBFUSCATED)
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
    public @Nullable SpellCaster getSpellCaster(ItemStack stack) {
        return stack.get(DataComponentRegistry.SPELL_CASTER.get());
    }

    @Override
    public boolean onScribe(Level world, BlockPos pos, Player player, InteractionHand handIn, ItemStack thisStack) {
        if (!thisStack.has(DataComponentRegistry.SPELL_CASTER.get()))
            thisStack.set(DataComponentRegistry.SPELL_CASTER.get(), new SpellCaster(1));

        return ICasterTool.super.onScribe(world, pos, player, handIn, thisStack);
    }

    @Override
    public boolean isScribedSpellValid(com.hollingsworth.arsnouveau.api.spell.AbstractCaster<?> caster, Player player, InteractionHand hand, ItemStack stack, Spell spell) {
        if (spell == null || !spell.isValid() || spell.size() <= 0)
            return false;

        if (!(spell.get(0) instanceof MethodTouch))
            return false;

        var maxGlyphs = Math.max(1, (int) Math.round(Math.max(0D, this.getRelicData(null, stack).getAbilitiesData().getAbilityData("onslaught").getStatData("max_glyphs").getValue())));

        return spell.size() <= maxGlyphs + 1;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<net.minecraft.network.chat.Component> tooltip, @NotNull TooltipFlag flag) {
        stack.addToTooltip(DataComponentRegistry.SPELL_CASTER, context, tooltip::add, flag);
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack stack) {
        var caster = this.getSpellCaster(stack);

        if (caster != null && Config.GLYPH_TOOLTIPS.get() && !Screen.hasShiftDown() && !caster.isSpellHidden() && !caster.getSpell().isEmpty())
            return Optional.of(new SpellTooltip(caster));

        return Optional.empty();
    }

    @EventBusSubscriber(modid = ReliquifiedArsNouveau.MODID)
    public static class CommonEvents {
        @SubscribeEvent
        public static void onSpellCostCalc(SpellCostCalcEvent event) {
            if (event.context == null || event.context.getCasterTool().isEmpty())
                return;

            if (event.context.getCasterTool().getItem() instanceof EmblemOfAssaultItem)
                event.currentCost = 0;
        }

        @SubscribeEvent
        public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
            if (!(event.getSource().getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || event.getEntity() == player || event.getNewDamage() <= 0F)
                return;

            var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.EMBLEM_OF_ASSAULT.get()).stream()
                    .filter(relicStack -> relicStack.getItem() instanceof EmblemOfAssaultItem)
                    .toList();

            if (stacks.isEmpty())
                return;

            var currentTick = player.level().getGameTime();
            var baseDamage = Math.max(0D, event.getNewDamage());
            var totalBoost = 0D;

            for (var stack : stacks) {
                if (!(stack.getItem() instanceof EmblemOfAssaultItem item))
                    continue;

                var ability = item.getRelicData(player, stack).getAbilitiesData().getAbilityData("onslaught");

                if (!ability.getRankModifierData("war_frenzy").isEnabled())
                    continue;

                var activeUntil = stack.getOrDefault(RANDataComponentRegistry.EMBLEM_OF_ASSAULT_DAMAGE_UNTIL.get(), 0L);

                if (activeUntil <= currentTick)
                    continue;

                var boost = Math.max(0D, ability.getStatData("damage_boost").getValue());

                if (boost <= 0D)
                    continue;

                totalBoost += boost;
                item.getRelicData(player, stack).getAbilitiesData().getAbilityData("onslaught").getStatisticData().getMetricData("bonus_damage_dealt").addValue(baseDamage * boost);
            }

            if (totalBoost > 0D)
                event.setNewDamage((float) Math.max(0D, baseDamage * (1D + totalBoost)));
        }

        @SubscribeEvent
        public static void onLivingDamagePost(LivingDamageEvent.Post event) {
            if (!(event.getSource().getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || event.getEntity() == player || event.getNewDamage() <= 0F
                    || event.getSource().getDirectEntity() != player)
                return;

            if (!(event.getEntity() instanceof LivingEntity target) || !target.isAlive())
                return;

            var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.EMBLEM_OF_ASSAULT.get()).stream()
                    .filter(relicStack -> relicStack.getItem() instanceof EmblemOfAssaultItem)
                    .toList();

            if (stacks.isEmpty())
                return;

            var currentTick = player.level().getGameTime();
            for (var stack : stacks) {
                if (!(stack.getItem() instanceof EmblemOfAssaultItem item))
                    continue;

                var relicData = item.getRelicData(player, stack);
                var ability = relicData.getAbilitiesData().getAbilityData("onslaught");

                var caster = item.getSpellCaster(stack);

                if (caster == null) {
                    caster = new SpellCaster(1);
                    stack.set(DataComponentRegistry.SPELL_CASTER.get(), caster);
                }

                var spell = caster.getSpell();

                if (spell == null || !spell.isValid() || spell.size() <= 0 || !(spell.get(0) instanceof MethodTouch))
                    continue;

                var triggerChance = Mth.clamp(ability.getStatData("trigger_chance").getValue(), 0D, 1D);

                if (triggerChance <= 0D || player.getRandom().nextDouble() >= triggerChance)
                    continue;

                var resolver = new SpellResolver(new SpellContext(player.serverLevel(), spell, player, new PlayerCaster(player), stack)).withSilent(true);
                var casted = resolver.onCastOnEntity(stack, target, InteractionHand.MAIN_HAND);

                if (!casted)
                    continue;

                ability.getStatisticData().getMetricData("spell_triggers").addValue(1D);
                relicData.getLevelingData().addExperience("onslaught", "spell_trigger", 1D);

                if (ability.getRankModifierData("vampiric_burst").isEnabled()) {
                    var healAmount = (float) Math.max(0D, ability.getStatData("heal_on_trigger").getValue());

                    if (healAmount > 0F) {
                        var before = player.getHealth();

                        player.heal(healAmount);

                        var restored = Math.max(0F, player.getHealth() - before);

                        if (restored > 0F) {
                            ability.getStatisticData().getMetricData("health_restored").addValue(restored);
                            relicData.getLevelingData().addExperience("onslaught", "healed_self", 1D);
                        }
                    }
                }

                if (ability.getRankModifierData("war_frenzy").isEnabled()) {
                    var durationTicks = Math.max(1L, Math.round(Math.max(0D, ability.getStatData("damage_boost_duration").getValue()) * 20D));
                    var activeUntil = stack.getOrDefault(RANDataComponentRegistry.EMBLEM_OF_ASSAULT_DAMAGE_UNTIL.get(), 0L);

                    stack.set(RANDataComponentRegistry.EMBLEM_OF_ASSAULT_DAMAGE_UNTIL.get(), Math.max(activeUntil, currentTick + durationTicks));
                }

                if (ability.getRankModifierData("spell_propagation").isEnabled()) {
                    var propagationChance = Mth.clamp(ability.getStatData("propagation_chance").getValue(), 0D, 1D);

                    if (propagationChance > 0D) {
                        var propagationRadius = Math.max(0D, ability.getStatData("propagation_radius").getValue());
                        var maxPropagationTargets = Math.max(1, (int) Math.round(Math.max(0D, ability.getStatData("propagation_targets").getValue())));
                        var propagationTargets = MathUtils.multicast(player.getRandom(), propagationChance, maxPropagationTargets);

                        if (propagationRadius > 0D && propagationTargets > 0) {
                            var neighbors = player.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(propagationRadius),
                                            neighbor -> neighbor != player && neighbor != target && neighbor.isAlive() && !EntityUtils.isAlliedTo(player, neighbor))
                                    .stream()
                                    .sorted(Comparator.comparingDouble(target::distanceToSqr))
                                    .limit(propagationTargets)
                                    .toList();

                            if (!neighbors.isEmpty()) {
                                var hits = 0;

                                for (var neighbor : neighbors) {
                                    var chainedResolver = new SpellResolver(new SpellContext(player.serverLevel(), spell, player, new PlayerCaster(player), stack)).withSilent(true);

                                    if (chainedResolver.onCastOnEntity(stack, neighbor, InteractionHand.MAIN_HAND))
                                        hits++;
                                }

                                if (hits > 0) {
                                    ability.getStatisticData().getMetricData("extra_targets_hit").addValue(hits);
                                    relicData.getLevelingData().addExperience("onslaught", "chain_hit", hits);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
