package it.hurts.shatterbyte.reliquified_ars_nouveau.items.head;

import com.hollingsworth.arsnouveau.common.entity.SummonWolf;
import com.hollingsworth.arsnouveau.setup.registry.ModEntities;
import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.base.RANWearableRelicItem;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.base.loot.LootEntries;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import top.theillusivec4.curios.api.SlotContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HornOfWildHunterItem extends RANWearableRelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("summoner")
                                .rankModifier(1, "bone_harvest")
                                .rankModifier(3, "bleeding_bite")
                                .rankModifier(5, "pack_growth")
                                .stat(AbilityStatTemplate.builder("max_wolves")
                                        .initialValue(1D, 3D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 10D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("wolf_damage")
                                        .initialValue(3D, 5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 10D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("bone_drop_chance")
                                        .initialValue(0.1D, 0.25D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.5D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("bone_drop_max")
                                        .initialValue(1D, 3D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 10D)
                                        .formatValue(value -> Math.max(1, (int) MathUtils.round(value, 0)))
                                        .build())
                                .stat(AbilityStatTemplate.builder("bleeding_level")
                                        .initialValue(1D, 3D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 10D)
                                        .formatValue(value -> Math.max(1, (int) MathUtils.round(value, 0)))
                                        .build())
                                .stat(AbilityStatTemplate.builder("bleeding_duration")
                                        .initialValue(3D, 5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 10D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("extra_wolf_chance")
                                        .initialValue(0.05D, 0.1D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.25D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("extra_wolf_lifetime")
                                        .initialValue(5D, 10D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 30D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("extra_wolf_cap")
                                        .initialValue(1D, 3D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 10D)
                                        .formatValue(value -> Math.max(1, (int) MathUtils.round(value, 0)))
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
        if (!(slotContext.entity() instanceof Player player) || player.level().isClientSide())
            return;

        var relicData = this.getRelicData(player, stack);
        var ability = relicData.getAbilitiesData().getAbilityData("summoner");
        var level = (ServerLevel) player.level();
        var wolves = new ArrayList<>(stack.getOrDefault(RANDataComponentRegistry.HORN_OF_THE_WILD_HUNTER_WOLVES.get(), java.util.List.<String>of()));
        var gameTime = level.getGameTime();
        var aliveBaseWolves = 0;

        for (var i = wolves.size() - 1; i >= 0; i--) {
            try {
                var wolfEntity = level.getEntity(UUID.fromString(wolves.get(i)));

                if (!(wolfEntity instanceof Wolf wolf) || !wolf.isAlive() || wolf.distanceTo(player) > 96F) {
                    wolves.remove(i);
                    continue;
                }

                if (wolf.getPersistentData().contains("ran_wild_hunter_temp_until") && gameTime > wolf.getPersistentData().getLong("ran_wild_hunter_temp_until")) {
                    wolf.discard();
                    wolves.remove(i);
                    continue;
                }

                if (!wolf.getPersistentData().contains("ran_wild_hunter_temp_until"))
                    aliveBaseWolves++;
            } catch (IllegalArgumentException ignored) {
                wolves.remove(i);
            }
        }

        var maxWolves = Math.max(0, (int) Math.round(ability.getStatData("max_wolves").getValue()));

        while (aliveBaseWolves < maxWolves && level.isLoaded(player.blockPosition())) {
            var random = player.getRandom();
            var angle = random.nextDouble() * Math.PI * 2D;
            var radius = 1D + random.nextDouble() * 2D;
            var spawn = player.blockPosition().offset((int) Math.round(Math.cos(angle) * radius), 0, (int) Math.round(Math.sin(angle) * radius));

            for (var y = 0; y < 5; y++) {
                if (level.getBlockState(spawn).getCollisionShape(level, spawn).isEmpty() && level.getBlockState(spawn.above()).getCollisionShape(level, spawn.above()).isEmpty())
                    break;

                spawn = spawn.above();
            }

            if (!level.getBlockState(spawn).getCollisionShape(level, spawn).isEmpty() || !level.getBlockState(spawn.above()).getCollisionShape(level, spawn.above()).isEmpty())
                break;

            var wolf = new SummonWolf(ModEntities.SUMMON_WOLF.get(), level);

            wolf.moveTo(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D, player.getYRot(), 0F);

            wolf.ticksLeft = Integer.MAX_VALUE;

            wolf.tame(player);
            wolf.setSilent(true);
            wolf.setAggressive(true);
            wolf.setInvulnerable(true);
            wolf.setPersistenceRequired();
            wolf.setOwnerID(player.getUUID());
            wolf.setTame(true, false);
            wolf.getPersistentData().putBoolean("ran_wild_hunter_summon", true);

            level.addFreshEntity(wolf);

            wolves.add(wolf.getUUID().toString());

            aliveBaseWolves++;
        }

        stack.set(RANDataComponentRegistry.HORN_OF_THE_WILD_HUNTER_WOLVES.get(), wolves);
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player) || player.level().isClientSide() || newStack.getItem() == stack.getItem())
            return;

        var server = ((ServerLevel) player.level()).getServer();

        for (var wolfId : stack.getOrDefault(RANDataComponentRegistry.HORN_OF_THE_WILD_HUNTER_WOLVES.get(), List.<String>of())) {
            try {
                var uuid = UUID.fromString(wolfId);
                Entity wolf = null;

                for (var serverLevel : server.getAllLevels()) {
                    wolf = serverLevel.getEntity(uuid);

                    if (wolf != null)
                        break;
                }

                if (wolf != null)
                    wolf.discard();
            } catch (IllegalArgumentException ignored) {
            }
        }

        stack.set(RANDataComponentRegistry.HORN_OF_THE_WILD_HUNTER_WOLVES.get(), java.util.List.<String>of());
    }

    @EventBusSubscriber(modid = ReliquifiedArsNouveau.MODID)
    public static class CommonEvents {
        @SubscribeEvent
        public static void onSummonedWolfDimensionTravel(EntityTravelToDimensionEvent event) {
            if (!(event.getEntity() instanceof SummonWolf wolf) || !wolf.getPersistentData().getBoolean("ran_wild_hunter_summon"))
                return;

            event.setCanceled(true);

            if (!wolf.level().isClientSide())
                wolf.discard();
        }

        @SubscribeEvent
        public static void onSummonedWolfIncomingDamage(LivingIncomingDamageEvent event) {
            if (!(event.getEntity() instanceof SummonWolf wolf) || !wolf.getPersistentData().getBoolean("ran_wild_hunter_summon"))
                return;

            event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
            if (!(event.getSource().getEntity() instanceof Wolf wolf) || event.getNewDamage() <= 0F || !wolf.getPersistentData().getBoolean("ran_wild_hunter_summon"))
                return;

            if (!(wolf.getOwner() instanceof Player player) || !(event.getEntity() instanceof LivingEntity target))
                return;

            var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.HORN_OF_THE_WILD_HUNTER.get()).stream()
                    .filter(stack -> stack.getItem() instanceof HornOfWildHunterItem)
                    .toList();

            if (stacks.isEmpty())
                return;

            var damage = 0D;
            var bleedingLevel = 0;
            var bleedingDurationTicks = 0;

            for (var stack : stacks) {
                var item = (HornOfWildHunterItem) stack.getItem();
                var ability = item.getRelicData(player, stack).getAbilitiesData().getAbilityData("summoner");

                if (!stack.getOrDefault(RANDataComponentRegistry.HORN_OF_THE_WILD_HUNTER_WOLVES.get(), java.util.List.<String>of()).contains(wolf.getUUID().toString()))
                    continue;

                damage += Math.max(0D, ability.getStatData("wolf_damage").getValue());

                if (ability.getRankModifierData("bleeding_bite").isEnabled()) {
                    bleedingLevel += Math.max(1, (int) Math.round(ability.getStatData("bleeding_level").getValue()));
                    bleedingDurationTicks += Math.max(1, (int) Math.round(ability.getStatData("bleeding_duration").getValue() * 20D));
                }
            }

            if (damage > 0D)
                event.setNewDamage((float) damage);

            if (bleedingLevel > 0 && bleedingDurationTicks > 0)
                target.addEffect(new MobEffectInstance(RelicsMobEffects.BLEEDING, bleedingDurationTicks, Math.max(0, bleedingLevel - 1), false, true, true), player);
        }

        @SubscribeEvent
        public static void onLivingDamagePost(LivingDamageEvent.Post event) {
            if (!(event.getSource().getEntity() instanceof Wolf wolf) || !(event.getEntity() instanceof LivingEntity target) || target.isAlive() || !wolf.getPersistentData().getBoolean("ran_wild_hunter_summon"))
                return;

            if (!(wolf.getOwner() instanceof Player player) || player.level().isClientSide())
                return;

            var level = (ServerLevel) player.level();

            for (var stack : EntityUtils.findEquippedCurios(player, ItemRegistry.HORN_OF_THE_WILD_HUNTER.get())) {
                if (!(stack.getItem() instanceof HornOfWildHunterItem item))
                    continue;

                var relicData = item.getRelicData(player, stack);
                var ability = relicData.getAbilitiesData().getAbilityData("summoner");

                if (!stack.getOrDefault(RANDataComponentRegistry.HORN_OF_THE_WILD_HUNTER_WOLVES.get(), java.util.List.<String>of()).contains(wolf.getUUID().toString()))
                    continue;

                if (ability.getRankModifierData("bone_harvest").isEnabled()) {
                    var chance = Math.max(0D, ability.getStatData("bone_drop_chance").getValue());
                    var maxBones = Math.max(1, (int) Math.round(ability.getStatData("bone_drop_max").getValue()));
                    var bones = MathUtils.multicast(level.getRandom(), chance, maxBones);

                    if (bones > 0)
                        level.addFreshEntity(new ItemEntity(level, target.getX(), target.getY() + 0.25D, target.getZ(), new ItemStack(Items.BONE, bones)));
                }

                if (ability.getRankModifierData("pack_growth").isEnabled() && level.getRandom().nextDouble() < Math.max(0D, ability.getStatData("extra_wolf_chance").getValue())) {
                    var wolves = new ArrayList<>(stack.getOrDefault(RANDataComponentRegistry.HORN_OF_THE_WILD_HUNTER_WOLVES.get(), java.util.List.<String>of()));
                    var tempCount = 0;

                    for (var i = wolves.size() - 1; i >= 0; i--) {
                        try {
                            var wolfEntity = level.getEntity(UUID.fromString(wolves.get(i)));

                            if (!(wolfEntity instanceof Wolf livingWolf) || !livingWolf.isAlive()) {
                                wolves.remove(i);
                                continue;
                            }

                            if (livingWolf.getPersistentData().contains("ran_wild_hunter_temp_until")) {
                                if (level.getGameTime() > livingWolf.getPersistentData().getLong("ran_wild_hunter_temp_until")) {
                                    livingWolf.discard();
                                    wolves.remove(i);
                                    continue;
                                }

                                tempCount++;
                            }
                        } catch (IllegalArgumentException ignored) {
                            wolves.remove(i);
                        }
                    }

                    var tempCap = Math.max(1, (int) Math.round(ability.getStatData("extra_wolf_cap").getValue()));

                    if (tempCount < tempCap) {
                        var random = player.getRandom();
                        var angle = random.nextDouble() * Math.PI * 2D;
                        var radius = 1D + random.nextDouble() * 2D;
                        var spawn = player.blockPosition().offset((int) Math.round(Math.cos(angle) * radius), 0, (int) Math.round(Math.sin(angle) * radius));

                        for (var y = 0; y < 5; y++) {
                            if (level.getBlockState(spawn).getCollisionShape(level, spawn).isEmpty() && level.getBlockState(spawn.above()).getCollisionShape(level, spawn.above()).isEmpty())
                                break;

                            spawn = spawn.above();
                        }

                        if (level.getBlockState(spawn).getCollisionShape(level, spawn).isEmpty() && level.getBlockState(spawn.above()).getCollisionShape(level, spawn.above()).isEmpty()) {
                            var extraWolf = new SummonWolf(ModEntities.SUMMON_WOLF.get(), level);

                            extraWolf.moveTo(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D, player.getYRot(), 0F);
                            extraWolf.ticksLeft = Math.max(1, (int) Math.round(ability.getStatData("extra_wolf_lifetime").getValue() * 20D));
                            extraWolf.setTame(true, false);
                            extraWolf.tame(player);
                            extraWolf.setAggressive(true);
                            extraWolf.setInvulnerable(true);
                            extraWolf.setSilent(true);
                            extraWolf.setPersistenceRequired();
                            extraWolf.getPersistentData().putBoolean("ran_wild_hunter_summon", true);
                            extraWolf.getPersistentData().putLong("ran_wild_hunter_temp_until", level.getGameTime() + Math.max(1L, Math.round(ability.getStatData("extra_wolf_lifetime").getValue() * 20D)));
                            level.addFreshEntity(extraWolf);

                            wolves.add(extraWolf.getUUID().toString());
                            stack.set(RANDataComponentRegistry.HORN_OF_THE_WILD_HUNTER_WOLVES.get(), wolves);
                        }
                    } else {
                        stack.set(RANDataComponentRegistry.HORN_OF_THE_WILD_HUNTER_WOLVES.get(), wolves);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
            var player = event.getEntity();

            if (player.level().isClientSide())
                return;

            for (var stack : EntityUtils.findEquippedCurios(player, ItemRegistry.HORN_OF_THE_WILD_HUNTER.get())) {
                if (!(stack.getItem() instanceof HornOfWildHunterItem))
                    continue;

                var oldLevel = player.getServer() == null ? null : player.getServer().getLevel(event.getFrom());

                if (oldLevel != null) {
                    for (var wolfId : stack.getOrDefault(RANDataComponentRegistry.HORN_OF_THE_WILD_HUNTER_WOLVES.get(), java.util.List.<String>of())) {
                        try {
                            var wolf = oldLevel.getEntity(UUID.fromString(wolfId));

                            if (wolf != null)
                                wolf.discard();
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                }

                stack.set(RANDataComponentRegistry.HORN_OF_THE_WILD_HUNTER_WOLVES.get(), java.util.List.<String>of());
            }
        }
    }
}

