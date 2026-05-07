package it.hurts.shatterbyte.reliquified_ars_nouveau.items.bracelet;

import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.shatterbyte.reliquified_ars_nouveau.entities.BallistarianPhantomProjectileEntity;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.EntityRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.base.RANWearableRelicItem;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.base.loot.LootEntries;
import it.hurts.sskirillss.relics.api.relics.AbilityMetricTemplate;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourceTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import top.theillusivec4.curios.api.SlotContext;

public class BallistarianBracerItem extends RANWearableRelicItem {
    private static final ResourceLocation BALLISTARIAN_MAX_ABSORPTION_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(ReliquifiedArsNouveau.MODID, "ballistarian_max_absorption");

    private static double getBallistarianAbsorptionCap(ServerPlayer player) {
        var totalMaxAbsorption = 0D;

        for (var stack : EntityUtils.findEquippedCurios(player, ItemRegistry.BALLISTARIAN_BRACER.get())) {
            if (!(stack.getItem() instanceof BallistarianBracerItem item))
                continue;

            var ability = item.getRelicData(player, stack).getAbilitiesData().getAbilityData("ballistary");

            if (!ability.getRankModifierData("absorbing_kills").isEnabled())
                continue;

            totalMaxAbsorption += Math.max(0D, ability.getStatData("max_absorption").getValue());
        }

        return Math.max(0D, totalMaxAbsorption);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof ServerPlayer player) || player.level().isClientSide())
            return;

        var totalMaxAbsorption = getBallistarianAbsorptionCap(player);

        if (totalMaxAbsorption > 0D)
            EntityUtils.resetAttribute(player, Attributes.MAX_ABSORPTION, (float) totalMaxAbsorption, AttributeModifier.Operation.ADD_VALUE, BALLISTARIAN_MAX_ABSORPTION_MODIFIER_ID);
        else
            EntityUtils.removeAttribute(player, Attributes.MAX_ABSORPTION, AttributeModifier.Operation.ADD_VALUE, BALLISTARIAN_MAX_ABSORPTION_MODIFIER_ID);

        if (totalMaxAbsorption > 0D && player.getAbsorptionAmount() > (float) totalMaxAbsorption)
            player.setAbsorptionAmount((float) totalMaxAbsorption);
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (!(slotContext.entity() instanceof ServerPlayer player) || player.level().isClientSide() || newStack.getItem() == stack.getItem())
            return;

        EntityUtils.removeAttribute(player, Attributes.MAX_ABSORPTION, AttributeModifier.Operation.ADD_VALUE, BALLISTARIAN_MAX_ABSORPTION_MODIFIER_ID);
        player.setAbsorptionAmount(0F);
    }

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("ballistary")
                                .rankModifier(1, "absorbing_kills")
                                .rankModifier(3, "multicast_duplication")
                                .rankModifier(5, "paralyzing_shots")
                                .stat(AbilityStatTemplate.builder("trigger_chance")
                                        .initialValue(0.2D, 0.35D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.07143D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("projectile_count")
                                        .initialValue(2D, 4D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> Math.max(1, (int) MathUtils.round(value, 0)))
                                        .build())
                                .stat(AbilityStatTemplate.builder("projectile_damage")
                                        .initialValue(2D, 4D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("absorption_on_kill")
                                        .initialValue(1D, 2D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("max_absorption")
                                        .initialValue(6D, 12D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("duplication_chance")
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.07143D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("duplication_radius")
                                        .initialValue(5D, 8D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("stun_duration")
                                        .initialValue(0.4D, 0.8D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05714D)
                                        .formatValue(value -> MathUtils.round(value, 2))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("volley_release").build())
                                        .source(ExperienceSourceTemplate.builder("volley_hit").build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("phantom_projectiles_released")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("phantom_damage_dealt")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
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
        public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
            if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Projectile projectile) || !(projectile.getOwner() instanceof ServerPlayer player))
                return;

            var projectileData = projectile.getPersistentData();

            if (projectileData.getBoolean("ran_ballistarian_processed"))
                return;

            var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.BALLISTARIAN_BRACER.get()).stream()
                    .filter(relicStack -> relicStack.getItem() instanceof BallistarianBracerItem)
                    .toList();

            if (stacks.isEmpty())
                return;

            projectileData.putBoolean("ran_ballistarian_processed", true);
            var totalProjectiles = 0;
            var totalDamage = 0D;

            for (var stack : stacks) {
                if (!(stack.getItem() instanceof BallistarianBracerItem item))
                    continue;

                var relicData = item.getRelicData(player, stack);
                var ability = relicData.getAbilitiesData().getAbilityData("ballistary");

                var triggerChance = Mth.clamp(ability.getStatData("trigger_chance").getValue(), 0D, 1D);
                var maxProjectileCount = Math.max(1, (int) Math.round(Math.max(0D, ability.getStatData("projectile_count").getValue())));
                var projectileDamage = Math.max(0D, ability.getStatData("projectile_damage").getValue());
                var projectileCount = MathUtils.multicast(player.getRandom(), triggerChance, maxProjectileCount);

                if (projectileDamage <= 0D || projectileCount <= 0)
                    continue;

                totalProjectiles += projectileCount;
                totalDamage += projectileCount * projectileDamage;
                relicData.getLevelingData().addExperience("ballistary", "volley_release", 1D);
                ability.getStatisticData().getMetricData("phantom_projectiles_released").addValue(projectileCount);
            }

            if (totalProjectiles <= 0 || totalDamage <= 0D)
                return;

            projectileData.putInt("ran_ballistarian_count", totalProjectiles);
            projectileData.putDouble("ran_ballistarian_total_damage", totalDamage);

            for (int i = 0; i < totalProjectiles; i++) {
                var phantom = new BallistarianPhantomProjectileEntity(EntityRegistry.BALLISTARIAN_PHANTOM_PROJECTILE.get(), player.serverLevel());
                var start = projectile.position().add(0D, projectile.getBbHeight() * 0.5D, 0D);

                phantom.setPos(start);
                phantom.bindToTarget(projectile, i, totalProjectiles);

                player.serverLevel().addFreshEntity(phantom);
            }
        }

        @SubscribeEvent
        public static void onLivingDamagePost(LivingDamageEvent.Post event) {
            if (!(event.getEntity() instanceof LivingEntity target) || !(event.getSource().getDirectEntity() instanceof Projectile projectile) || event.getNewDamage() <= 0F)
                return;

            var projectileData = projectile.getPersistentData();

            if (!projectileData.contains("ran_ballistarian_count")
                    || !projectileData.contains("ran_ballistarian_total_damage")
                    || projectileData.getBoolean("ran_ballistarian_duplicating"))
                return;

            var owner = projectile.getOwner();
            var bonusDamage = Math.max(0D, projectileData.getDouble("ran_ballistarian_total_damage"));
            var phantomCount = Math.max(0, projectileData.getInt("ran_ballistarian_count"));
            var hitDamage = Math.max(0F, event.getNewDamage());

            if (bonusDamage <= 0D || phantomCount <= 0 || owner == null || !owner.isAlive())
                return;

            projectileData.remove("ran_ballistarian_total_damage");
            projectileData.remove("ran_ballistarian_count");
            projectileData.putBoolean("ran_ballistarian_duplicating", true);

            try {
                target.hurt(target.damageSources().mobProjectile(projectile, owner instanceof LivingEntity living ? living : null), (float) bonusDamage);

                if (!(owner instanceof ServerPlayer player))
                    return;

                for (var stack : EntityUtils.findEquippedCurios(player, ItemRegistry.BALLISTARIAN_BRACER.get())) {
                    if (!(stack.getItem() instanceof BallistarianBracerItem item))
                        continue;

                    var relicData = item.getRelicData(player, stack);
                    var ability = relicData.getAbilitiesData().getAbilityData("ballistary");

                    if (ability.getRankModifierData("absorbing_kills").isEnabled() && !target.isAlive()) {
                        var absorptionGain = Math.max(0D, ability.getStatData("absorption_on_kill").getValue());

                        if (absorptionGain > 0D) {
                            var absorptionCap = getBallistarianAbsorptionCap(player);

                            if (absorptionCap > 0D) {
                                EntityUtils.resetAttribute(player, Attributes.MAX_ABSORPTION, (float) absorptionCap, AttributeModifier.Operation.ADD_VALUE, BALLISTARIAN_MAX_ABSORPTION_MODIFIER_ID);
                                player.setAbsorptionAmount((float) Math.min(absorptionCap, player.getAbsorptionAmount() + (float) absorptionGain));
                            }
                        }
                    }

                    if (ability.getRankModifierData("multicast_duplication").isEnabled() && hitDamage > 0F) {
                        var duplicationChance = Mth.clamp(ability.getStatData("duplication_chance").getValue(), 0D, 1D);
                        var duplicationRadius = Math.max(0D, ability.getStatData("duplication_radius").getValue());
                        var duplicationTargets = MathUtils.multicast(player.getRandom(), duplicationChance, phantomCount);

                        if (duplicationChance > 0D && duplicationRadius > 0D && duplicationTargets > 0) {
                            var duplicated = 0;

                            for (var nearby : player.serverLevel().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(duplicationRadius),
                                    candidate -> candidate.isAlive() && candidate != target && candidate != player && !EntityUtils.isAlliedTo(player, candidate))) {
                                nearby.invulnerableTime = 0;
                                nearby.hurt(nearby.damageSources().mobProjectile(projectile, owner instanceof LivingEntity living ? living : null), hitDamage);

                                duplicated++;

                                if (duplicated >= duplicationTargets)
                                    break;
                            }
                        }
                    }

                    if (ability.getRankModifierData("paralyzing_shots").isEnabled() && target.isAlive()) {
                        var stunDuration = Math.max(0D, ability.getStatData("stun_duration").getValue());

                        if (stunDuration > 0D)
                            target.addEffect(new MobEffectInstance(RelicsMobEffects.STUN, Math.max(1, (int) Math.round(stunDuration * 20D * phantomCount)), 0, false, true, true), player);
                    }

                    relicData.getLevelingData().addExperience("ballistary", "volley_hit", 1D);
                    ability.getStatisticData().getMetricData("phantom_damage_dealt").addValue(bonusDamage);
                }
            } finally {
                projectileData.remove("ran_ballistarian_duplicating");
            }
        }
    }
}

