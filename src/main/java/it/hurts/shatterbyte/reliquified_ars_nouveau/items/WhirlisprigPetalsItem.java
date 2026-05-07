package it.hurts.shatterbyte.reliquified_ars_nouveau.items;

import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class WhirlisprigPetalsItem extends RANWearableRelicItem {
    private static final ResourceLocation WHIRLISPRIG_PETALS_GRAVITY_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(ReliquifiedArsNouveau.MODID, "whirlisprig_petals_slow_fall_gravity");

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("petal_lift")
                                .rankModifier(1, "slow_fall")
                                .rankModifier(3, "directed_lift")
                                .rankModifier(5, "airborne_volley")
                                .stat(AbilityStatTemplate.builder("max_charges")
                                        .initialValue(3D, 6D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.08571D)
                                        .formatValue(value -> Math.max(1, (int) MathUtils.round(value, 0)))
                                        .build())
                                .stat(AbilityStatTemplate.builder("charge_regen_time")
                                        .initialValue(8D, 5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), -0.04286D)
                                        .formatValue(value -> MathUtils.round(value, 2))
                                        .build())
                                .stat(AbilityStatTemplate.builder("lift_speed")
                                        .initialValue(2D, 5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> MathUtils.round(value, 2))
                                        .build())
                                .stat(AbilityStatTemplate.builder("ranged_bonus_damage")
                                        .initialValue(0.15D, 0.35D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("lift_time").build())
                                        .source(ExperienceSourceTemplate.builder("projectile_reflect")
                                                .rankModifierVisibilityState("directed_lift", VisibilityState.OBFUSCATED)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("airborne_hit")
                                                .rankModifierVisibilityState("airborne_volley", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("lift_time")
                                                .formatValue(value -> MathUtils.formatTime((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("projectiles_reflected")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .rankModifierVisibilityState("directed_lift", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("bonus_damage_dealt")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .rankModifierVisibilityState("airborne_volley", VisibilityState.OBFUSCATED)
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
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);

        if (player instanceof ServerPlayer serverPlayer) {
            var relicData = this.getRelicData(serverPlayer, stack);
            var ability = relicData.getAbilitiesData().getAbilityData("petal_lift");

            var maxCharges = Math.max(1, (int) Math.round(Math.max(0D, ability.getStatData("max_charges").getValue())));
            var charges = Mth.clamp(stack.getOrDefault(RANDataComponentRegistry.WHIRLISPRIG_PETALS_CHARGES.get(), maxCharges), 0, maxCharges);

            if (charges <= 0)
                return InteractionResultHolder.fail(stack);

            var launchHeight = Math.max(0D, ability.getStatData("lift_speed").getValue());

            if (launchHeight <= 0D)
                return InteractionResultHolder.fail(stack);

            var wasActive = stack.getOrDefault(RANDataComponentRegistry.WHIRLISPRIG_PETALS_ACTIVE.get(), false);
            var bonusStacks = Math.max(0, stack.getOrDefault(RANDataComponentRegistry.WHIRLISPRIG_PETALS_BONUS_STACKS.get(), 0));

            charges--;
            stack.set(RANDataComponentRegistry.WHIRLISPRIG_PETALS_CHARGES.get(), charges);
            stack.set(RANDataComponentRegistry.WHIRLISPRIG_PETALS_REGEN_TICKS.get(), 0);
            stack.set(RANDataComponentRegistry.WHIRLISPRIG_PETALS_ACTIVE.get(), true);
            stack.set(RANDataComponentRegistry.WHIRLISPRIG_PETALS_LIFT_TICKS.get(), 80);
            if (wasActive)
                stack.set(RANDataComponentRegistry.WHIRLISPRIG_PETALS_BONUS_STACKS.get(), bonusStacks + 1);

            var jumpVelocity = Math.sqrt(0.16D * launchHeight);
            var motion = serverPlayer.getDeltaMovement();

            if (ability.getRankModifierData("directed_lift").isEnabled()) {
                var look = serverPlayer.getLookAngle();
                var lookLength = look.lengthSqr() > 1.0E-6D ? look.normalize() : new Vec3(0, 1, 0);

                serverPlayer.setDeltaMovement(motion.multiply(1,0.5F,1).add(lookLength.scale(jumpVelocity)));

                relicData.getLevelingData().addExperience("petal_lift", "projectile_reflect", 1D);
                ability.getStatisticData().getMetricData("projectiles_reflected").addValue(1D);
            } else {
                var nextY = Math.max(motion.y, jumpVelocity);
                serverPlayer.setDeltaMovement(motion.x, nextY, motion.z);
            }

            serverPlayer.fallDistance = 0F;
            serverPlayer.hurtMarked = true;
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        var charges = stack.getOrDefault(RANDataComponentRegistry.WHIRLISPRIG_PETALS_CHARGES.get(), 0);
        var maxCharges = Math.max(1, (int) Math.round(Math.max(0D, this.getRelicData(null, stack).getAbilitiesData().getAbilityData("petal_lift").getStatData("max_charges").getValue())));

        return Mth.clamp((int) Math.floor(charges * 13D / maxCharges), 0, 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x9AEAF8;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!(entity instanceof Player player))
            return;

        var relicData = this.getRelicData(player, stack);
        var ability = relicData.getAbilitiesData().getAbilityData("petal_lift");
        var maxCharges = Math.max(1, (int) Math.round(Math.max(0D, ability.getStatData("max_charges").getValue())));
        var charges = Mth.clamp(stack.getOrDefault(RANDataComponentRegistry.WHIRLISPRIG_PETALS_CHARGES.get(), maxCharges), 0, maxCharges);
        var inMainHand = player.getMainHandItem() == stack;
        var inOffHand = player.getOffhandItem() == stack;
        var assistedTicks = Math.max(0, stack.getOrDefault(RANDataComponentRegistry.WHIRLISPRIG_PETALS_LIFT_TICKS.get(), 0));
        var hasSlowFallGravityModifier = EntityUtils.hasAttribute(player, Attributes.GRAVITY, WHIRLISPRIG_PETALS_GRAVITY_MODIFIER_ID);
        var regenLockedUntilLand = stack.getOrDefault(RANDataComponentRegistry.WHIRLISPRIG_PETALS_ACTIVE.get(), false);

        if (assistedTicks > 0) {
            if (!(inMainHand || inOffHand))
                assistedTicks = 0;
            else
                assistedTicks--;
        }

        var shouldHaveSlowFallGravity = ability.getRankModifierData("slow_fall").isEnabled()
                && (inMainHand || inOffHand)
                && !player.onGround()
                && !player.isShiftKeyDown();

        if (shouldHaveSlowFallGravity) {
            player.fallDistance = 0F;

            if (!hasSlowFallGravityModifier)
                EntityUtils.applyAttribute(player, Attributes.GRAVITY, -0.5F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, WHIRLISPRIG_PETALS_GRAVITY_MODIFIER_ID);
        } else if (hasSlowFallGravityModifier)
            EntityUtils.removeAttribute(player, Attributes.GRAVITY, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, WHIRLISPRIG_PETALS_GRAVITY_MODIFIER_ID);

        if (assistedTicks > 0)
            stack.set(RANDataComponentRegistry.WHIRLISPRIG_PETALS_LIFT_TICKS.get(), assistedTicks);
        else
            stack.remove(RANDataComponentRegistry.WHIRLISPRIG_PETALS_LIFT_TICKS.get());

        if (regenLockedUntilLand && player.onGround() && assistedTicks <= 0) {
            regenLockedUntilLand = false;

            stack.set(RANDataComponentRegistry.WHIRLISPRIG_PETALS_ACTIVE.get(), false);
            stack.set(RANDataComponentRegistry.WHIRLISPRIG_PETALS_REGEN_TICKS.get(), 0);
            stack.set(RANDataComponentRegistry.WHIRLISPRIG_PETALS_BONUS_STACKS.get(), 0);
        }

        var regenIntervalTicks = Math.max(1, (int) Math.round(Math.max(0.05D, ability.getStatData("charge_regen_time").getValue()) * 20D));
        var regenTicks = Math.max(0, stack.getOrDefault(RANDataComponentRegistry.WHIRLISPRIG_PETALS_REGEN_TICKS.get(), 0));

        if (!regenLockedUntilLand && charges < maxCharges) {
            regenTicks++;

            while (regenTicks >= regenIntervalTicks && charges < maxCharges) {
                regenTicks -= regenIntervalTicks;
                charges++;
            }
        } else {
            regenTicks = 0;
        }

        stack.set(RANDataComponentRegistry.WHIRLISPRIG_PETALS_CHARGES.get(), charges);
        stack.set(RANDataComponentRegistry.WHIRLISPRIG_PETALS_REGEN_TICKS.get(), regenTicks);
    }

    @EventBusSubscriber(modid = ReliquifiedArsNouveau.MODID)
    public static class CommonEvents {
        @SubscribeEvent
        public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
            if (!(event.getSource().getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || event.getEntity() == player || event.getNewDamage() <= 0F)
                return;

            var baseDamage = Math.max(0D, event.getNewDamage());
            var totalBonus = 0D;

            for (var stack : new ItemStack[]{player.getMainHandItem(), player.getOffhandItem()}) {
                if (!(stack.getItem() instanceof WhirlisprigPetalsItem item))
                    continue;

                var relicData = item.getRelicData(player, stack);
                var ability = relicData.getAbilitiesData().getAbilityData("petal_lift");
                var bonusStacks = Math.max(0, stack.getOrDefault(RANDataComponentRegistry.WHIRLISPRIG_PETALS_BONUS_STACKS.get(), 0));

                if (bonusStacks <= 0)
                    continue;

                stack.set(RANDataComponentRegistry.WHIRLISPRIG_PETALS_BONUS_STACKS.get(), 0);

                if (!ability.getRankModifierData("airborne_volley").isEnabled() || player.onGround())
                    continue;

                var bonusPerStack = Math.max(0D, ability.getStatData("ranged_bonus_damage").getValue());
                var bonus = bonusStacks * bonusPerStack;

                if (bonus <= 0D)
                    continue;

                totalBonus += bonus;
                relicData.getLevelingData().addExperience("petal_lift", "airborne_hit", 1D);
                ability.getStatisticData().getMetricData("bonus_damage_dealt").addValue(baseDamage * bonus);
            }

            if (totalBonus > 0D)
                event.setNewDamage((float) Math.max(0D, baseDamage * (1D + totalBonus)));
        }
    }
}
