package it.hurts.shatterbyte.reliquified_ars_nouveau.items;

import com.hollingsworth.arsnouveau.api.util.BlockUtil;
import com.hollingsworth.arsnouveau.common.block.tile.IntangibleAirTile;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectIntangible;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.base.RANRelicItem;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.base.loot.LootEntries;
import it.hurts.sskirillss.relics.api.relics.AbilityMetricTemplate;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashSet;

public class StaffOfTheSpectralWalkerItem extends RANRelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("spectral")
                                .rankModifier(1, "spectral_vision")
                                .rankModifier(3, "safe_return")
                                .rankModifier(5, "immortality")
                                .stat(AbilityStatTemplate.builder("max_charges")
                                        .initialValue(3D, 5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 25.00075D)
                                        .formatValue(value -> Math.max(1, (int) MathUtils.round(value, 0)))
                                        .build())
                                .stat(AbilityStatTemplate.builder("entity_reveal_radius")
                                        .initialValue(5D, 10D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 19.99950D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("charge_regen_time")
                                        .initialValue(10D, 7.5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 1.00050D)
                                        .formatValue(value -> MathUtils.round(value, 2))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("spectral_time").build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("ability_uses")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("spectral_time")
                                                .formatValue(value -> MathUtils.formatTime((int) MathUtils.round(value, 0)))
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
            var ability = relicData.getAbilitiesData().getAbilityData("spectral");

            var maxCharges = Math.max(1, (int) Math.round(Math.max(0D, ability.getStatData("max_charges").getValue())));
            var charges = Mth.clamp(stack.getOrDefault(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_CHARGES.get(), maxCharges), 0, maxCharges);

            if (charges <= 0)
                return InteractionResultHolder.fail(stack);

            stack.set(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_POSITION.get(), new WorldPosition(serverPlayer));
            stack.set(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_CHARGES.get(), charges);
            stack.set(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_MOVE_TICKS.get(), 0);
            stack.set(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_REGEN_TICKS.get(), 0);
            stack.set(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_WAS_IN_SPECTRAL.get(), false);
            stack.set(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_FALL_IMMUNITY_TICKS.get(), 0);

            ability.getStatisticData().getMetricData("ability_uses").addValue(1D);
        }

        player.startUsingItem(hand);

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.getItem() == this;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if (stack.getItem() != this)
            return 0;

        var charges = stack.getOrDefault(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_CHARGES.get(), 0);
        var maxCharges = Math.max(1, (int) Math.round(this.getRelicData(null, stack).getAbilitiesData().getAbilityData("spectral").getStatData("max_charges").getValue()));

        return Mth.clamp((int) Math.floor(charges * 13D / maxCharges), 0, 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x58D4FF;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof ServerPlayer player) || level.isClientSide())
            return;

        var relicData = this.getRelicData(player, stack);
        var ability = relicData.getAbilitiesData().getAbilityData("spectral");
        var maxCharges = Math.max(1, (int) Math.round(Math.max(0D, ability.getStatData("max_charges").getValue())));
        var charges = Mth.clamp(stack.getOrDefault(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_CHARGES.get(), maxCharges), 0, maxCharges);

        stack.set(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_CHARGES.get(), charges);

        if (charges <= 0) {
            this.releaseUsing(stack, level, player, remainingUseDuration);
            player.stopUsingItem();
            return;
        }

        var playerBlockPos = player.blockPosition();
        var playerBlockState = level.getBlockState(playerBlockPos);
        var playerBlockStateAbove = level.getBlockState(playerBlockPos.above());
        var forwardDirection = player.getLookAngle();

        if (forwardDirection.lengthSqr() <= 1.0E-6D)
            forwardDirection = Vec3.directionFromRotation(0F, player.getYRot());

        forwardDirection = forwardDirection.normalize();

        var forcedForwardMotion = forwardDirection.scale(0.62D);
        var isInsideSpectralBlocks = playerBlockState.is(BlockRegistry.INTANGIBLE_AIR.get()) || playerBlockStateAbove.is(BlockRegistry.INTANGIBLE_AIR.get());
        var wasInsideSpectralBlocks = stack.getOrDefault(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_WAS_IN_SPECTRAL.get(), false);
        var fallImmunityTicks = stack.getOrDefault(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_FALL_IMMUNITY_TICKS.get(), 0);

        if (!isInsideSpectralBlocks && wasInsideSpectralBlocks)
            fallImmunityTicks = 1;

        if (fallImmunityTicks > 0 && player.onGround())
            fallImmunityTicks = 0;

        if (fallImmunityTicks > 0)
            player.fallDistance = 0F;

        stack.set(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_WAS_IN_SPECTRAL.get(), isInsideSpectralBlocks);
        stack.set(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_FALL_IMMUNITY_TICKS.get(), fallImmunityTicks);

        if (isInsideSpectralBlocks && forcedForwardMotion.y >= 0.2D)
            forcedForwardMotion = forcedForwardMotion.scale(1.9D);

        if (isInsideSpectralBlocks) {
            player.setDeltaMovement(forcedForwardMotion);
            player.hurtMarked = true;
            player.fallDistance = 0F;
            player.startAutoSpinAttack(5, 0F, ItemStack.EMPTY);

            if (ability.getRankModifierData("immortality").isEnabled())
                player.addEffect(new MobEffectInstance(RelicsMobEffects.IMMORTALITY, 5, 0, false, false, false), player);

            var moveTicks = Math.max(0, stack.getOrDefault(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_MOVE_TICKS.get(), 0)) + 1;

            while (moveTicks >= 20 && charges > 0) {
                charges--;
                moveTicks -= 20;
                relicData.getLevelingData().addExperience("spectral", "spectral_time", 1D);
                ability.getStatisticData().getMetricData("spectral_time").addValue(1D);
            }

            if (charges <= 0)
                moveTicks = 0;

            stack.set(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_MOVE_TICKS.get(), moveTicks);
            stack.set(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_CHARGES.get(), charges);

            if (charges <= 0) {
                this.releaseUsing(stack, level, player, remainingUseDuration);
                player.stopUsingItem();
                return;
            }
        }

        var revealRadius = Math.max(0D, ability.getStatData("entity_reveal_radius").getValue());

        if (ability.getRankModifierData("spectral_vision").isEnabled() && revealRadius > 0D) {
            var revealEntities = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(revealRadius),
                    target -> target != player && target.isAlive());

            for (var target : revealEntities)
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 10, 0, false, false, true), player);
        }

        if ((playerBlockState.isAir() || playerBlockState.getFluidState().is(FluidTags.WATER))
                && (playerBlockStateAbove.isAir() || playerBlockStateAbove.getFluidState().is(FluidTags.WATER))
                && playerBlockState.getCollisionShape(level, playerBlockPos).isEmpty()
                && playerBlockStateAbove.getCollisionShape(level, playerBlockPos.above()).isEmpty()
                && !playerBlockState.is(BlockRegistry.INTANGIBLE_AIR.get())
                && !playerBlockStateAbove.is(BlockRegistry.INTANGIBLE_AIR.get())) {
            stack.set(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_POSITION.get(), new WorldPosition(player));
        }

        var spectralRadius = 2.5D;
        var tunnelDirection = player.getLookAngle();

        if (tunnelDirection.lengthSqr() <= 1.0E-6D)
            tunnelDirection = player.getKnownMovement();

        if (tunnelDirection.lengthSqr() <= 1.0E-6D)
            tunnelDirection = Vec3.directionFromRotation(0F, player.getYRot());

        tunnelDirection = tunnelDirection.normalize();

        var movementSpeed = Math.max(
                Math.max(player.getDeltaMovement().length(), player.getKnownMovement().length()),
                0.62D
        );
        var predictionDistance = Mth.clamp(movementSpeed * 20D + 2.2D, 2D, 5D);
        var sampleStep = Mth.clamp(spectralRadius * 0.35D, 0.15D, 0.3D);
        var sampleCount = Math.max(1, (int) Math.ceil(predictionDistance / sampleStep));
        var sphereRadius = spectralRadius;
        var sphereRadiusSqr = sphereRadius * sphereRadius;
        var minYOffset = 0.15D;
        var maxYOffset = Math.max(minYOffset, player.getBbHeight() - 0.15D);
        var yStep = 0.55D;
        var basePos = player.position();
        var targetBlocks = new HashSet<BlockPos>();

        for (var sample = 0; sample <= sampleCount; sample++) {
            var sampleDistance = Math.min(predictionDistance, sample * sampleStep);
            var pathCenter = basePos.add(tunnelDirection.scale(sampleDistance));

            for (double yOffset = minYOffset; yOffset <= maxYOffset + 1.0E-6D; yOffset += yStep) {
                var center = pathCenter.add(0D, yOffset, 0D);
                var minX = Mth.floor(center.x - sphereRadius);
                var maxX = Mth.floor(center.x + sphereRadius);
                var minY = Mth.floor(center.y - sphereRadius);
                var maxY = Mth.floor(center.y + sphereRadius);
                var minZ = Mth.floor(center.z - sphereRadius);
                var maxZ = Mth.floor(center.z + sphereRadius);

                for (var x = minX; x <= maxX; x++) {
                    for (var y = minY; y <= maxY; y++) {
                        for (var z = minZ; z <= maxZ; z++) {
                            var dx = x + 0.5D - center.x;
                            var dy = y + 0.5D - center.y;
                            var dz = z + 0.5D - center.z;

                            if (dx * dx + dy * dy + dz * dz <= sphereRadiusSqr)
                                targetBlocks.add(new BlockPos(x, y, z));
                        }
                    }
                }
            }
        }

        for (var blockPos : targetBlocks) {
            var state = level.getBlockState(blockPos);
            var intangible = EffectIntangible.INSTANCE;

            if (level.getBlockEntity(blockPos) != null || state.isAir() || state.getDestroySpeed(level, blockPos) < 0F
                    || !BlockUtil.destroyRespectsClaim(intangible.getPlayer(player, serverLevel), level, blockPos))
                continue;

            level.setBlockAndUpdate(blockPos, BlockRegistry.INTANGIBLE_AIR.defaultBlockState());

            if (level.getBlockEntity(blockPos) instanceof IntangibleAirTile tile) {
                tile.stateID = Block.getId(state);
                tile.duration = 0;
                tile.maxLength = 10;
            }
        }

        var refreshRadius = Mth.clamp((int) Math.ceil(2.5D + 2D), 4, 8);
        var centerPos = player.blockPosition();

        for (var x = centerPos.getX() - refreshRadius; x <= centerPos.getX() + refreshRadius; x++) {
            for (var y = centerPos.getY() - refreshRadius; y <= centerPos.getY() + refreshRadius; y++) {
                for (var z = centerPos.getZ() - refreshRadius; z <= centerPos.getZ() + refreshRadius; z++) {
                    var nearbyPos = new BlockPos(x, y, z);

                    if (!level.getBlockState(nearbyPos).is(BlockRegistry.INTANGIBLE_AIR.get()))
                        continue;

                    if (level.getBlockEntity(nearbyPos) instanceof IntangibleAirTile tile) {
                        tile.duration = 0;
                    }
                }
            }
        }

        stack.set(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_CHARGES.get(), charges);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!(entity instanceof ServerPlayer player) || level.isClientSide() || stack.getItem() != this)
            return;

        var relicData = this.getRelicData(player, stack);
        var ability = relicData.getAbilitiesData().getAbilityData("spectral");

        var maxCharges = Math.max(1, (int) Math.round(Math.max(0D, ability.getStatData("max_charges").getValue())));
        var charges = Mth.clamp(stack.getOrDefault(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_CHARGES.get(), maxCharges), 0, maxCharges);
        var isUsingThisStack = player.isUsingItem() && player.getUseItem() == stack;
        var regenIntervalTicks = Math.max(1, (int) Math.round(Math.max(0.05D, ability.getStatData("charge_regen_time").getValue()) * 20D));
        var regenTicks = Math.max(0, stack.getOrDefault(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_REGEN_TICKS.get(), 0));
        var fallImmunityTicks = stack.getOrDefault(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_FALL_IMMUNITY_TICKS.get(), 0);

        if (!isUsingThisStack && fallImmunityTicks > 0) {
            if (player.onGround())
                stack.set(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_FALL_IMMUNITY_TICKS.get(), 0);
            else
                player.fallDistance = 0F;
        }

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

        stack.set(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_CHARGES.get(), charges);
        stack.set(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_REGEN_TICKS.get(), regenTicks);

        if (!isUsingThisStack && stack.getOrDefault(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_MOVE_TICKS.get(), 0) > 0)
            stack.set(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_MOVE_TICKS.get(), 0);

        if (!isUsingThisStack)
            stack.set(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_WAS_IN_SPECTRAL.get(), false);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (!(entity instanceof ServerPlayer player) || level.isClientSide()) {
            stack.remove(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_POSITION.get());
            stack.remove(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_MOVE_TICKS.get());
            stack.remove(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_WAS_IN_SPECTRAL.get());
            return;
        }

        player.fallDistance = 0F;

        var position = stack.get(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_POSITION.get());
        var ability = this.getRelicData(player, stack).getAbilitiesData().getAbilityData("spectral");
        var playerBlockPos = player.blockPosition();
        var playerBlockState = level.getBlockState(playerBlockPos);
        var playerBlockStateAbove = level.getBlockState(playerBlockPos.above());
        var teleportedToSafePosition = false;

        if (position != null && ability.getRankModifierData("safe_return").isEnabled()
                && (!playerBlockStateAbove.getCollisionShape(level, playerBlockPos.above()).isEmpty() || playerBlockState.is(BlockRegistry.INTANGIBLE_AIR.get())
                || playerBlockStateAbove.is(BlockRegistry.INTANGIBLE_AIR.get()))) {
            var targetLevel = player.server.getLevel(position.getLevel());

            if (targetLevel != null) {
                var targetPos = position.getPos();
                var safeY = targetPos.y();
                var minY = Math.max(targetLevel.getMinBuildHeight() + 1, targetPos.y() - 16);

                for (var y = targetPos.y(); y >= minY; y--) {
                    var candidatePos = new BlockPos((int) targetPos.x(), (int) y, (int) targetPos.z());
                    var candidateBelow = candidatePos.below();
                    var feetState = targetLevel.getBlockState(candidatePos);
                    var headState = targetLevel.getBlockState(candidatePos.above());
                    var belowState = targetLevel.getBlockState(candidateBelow);
                    var feetFree = (feetState.isAir() || feetState.getFluidState().is(FluidTags.WATER))
                            && feetState.getCollisionShape(targetLevel, candidatePos).isEmpty()
                            && !feetState.is(BlockRegistry.INTANGIBLE_AIR.get());
                    var headFree = (headState.isAir() || headState.getFluidState().is(FluidTags.WATER))
                            && headState.getCollisionShape(targetLevel, candidatePos.above()).isEmpty()
                            && !headState.is(BlockRegistry.INTANGIBLE_AIR.get());
                    var hasSupport = !belowState.getCollisionShape(targetLevel, candidateBelow).isEmpty()
                            || belowState.getFluidState().is(FluidTags.WATER);

                    if (feetFree && headFree && hasSupport) {
                        safeY = y;
                        break;
                    }
                }

                player.teleportTo(targetLevel, targetPos.x() + 0.5D, safeY + player.getBbHeight(), targetPos.z() + 0.5D, player.getYRot(), player.getXRot());

                player.setDeltaMovement(player.getLookAngle().scale(-0.5F));
                player.hurtMarked = true;
                player.fallDistance = 0F;
                player.level().playSound(null, player, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 0.9F + player.getRandom().nextFloat() * 0.2F);
                teleportedToSafePosition = true;
            }
        }

        if (playerBlockState.is(BlockRegistry.INTANGIBLE_AIR.get()) || playerBlockStateAbove.is(BlockRegistry.INTANGIBLE_AIR.get())
                || stack.getOrDefault(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_WAS_IN_SPECTRAL.get(), false)
                || teleportedToSafePosition) {
            stack.set(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_FALL_IMMUNITY_TICKS.get(), 1);
        }

        stack.remove(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_POSITION.get());
        stack.remove(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_MOVE_TICKS.get());
        stack.remove(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_WAS_IN_SPECTRAL.get());
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @EventBusSubscriber(modid = ReliquifiedArsNouveau.MODID)
    public static class CommonEvents {
        @SubscribeEvent
        public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || event.getAmount() <= 0F)
                return;

            if (!event.getSource().is(DamageTypes.FALL))
                return;

            var mainHand = player.getMainHandItem();
            var offHand = player.getOffhandItem();
            var mainProtected = mainHand.getItem() instanceof StaffOfTheSpectralWalkerItem
                    && mainHand.getOrDefault(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_FALL_IMMUNITY_TICKS.get(), 0) > 0;
            var offProtected = offHand.getItem() instanceof StaffOfTheSpectralWalkerItem
                    && offHand.getOrDefault(RANDataComponentRegistry.STAFF_OF_THE_SPECTRAL_WALKER_FALL_IMMUNITY_TICKS.get(), 0) > 0;

            if (!mainProtected && !offProtected)
                return;

            event.setCanceled(true);
            player.fallDistance = 0F;
        }
    }
}

