package it.hurts.shatterbyte.reliquified_ars_nouveau.items;

import com.hollingsworth.arsnouveau.common.block.MageBlock;
import com.hollingsworth.arsnouveau.common.block.tile.MageBlockTile;
import com.hollingsworth.arsnouveau.client.particle.ParticleColor;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.base.RANRelicItem;
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
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.data.WorldPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.awt.Color;

public class ArchitectsStaffItem extends RANRelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("bridgecraft")
                                .rankModifier(1, "vital_support")
                                .rankModifier(3, "ballistic_support")
                                .rankModifier(5, "disappearance")
                                .stat(AbilityStatTemplate.builder("max_charges")
                                        .initialValue(50D, 75D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 1000D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("charge_regen_time")
                                        .initialValue(10D, 5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 1D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("max_bridge_length")
                                        .initialValue(10D, 15D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 100D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("bridge_duration")
                                        .initialValue(10D, 20D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 100D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("healing_bonus")
                                        .initialValue(0.1D, 0.25D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("projectile_damage_bonus")
                                        .initialValue(0.1D, 0.25D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("disappearance_action_lock_time")
                                        .initialValue(10D, 7.5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 1D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("bridge_created").build())
                                        .source(ExperienceSourceTemplate.builder("bridge_block_created").build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("bridges_created")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("bridge_blocks_created")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("healing_boosted")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .rankModifierVisibilityState("vital_support", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("projectile_bonus_damage")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 2)))
                                                .rankModifierVisibilityState("ballistic_support", VisibilityState.OBFUSCATED)
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
        var firstPoint = player.blockPosition();
        var hit = player.pick(64D, 1F, false);

        if (hit instanceof BlockHitResult blockHitResult)
            firstPoint = blockHitResult.getBlockPos();

        stack.set(RANDataComponentRegistry.ARCHITECTS_STAFF_FIRST_POINT.get(), new WorldPosition(level.dimension(), Vec3.atCenterOf(firstPoint)));

        player.startUsingItem(hand);

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.getItem() == this;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        var charges = stack.getOrDefault(RANDataComponentRegistry.ARCHITECTS_STAFF_CHARGES.get(), 0);
        var maxCharges = Math.max(1, (int) Math.round(Math.max(0D, this.getRelicData(null, stack).getAbilitiesData().getAbilityData("bridgecraft").getStatData("max_charges").getValue())));

        return Mth.clamp((int) Math.floor(charges * 13D / maxCharges), 0, 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x4EA5FF;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (!(entity instanceof ServerPlayer player) || level.isClientSide()) {
            stack.remove(RANDataComponentRegistry.ARCHITECTS_STAFF_FIRST_POINT.get());
            return;
        }

        var relicData = this.getRelicData(player, stack);
        var ability = relicData.getAbilitiesData().getAbilityData("bridgecraft");
        var firstPointData = stack.get(RANDataComponentRegistry.ARCHITECTS_STAFF_FIRST_POINT.get());

        if (firstPointData == null || !firstPointData.getLevel().equals(player.level().dimension())) {
            stack.remove(RANDataComponentRegistry.ARCHITECTS_STAFF_FIRST_POINT.get());
            return;
        }

        var secondPoint = player.blockPosition();
        var hit = player.pick(64D, 1F, false);

        if (hit instanceof BlockHitResult blockHitResult)
            secondPoint = blockHitResult.getBlockPos();

        var maxCharges = Math.max(1, (int) Math.round(Math.max(0D, ability.getStatData("max_charges").getValue())));
        var charges = Mth.clamp(stack.getOrDefault(RANDataComponentRegistry.ARCHITECTS_STAFF_CHARGES.get(), maxCharges), 0, maxCharges);
        var maxBridgeLength = Math.max(1, (int) Math.round(Math.max(0D, ability.getStatData("max_bridge_length").getValue())));

        if (charges <= 0) {
            stack.remove(RANDataComponentRegistry.ARCHITECTS_STAFF_FIRST_POINT.get());
            return;
        }

        var fromCenter = firstPointData.getPos();
        var toCenter = Vec3.atCenterOf(secondPoint);

        var path = new LinkedHashSet<BlockPos>();

        var currentX = Mth.floor(fromCenter.x);
        var currentY = Mth.floor(fromCenter.y);
        var currentZ = Mth.floor(fromCenter.z);

        var targetX = Mth.floor(toCenter.x);
        var targetY = Mth.floor(toCenter.y);
        var targetZ = Mth.floor(toCenter.z);

        var deltaX = toCenter.x - fromCenter.x;
        var deltaY = toCenter.y - fromCenter.y;
        var deltaZ = toCenter.z - fromCenter.z;

        var stepX = Double.compare(deltaX, 0D);
        var stepY = Double.compare(deltaY, 0D);
        var stepZ = Double.compare(deltaZ, 0D);

        var tDeltaX = stepX == 0 ? Double.POSITIVE_INFINITY : 1D / Math.abs(deltaX);
        var tDeltaY = stepY == 0 ? Double.POSITIVE_INFINITY : 1D / Math.abs(deltaY);
        var tDeltaZ = stepZ == 0 ? Double.POSITIVE_INFINITY : 1D / Math.abs(deltaZ);

        var tMaxX = stepX > 0 ? ((currentX + 1D) - fromCenter.x) / deltaX : stepX < 0 ? (fromCenter.x - currentX) / -deltaX : Double.POSITIVE_INFINITY;
        var tMaxY = stepY > 0 ? ((currentY + 1D) - fromCenter.y) / deltaY : stepY < 0 ? (fromCenter.y - currentY) / -deltaY : Double.POSITIVE_INFINITY;
        var tMaxZ = stepZ > 0 ? ((currentZ + 1D) - fromCenter.z) / deltaZ : stepZ < 0 ? (fromCenter.z - currentZ) / -deltaZ : Double.POSITIVE_INFINITY;

        var safety = 0;

        path.add(new BlockPos(currentX, currentY, currentZ));

        while ((currentX != targetX || currentY != targetY || currentZ != targetZ) && safety++ < 4096) {
            if (tMaxX <= tMaxY && tMaxX <= tMaxZ) {
                currentX += stepX;
                tMaxX += tDeltaX;
            } else if (tMaxY <= tMaxX && tMaxY <= tMaxZ) {
                currentY += stepY;
                tMaxY += tDeltaY;
            } else {
                currentZ += stepZ;
                tMaxZ += tDeltaZ;
            }

            path.add(new BlockPos(currentX, currentY, currentZ));
        }

        if (path.isEmpty()) {
            stack.remove(RANDataComponentRegistry.ARCHITECTS_STAFF_FIRST_POINT.get());

            return;
        }

        var firstPoint = BlockPos.containing(firstPointData.getPos());
        var pathList = new ArrayList<BlockPos>();

        for (var blockPos : path) {
            if (blockPos.equals(firstPoint) || blockPos.equals(secondPoint))
                continue;

            pathList.add(blockPos);
        }

        var unitsToBuild = Math.min(Math.min(pathList.size(), charges), maxBridgeLength);
        var createdBlocks = 0;
        var bridgeDuration = Math.max(1D, ability.getStatData("bridge_duration").getValue());
        var lengthModifier = Math.max(0D, (bridgeDuration - 15D) / 5D);
        var randomHue = player.getRandom().nextFloat();
        var randomRgb = Color.HSBtoRGB(randomHue, 0.85F, 1F);
        var bridgeColor = new ParticleColor((randomRgb >> 16) & 255, (randomRgb >> 8) & 255, randomRgb & 255);

        for (var i = 0; i < unitsToBuild; i++) {
            var blockPos = pathList.get(i);

            if (!level.isInWorldBounds(blockPos))
                continue;

            var state = level.getBlockState(blockPos);

            if (!state.canBeReplaced() || level.getBlockEntity(blockPos) != null || !level.isUnobstructed(BlockRegistry.MAGE_BLOCK.get().defaultBlockState(), blockPos, CollisionContext.empty()))
                continue;

            level.setBlockAndUpdate(blockPos, BlockRegistry.MAGE_BLOCK.get().defaultBlockState().setValue(MageBlock.TEMPORARY, true));

            if (level.getBlockEntity(blockPos) instanceof MageBlockTile tile) {
                tile.setColor(bridgeColor);
                tile.lengthModifier = lengthModifier;
                tile.isPermanent = false;
                tile.updateBlock();
            }

            createdBlocks++;
        }

        charges -= createdBlocks;

        stack.set(RANDataComponentRegistry.ARCHITECTS_STAFF_CHARGES.get(), charges);
        stack.set(RANDataComponentRegistry.ARCHITECTS_STAFF_REGEN_TICKS.get(), 0);
        stack.remove(RANDataComponentRegistry.ARCHITECTS_STAFF_FIRST_POINT.get());

        if (createdBlocks <= 0)
            return;

        var bridgeTicks = Math.max(1L, Math.round(bridgeDuration * 20D));

        stack.set(RANDataComponentRegistry.ARCHITECTS_STAFF_BRIDGE_ACTIVE_UNTIL.get(), level.getGameTime() + bridgeTicks);

        relicData.getLevelingData().addExperience("bridgecraft", "bridge_created", 1D);
        relicData.getLevelingData().addExperience("bridgecraft", "bridge_block_created", createdBlocks);
        
        ability.getStatisticData().getMetricData("bridges_created").addValue(1D);
        ability.getStatisticData().getMetricData("bridge_blocks_created").addValue(createdBlocks);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!(entity instanceof ServerPlayer player) || level.isClientSide())
            return;

        var ability = this.getRelicData(player, stack).getAbilitiesData().getAbilityData("bridgecraft");

        var maxCharges = Math.max(1, (int) Math.round(Math.max(0D, ability.getStatData("max_charges").getValue())));
        var charges = Mth.clamp(stack.getOrDefault(RANDataComponentRegistry.ARCHITECTS_STAFF_CHARGES.get(), maxCharges), 0, maxCharges);
        var regenIntervalTicks = Math.max(1, (int) Math.round(Math.max(0.05D, ability.getStatData("charge_regen_time").getValue()) * 20D));
        var regenTicks = Math.max(0, stack.getOrDefault(RANDataComponentRegistry.ARCHITECTS_STAFF_REGEN_TICKS.get(), 0));
        var isUsingThisStack = player.isUsingItem() && player.getUseItem() == stack;

        if (isUsingThisStack) {
            regenTicks = 0;
        } else if (charges < maxCharges) {
            regenTicks++;

            while (regenTicks >= regenIntervalTicks && charges < maxCharges) {
                charges++;
                regenTicks -= regenIntervalTicks;
            }
        } else {
            regenTicks = 0;
        }

        stack.set(RANDataComponentRegistry.ARCHITECTS_STAFF_CHARGES.get(), charges);
        stack.set(RANDataComponentRegistry.ARCHITECTS_STAFF_REGEN_TICKS.get(), regenTicks);

        if (player.getMainHandItem() != stack && player.getOffhandItem() != stack)
            return;

        if (!ability.getRankModifierData("disappearance").isEnabled())
            return;

        if (stack.getOrDefault(RANDataComponentRegistry.ARCHITECTS_STAFF_BRIDGE_ACTIVE_UNTIL.get(), 0L) <= level.getGameTime())
            return;

        var playerPos = player.blockPosition();
        var feet = player.level().getBlockState(playerPos);
        var below = player.level().getBlockState(playerPos.below());

        if (!(feet.is(BlockRegistry.MAGE_BLOCK.get()) || below.is(BlockRegistry.MAGE_BLOCK.get())))
            return;

        if (stack.getOrDefault(RANDataComponentRegistry.ARCHITECTS_STAFF_DISAPPEAR_LOCK_UNTIL.get(), 0L) > level.getGameTime())
            return;

        player.addEffect(new MobEffectInstance(RelicsMobEffects.VANISHING, 10, 0, false, false, true), player);

        for (var target : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(24D), mob -> mob.isAlive() && mob.getTarget() == player))
            target.setTarget(null);
    }

    @EventBusSubscriber(modid = ReliquifiedArsNouveau.MODID)
    public static class CommonEvents {
        @SubscribeEvent
        public static void onLivingHeal(LivingHealEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || event.getAmount() <= 0F)
                return;

            var gameTime = player.level().getGameTime();
            var totalBonus = 0D;

            var staffStacks = new ArrayList<ItemStack>();

            for (var stack : player.getInventory().items) {
                if (stack.getItem() instanceof ArchitectsStaffItem)
                    staffStacks.add(stack);
            }

            for (var stack : player.getInventory().offhand) {
                if (stack.getItem() instanceof ArchitectsStaffItem)
                    staffStacks.add(stack);
            }

            for (var stack : staffStacks) {
                if (!(stack.getItem() instanceof ArchitectsStaffItem item))
                    continue;

                var relicData = item.getRelicData(player, stack);
                var ability = relicData.getAbilitiesData().getAbilityData("bridgecraft");

                if (!ability.getRankModifierData("vital_support").isEnabled())
                    continue;

                if (stack.getOrDefault(RANDataComponentRegistry.ARCHITECTS_STAFF_BRIDGE_ACTIVE_UNTIL.get(), 0L) <= gameTime)
                    continue;

                var playerPos = player.blockPosition();
                var feet = player.level().getBlockState(playerPos);
                var below = player.level().getBlockState(playerPos.below());

                if (!(feet.is(BlockRegistry.MAGE_BLOCK.get()) || below.is(BlockRegistry.MAGE_BLOCK.get())))
                    continue;

                var healBonus = Mth.clamp(ability.getStatData("healing_bonus").getValue(), 0D, 10D);

                if (healBonus <= 0D)
                    continue;

                totalBonus += healBonus;
                ability.getStatisticData().getMetricData("healing_boosted").addValue(event.getAmount() * healBonus);
            }

            if (totalBonus > 0D)
                event.setAmount((float) Math.max(0D, event.getAmount() * (1D + totalBonus)));
        }

        @SubscribeEvent
        public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
            if (!(event.getSource().getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || event.getEntity() == player || event.getNewDamage() <= 0F
                    || !(event.getSource().getDirectEntity() instanceof Projectile))
                return;

            var gameTime = player.level().getGameTime();
            var totalBonus = 0D;
            var baseDamage = event.getNewDamage();
            var staffStacks = new ArrayList<ItemStack>();

            for (var stack : player.getInventory().items) {
                if (stack.getItem() instanceof ArchitectsStaffItem)
                    staffStacks.add(stack);
            }

            for (var stack : player.getInventory().offhand) {
                if (stack.getItem() instanceof ArchitectsStaffItem)
                    staffStacks.add(stack);
            }

            for (var stack : staffStacks) {
                if (!(stack.getItem() instanceof ArchitectsStaffItem item))
                    continue;

                var relicData = item.getRelicData(player, stack);
                var ability = relicData.getAbilitiesData().getAbilityData("bridgecraft");

                if (!ability.getRankModifierData("ballistic_support").isEnabled())
                    continue;

                if (stack.getOrDefault(RANDataComponentRegistry.ARCHITECTS_STAFF_BRIDGE_ACTIVE_UNTIL.get(), 0L) <= gameTime)
                    continue;

                var playerPos = player.blockPosition();
                var feet = player.level().getBlockState(playerPos);
                var below = player.level().getBlockState(playerPos.below());

                if (!(feet.is(BlockRegistry.MAGE_BLOCK.get()) || below.is(BlockRegistry.MAGE_BLOCK.get())))
                    continue;

                var projectileBonus = Mth.clamp(ability.getStatData("projectile_damage_bonus").getValue(), 0D, 10D);

                if (projectileBonus <= 0D)
                    continue;

                totalBonus += projectileBonus;
                ability.getStatisticData().getMetricData("projectile_bonus_damage").addValue(baseDamage * projectileBonus);
            }

            if (totalBonus > 0D)
                event.setNewDamage((float) Math.max(0D, baseDamage * (1D + totalBonus)));
        }

        @SubscribeEvent
        public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
            onInteract(event.getEntity());
        }

        @SubscribeEvent
        public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
            onInteract(event.getEntity());
        }

        @SubscribeEvent
        public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
            onInteract(event.getEntity());
        }

        @SubscribeEvent
        public static void onBlockBreakAttempt(PlayerEvent.BreakSpeed event) {
            onInteract(event.getEntity());
        }

        @SubscribeEvent
        public static void onAttackEntity(AttackEntityEvent event) {
            onInteract(event.getEntity());
        }

        @SubscribeEvent
        public static void onItemToss(ItemTossEvent event) {
            onInteract(event.getPlayer());
        }

        @SubscribeEvent
        public static void onItemPickup(ItemEntityPickupEvent.Post event) {
            onInteract(event.getPlayer());
        }

        private static void onInteract(LivingEntity entity) {
            if (!(entity instanceof ServerPlayer player) || player.level().isClientSide() || !player.hasEffect(RelicsMobEffects.VANISHING))
                return;

            var gameTime = player.level().getGameTime();

            for (var stack : List.of(player.getMainHandItem(), player.getOffhandItem())) {
                if (!(stack.getItem() instanceof ArchitectsStaffItem item))
                    continue;

                var ability = item.getRelicData(player, stack).getAbilitiesData().getAbilityData("bridgecraft");

                if (!ability.getRankModifierData("disappearance").isEnabled())
                    continue;

                var lockSeconds = Math.max(0D, ability.getStatData("disappearance_action_lock_time").getValue());

                if (lockSeconds <= 0D)
                    continue;

                var lockUntil = gameTime + Math.max(1L, Math.round(lockSeconds * 20D));

                stack.set(RANDataComponentRegistry.ARCHITECTS_STAFF_DISAPPEAR_LOCK_UNTIL.get(), Math.max(lockUntil, stack.getOrDefault(RANDataComponentRegistry.ARCHITECTS_STAFF_DISAPPEAR_LOCK_UNTIL.get(), 0L)));

                player.removeEffect(RelicsMobEffects.VANISHING);
            }
        }
    }
}
