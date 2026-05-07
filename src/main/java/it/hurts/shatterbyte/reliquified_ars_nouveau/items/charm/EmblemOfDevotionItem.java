package it.hurts.shatterbyte.reliquified_ars_nouveau.items.charm;

import com.hollingsworth.arsnouveau.api.event.SpellCostCalcEvent;
import com.hollingsworth.arsnouveau.api.item.ICasterTool;
import com.hollingsworth.arsnouveau.api.spell.Spell;
import com.hollingsworth.arsnouveau.api.spell.SpellCaster;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import com.hollingsworth.arsnouveau.api.spell.SpellResolver;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster;
import com.hollingsworth.arsnouveau.client.gui.SpellTooltip;
import com.hollingsworth.arsnouveau.common.entity.EntityOrbitProjectile;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EmblemOfDevotionItem extends RANWearableRelicItem implements ICasterTool {
    public static final String DEVOTION_ORBIT_TAG = "ran_emblem_of_devotion_orbit";
    public static final String DEVOTION_STACK_ID_TAG = "ran_emblem_of_devotion_stack_id";
    public static final String DEVOTION_ORBIT_SLOT_TAG = "ran_emblem_of_devotion_orbit_slot";
    public static final String DEVOTION_ABSORPTION_AMOUNT_TAG = "ran_emblem_of_devotion_absorption_amount";
    public static final String DEVOTION_MAINTENANCE_TICK_TAG = "ran_emblem_of_devotion_maintenance_tick";

    public static final ResourceLocation DEVOTION_ABSORPTION_ATTRIBUTE_ID = ResourceLocation.fromNamespaceAndPath(ReliquifiedArsNouveau.MODID, "emblem_of_devotion_absorption");

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("devotion")
                                .rankModifier(1, "sphere_preservation")
                                .rankModifier(3, "absorption_burst")
                                .rankModifier(5, "spell_propagation")
                                .stat(AbilityStatTemplate.builder("max_glyphs")
                                        .initialValue(6D, 10D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> Math.max(1, (int) MathUtils.round(value, 0)))
                                        .build())
                                .stat(AbilityStatTemplate.builder("spawn_interval")
                                        .initialValue(4D, 2D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), -0.05714D)
                                        .formatValue(value -> MathUtils.round(value, 2))
                                        .build())
                                .stat(AbilityStatTemplate.builder("max_orbs")
                                        .initialValue(2D, 5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.07619D)
                                        .formatValue(value -> Math.max(1, (int) MathUtils.round(value, 0)))
                                        .build())
                                .stat(AbilityStatTemplate.builder("preservation_chance")
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("absorption_hearts")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.06667D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("absorption_cap")
                                        .initialValue(4D, 8D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("propagation_chance")
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("propagation_targets")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> Math.max(1, (int) MathUtils.round(value, 0)))
                                        .build())
                                .stat(AbilityStatTemplate.builder("propagation_radius")
                                        .initialValue(3D, 6D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("sphere_hit").build())
                                        .source(ExperienceSourceTemplate.builder("sphere_preserved")
                                                .rankModifierVisibilityState("sphere_preservation", VisibilityState.OBFUSCATED)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("chain_hit")
                                                .rankModifierVisibilityState("spell_propagation", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("orbs_summoned")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("sphere_hits")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("spheres_preserved")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .rankModifierVisibilityState("sphere_preservation", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("absorption_granted")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .rankModifierVisibilityState("absorption_burst", VisibilityState.OBFUSCATED)
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

        var maxGlyphs = Math.max(1, (int) Math.round(Math.max(0D, this.getRelicData(null, stack).getAbilitiesData().getAbilityData("devotion").getStatData("max_glyphs").getValue())));

        return spell.size() <= maxGlyphs;
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

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof ServerPlayer player) || player.level().isClientSide())
            return;

        var data = player.getPersistentData();
        var gameTime = player.level().getGameTime();

        if (data.getLong(DEVOTION_MAINTENANCE_TICK_TAG) != gameTime) {
            data.putLong(DEVOTION_MAINTENANCE_TICK_TAG, gameTime);

            var activeStackIds = EntityUtils.findEquippedCurios(player, ItemRegistry.EMBLEM_OF_DEVOTION.get()).stream()
                    .filter(relicStack -> relicStack.getItem() instanceof EmblemOfDevotionItem)
                    .map(relicStack -> relicStack.getOrDefault(RANDataComponentRegistry.EMBLEM_OF_DEVOTION_STACK_ID.get(), ""))
                    .filter(stackId -> !stackId.isBlank())
                    .collect(java.util.stream.Collectors.toSet());

            var absorptionAmount = Math.max(0D, data.getDouble(DEVOTION_ABSORPTION_AMOUNT_TAG));

            if (activeStackIds.isEmpty()) {
                EntityUtils.removeAttribute(player, Attributes.MAX_ABSORPTION, AttributeModifier.Operation.ADD_VALUE, DEVOTION_ABSORPTION_ATTRIBUTE_ID);
                player.setAbsorptionAmount(0F);
                data.remove(DEVOTION_ABSORPTION_AMOUNT_TAG);
            } else if (absorptionAmount > 0D) {
                EntityUtils.resetAttribute(player, Attributes.MAX_ABSORPTION, (float) absorptionAmount, AttributeModifier.Operation.ADD_VALUE, DEVOTION_ABSORPTION_ATTRIBUTE_ID);
                if (player.getAbsorptionAmount() > (float) absorptionAmount)
                    player.setAbsorptionAmount((float) absorptionAmount);
            } else {
                EntityUtils.removeAttribute(player, Attributes.MAX_ABSORPTION, AttributeModifier.Operation.ADD_VALUE, DEVOTION_ABSORPTION_ATTRIBUTE_ID);
                data.remove(DEVOTION_ABSORPTION_AMOUNT_TAG);
            }

            for (var orb : player.serverLevel().getEntitiesOfClass(EntityOrbitProjectile.class, player.getBoundingBox().inflate(48D),
                    projectile -> projectile.isAlive()
                            && projectile.getPersistentData().getBoolean(DEVOTION_ORBIT_TAG)
                            && projectile.getOwner() == player)) {
                var orbStackId = orb.getPersistentData().getString(DEVOTION_STACK_ID_TAG);

                if (orbStackId.isBlank() || !activeStackIds.contains(orbStackId))
                    orb.discard();
            }
        }

        var relicData = this.getRelicData(player, stack);
        var ability = relicData.getAbilitiesData().getAbilityData("devotion");

        var caster = this.getSpellCaster(stack);

        if (caster == null) {
            caster = new SpellCaster(1);
            stack.set(DataComponentRegistry.SPELL_CASTER.get(), caster);
        }

        var spell = caster.getSpell();

        if (!spell.isValid() || spell.size() <= 1 || !(spell.get(0) instanceof MethodTouch))
            return;

        var stackId = stack.get(RANDataComponentRegistry.EMBLEM_OF_DEVOTION_STACK_ID.get());

        if (stackId == null || stackId.isBlank()) {
            stackId = UUID.randomUUID().toString();
            stack.set(RANDataComponentRegistry.EMBLEM_OF_DEVOTION_STACK_ID.get(), stackId);
        }

        var finalStackId = stackId;
        
        var nextSpawnTick = stack.getOrDefault(RANDataComponentRegistry.EMBLEM_OF_DEVOTION_NEXT_SPAWN_TICK.get(), 0L);

        if (gameTime < nextSpawnTick)
            return;

        var maxOrbs = Math.max(1, (int) Math.round(Math.max(0D, ability.getStatData("max_orbs").getValue())));
        var orbs = player.serverLevel().getEntitiesOfClass(EntityOrbitProjectile.class, player.getBoundingBox().inflate(24D), projectile ->
                projectile.isAlive()
                        && projectile.getPersistentData().getBoolean(DEVOTION_ORBIT_TAG)
                        && finalStackId.equals(projectile.getPersistentData().getString(DEVOTION_STACK_ID_TAG)));
        var activeOrbs = orbs.size();
        var intervalTicks = Math.max(1L, Math.round(Math.max(0.05D, ability.getStatData("spawn_interval").getValue()) * 20D));

        stack.set(RANDataComponentRegistry.EMBLEM_OF_DEVOTION_NEXT_SPAWN_TICK.get(), gameTime + intervalTicks);

        if (activeOrbs >= maxOrbs)
            return;

        var occupiedSlots = orbs.stream()
                .map(projectile -> projectile.getPersistentData().contains(DEVOTION_ORBIT_SLOT_TAG)
                        ? projectile.getPersistentData().getInt(DEVOTION_ORBIT_SLOT_TAG)
                        : -1)
                .filter(slot -> slot >= 0 && slot < maxOrbs)
                .collect(java.util.stream.Collectors.toSet());
        var orbitSlot = 0;

        while (orbitSlot < maxOrbs && occupiedSlots.contains(orbitSlot))
            orbitSlot++;

        if (orbitSlot >= maxOrbs)
            orbitSlot = activeOrbs % maxOrbs;

        var baseOffset = Math.floorMod(finalStackId.hashCode(), 32);
        var effectiveOffset = baseOffset + orbitSlot;

        var resolver = new SpellResolver(new SpellContext(player.serverLevel(), new Spell(), player, new PlayerCaster(player), stack)).withSilent(true);
        var orb = new EntityOrbitProjectile(player.serverLevel(), resolver, player);

        orb.setOffset(effectiveOffset);
        orb.setTotal(maxOrbs);
        orb.setAccelerates(0);
        orb.setAoe(0F);
        orb.extendTimes = 0;
        orb.getPersistentData().putBoolean(DEVOTION_ORBIT_TAG, true);
        orb.getPersistentData().putString(DEVOTION_STACK_ID_TAG, finalStackId);
        orb.getPersistentData().putInt(DEVOTION_ORBIT_SLOT_TAG, orbitSlot);

        player.serverLevel().addFreshEntity(orb);

        ability.getStatisticData().getMetricData("orbs_summoned").addValue(1D);
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (!(slotContext.entity() instanceof ServerPlayer player) || player.level().isClientSide() || newStack.getItem() == stack.getItem())
            return;

        var stackId = stack.getOrDefault(RANDataComponentRegistry.EMBLEM_OF_DEVOTION_STACK_ID.get(), "");

        if (stackId.isBlank())
            return;

        for (var orb : player.serverLevel().getEntitiesOfClass(EntityOrbitProjectile.class, player.getBoundingBox().inflate(64D),
                projectile -> projectile.isAlive()
                        && projectile.getOwner() == player
                        && projectile.getPersistentData().getBoolean(DEVOTION_ORBIT_TAG)
                        && stackId.equals(projectile.getPersistentData().getString(DEVOTION_STACK_ID_TAG)))) {
            orb.discard();
        }

        EntityUtils.removeAttribute(player, Attributes.MAX_ABSORPTION, AttributeModifier.Operation.ADD_VALUE, DEVOTION_ABSORPTION_ATTRIBUTE_ID);

        player.setAbsorptionAmount(0F);

        var data = player.getPersistentData();

        data.remove(DEVOTION_ABSORPTION_AMOUNT_TAG);
    }

    @EventBusSubscriber(modid = ReliquifiedArsNouveau.MODID)
    public static class CommonEvents {
        @SubscribeEvent
        public static void onSpellCostCalc(SpellCostCalcEvent event) {
            if (event.context == null || event.context.getCasterTool().isEmpty())
                return;

            if (event.context.getCasterTool().getItem() instanceof EmblemOfDevotionItem)
                event.currentCost = 0;
        }
    }
}
