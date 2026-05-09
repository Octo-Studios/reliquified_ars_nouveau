package it.hurts.shatterbyte.reliquified_ars_nouveau.items;

import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.shatterbyte.reliquified_ars_nouveau.entities.WhirlingBroomEntity;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.EntityRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.base.RANRelicItem;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.base.loot.LootEntries;
import it.hurts.shatterbyte.reliquified_ars_nouveau.network.NetworkHandler;
import it.hurts.shatterbyte.reliquified_ars_nouveau.network.packets.C2SBroomStormElectricityPacket;
import it.hurts.shatterbyte.reliquified_ars_nouveau.network.packets.C2SBroomSprintStatePacket;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.api.relics.synergies.SynergyTemplate;
import it.hurts.sskirillss.relics.api.relics.synergies.conditions.AbilityConditionTemplate;
import it.hurts.sskirillss.relics.api.relics.synergies.conditions.RelicConditionTemplate;
import it.hurts.sskirillss.relics.api.relics.synergies.stats.SynergyStatTemplate;
import it.hurts.sskirillss.relics.entities.ChainedElectricityEntity;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.RelicsRelicContainers;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.UUID;

public class WhirlingBroomItem extends RANRelicItem {
    public static final String BROOM_STACK_ID_TAG = "ran_whirling_broom_stack_id";

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("broom")
                                .rankModifier(1, "flight_guard")
                                .rankModifier(3, "projectile_phase")
                                .rankModifier(5, "sprint_boost")
                                .stat(AbilityStatTemplate.builder("speed")
                                        .initialValue(0.25D, 0.5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 1D)
                                        .formatValue(value -> MathUtils.round(value, 2))
                                        .build())
                                .stat(AbilityStatTemplate.builder("max_height")
                                        .initialValue(15D, 30D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 100D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("broom_health")
                                        .initialValue(1D, 3D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 20D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("revive_cooldown")
                                        .initialValue(30D, 15D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 1D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("health_regen_interval")
                                        .initialValue(10D, 7.5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 1D)
                                        .formatValue(value -> MathUtils.round(value, 2))
                                        .build())
                                .stat(AbilityStatTemplate.builder("damage_reduction")
                                        .initialValue(0.1D, 0.25D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.75D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("sprint_speed_bonus")
                                        .initialValue(0.5D, 0.75D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 2D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("sprint_mana_per_second")
                                        .initialValue(100D, 75D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 10D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .synergy(SynergyTemplate.builder("storm_flight")
                                .modes("enabled", "disabled")
                                .stat(SynergyStatTemplate.builder("damage")
                                        .thresholdValue(1, 5)
                                        .formatValue(value -> value)
                                        .build())
                                .stat(SynergyStatTemplate.builder("lifetime")
                                        .thresholdValue(3, 10)
                                        .formatValue(value -> value)
                                        .build())
                                .condition(RelicConditionTemplate.builder(() -> (WhirlingBroomItem) ItemRegistry.WHIRLING_BROOM.get())
                                        .container(RelicsRelicContainers.INVENTORY.get())
                                        .condition(AbilityConditionTemplate.builder("broom").build())
                                        .build())
                                .condition(RelicConditionTemplate.builder(RelicsItems.JELLYFISH_NECKLACE::get)
                                        .container(RelicsRelicContainers.CURIOS.get())
                                        .condition(AbilityConditionTemplate.builder("shock").build())
                                        .build())
                                .build())
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.ARS_NOUVEAU)
                        .entry(LootEntries.ARS_NOUVEAU_LIKE)
                        .build())
                .build();
    }

    public static void setCooldownUntil(ItemStack stack, long gameTime) {
        stack.set(RANDataComponentRegistry.WHIRLING_BROOM_COOLDOWN_UNTIL.get(), Math.max(0L, gameTime));
    }

    public static long getCooldownUntil(ItemStack stack) {
        return stack.getOrDefault(RANDataComponentRegistry.WHIRLING_BROOM_COOLDOWN_UNTIL.get(), 0L);
    }

    public static ChainedElectricityEntity getLastElectricityEntity(ServerPlayer owner, ItemStack stack) {
        var entityId = stack.getOrDefault(RANDataComponentRegistry.WHIRLING_BROOM_LAST_ELECTRICITY_ID.get(), -1);

        if (entityId < 0)
            return null;

        var raw = owner.level().getEntity(entityId);

        if (raw instanceof ChainedElectricityEntity electricity && electricity.isAlive())
            return electricity;

        stack.set(RANDataComponentRegistry.WHIRLING_BROOM_LAST_ELECTRICITY_ID.get(), -1);
        return null;
    }

    public int getReviveCooldownTicks(ItemStack stack) {
        var cooldownSeconds = Math.max(0D, this.getRelicData(null, stack).getAbilitiesData().getAbilityData("broom").getStatData("revive_cooldown").getValue());

        return Math.max(0, (int) Math.round(cooldownSeconds * 20D));
    }

    public int getHealthRegenIntervalTicks(ItemStack stack) {
        var seconds = Math.max(0.05D, this.getRelicData(null, stack).getAbilitiesData().getAbilityData("broom").getStatData("health_regen_interval").getValue());

        return Math.max(1, (int) Math.round(seconds * 20D));
    }

    public float getBroomMaxHealth(ItemStack stack) {
        return Math.round(this.getRelicData(null, stack).getAbilitiesData().getAbilityData("broom").getStatData("broom_health").getValue());
    }

    public static String ensureStackId(ItemStack stack) {
        var stackId = stack.getOrDefault(RANDataComponentRegistry.WHIRLING_BROOM_STACK_ID.get(), "");

        if (stackId.isBlank()) {
            stackId = UUID.randomUUID().toString();
            stack.set(RANDataComponentRegistry.WHIRLING_BROOM_STACK_ID.get(), stackId);
        }

        return stackId;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        if (!(stack.getItem() instanceof WhirlingBroomItem item))
            return false;

        var level = Minecraft.getInstance().level;
        var cooldownUntil = getCooldownUntil(stack);

        return cooldownUntil > 0L && item.getReviveCooldownTicks(stack) > 0 && (level == null || cooldownUntil > level.getGameTime());
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if (!(stack.getItem() instanceof WhirlingBroomItem item))
            return 0;

        var totalTicks = item.getReviveCooldownTicks(stack);
        var cooldownUntil = getCooldownUntil(stack);

        if (totalTicks <= 0 || cooldownUntil <= 0L)
            return 0;

        var level = Minecraft.getInstance().level;
        var gameTime = level == null ? 0L : level.getGameTime();
        var remainingTicks = Math.max(0L, cooldownUntil - gameTime);
        var progress = 1D - Math.min(1D, remainingTicks / (double) totalTicks);

        return Mth.clamp((int) Math.floor(progress * 13D), 0, 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x52D8FF;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        if (!(entity instanceof ServerPlayer player) || level.isClientSide() || !(stack.getItem() instanceof WhirlingBroomItem item))
            return;

        var maxHealth = item.getBroomMaxHealth(stack);
        var currentHealth = Mth.clamp(stack.getOrDefault(RANDataComponentRegistry.WHIRLING_BROOM_CURRENT_HEALTH.get(), maxHealth), 0F, maxHealth);
        var regenTicks = Math.max(0, stack.getOrDefault(RANDataComponentRegistry.WHIRLING_BROOM_HEALTH_REGEN_TICKS.get(), 0));
        var intervalTicks = item.getHealthRegenIntervalTicks(stack);
        var stackId = ensureStackId(stack);
        WhirlingBroomEntity linkedBroom = null;

        if (player.getVehicle() instanceof WhirlingBroomEntity broom && stackId.equals(broom.getPersistentData().getString(BROOM_STACK_ID_TAG)))
            linkedBroom = broom;

        if (linkedBroom != null) {
            var maxHealthAttribute = linkedBroom.getAttribute(Attributes.MAX_HEALTH);

            if (maxHealthAttribute != null)
                maxHealthAttribute.setBaseValue(maxHealth);

            currentHealth = Mth.clamp(linkedBroom.getHealth(), 0F, maxHealth);
        }

        if (currentHealth < maxHealth) {
            regenTicks++;

            while (regenTicks >= intervalTicks && currentHealth < maxHealth) {
                regenTicks -= intervalTicks;
                currentHealth = Math.min(maxHealth, currentHealth + 1F);
            }
        } else {
            regenTicks = 0;
        }

        if (linkedBroom != null && linkedBroom.isAlive())
            linkedBroom.setHealth(Mth.clamp(currentHealth, 0F, maxHealth));

        stack.set(RANDataComponentRegistry.WHIRLING_BROOM_CURRENT_HEALTH.get(), currentHealth);
        stack.set(RANDataComponentRegistry.WHIRLING_BROOM_HEALTH_REGEN_TICKS.get(), regenTicks);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);

        if (level.isClientSide())
            return InteractionResultHolder.sidedSuccess(stack, true);

        var ability = this.getRelicData(player, stack).getAbilitiesData().getAbilityData("broom");
        var cooldownUntil = getCooldownUntil(stack);

        if (cooldownUntil > 0L && cooldownUntil <= level.getGameTime())
            stack.remove(RANDataComponentRegistry.WHIRLING_BROOM_COOLDOWN_UNTIL.get());

        if (cooldownUntil > level.getGameTime())
            return InteractionResultHolder.fail(stack);

        if (player.getVehicle() instanceof WhirlingBroomEntity)
            return InteractionResultHolder.fail(stack);

        for (var broom : level.getEntitiesOfClass(WhirlingBroomEntity.class, player.getBoundingBox().inflate(96D), entity -> entity.getFirstPassenger() == player))
            broom.discard();

        var broom = new WhirlingBroomEntity(EntityRegistry.WHIRLING_BROOM.get(), level);
        var synergy = this.getRelicData(player, stack).getAbilitiesData().getSynergyData("storm_flight");
        var stackId = ensureStackId(stack);

        broom.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
        broom.setBaseSpeed((float) Math.max(0D, ability.getStatData("speed").getValue()));
        broom.setMaxHeight((float) Math.max(0D, ability.getStatData("max_height").getValue()));
        broom.setDamageReduction((float) Math.max(0D, ability.getStatData("damage_reduction").getValue()));
        broom.setSprintBoostUnlocked(ability.getRankModifierData("sprint_boost").isEnabled());
        broom.setSprintSpeedBonus((float) Math.max(0D, ability.getStatData("sprint_speed_bonus").getValue()));
        broom.setSprintManaPerSecond((float) Math.max(0D, ability.getStatData("sprint_mana_per_second").getValue()));
        broom.setStormDamage((float) (synergy.isUnlocked() && !synergy.getMode().equals("disabled") ? Math.max(0D, synergy.getStatData("damage").getValue()) : 0D));
        broom.setStormLifetime(synergy.isUnlocked() && !synergy.getMode().equals("disabled") ? Math.max(0, (int) synergy.getStatData("lifetime").getValue()) : 0);
        broom.setStormFlawless(this.getRelicData(player, stack).isFlawless());
        broom.setFlawless(this.getRelicData(player, stack).isFlawless());
        broom.getPersistentData().putString(BROOM_STACK_ID_TAG, stackId);

        var broomHealth = Math.max(1D, ability.getStatData("broom_health").getValue());
        var healthAttribute = broom.getAttribute(Attributes.MAX_HEALTH);
        var storedHealth = Mth.clamp(stack.getOrDefault(RANDataComponentRegistry.WHIRLING_BROOM_CURRENT_HEALTH.get(), (float) broomHealth), 0F, (float) broomHealth);
        var spawnHealth = Math.max(1F, storedHealth);

        if (healthAttribute != null)
            healthAttribute.setBaseValue(broomHealth);

        broom.setHealth(spawnHealth);
        stack.set(RANDataComponentRegistry.WHIRLING_BROOM_CURRENT_HEALTH.get(), spawnHealth);

        level.addFreshEntity(broom);
        player.startRiding(broom, true);

        return InteractionResultHolder.success(stack);
    }

    @EventBusSubscriber(modid = ReliquifiedArsNouveau.MODID)
    public static class CommonEvents {
        @SubscribeEvent
        public static void onLivingDamagePre(LivingIncomingDamageEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || event.getAmount() <= 0F || !(player.getVehicle() instanceof WhirlingBroomEntity broom))
                return;

            var baseDamage = Math.max(0D, event.getAmount());
            var totalReduction = 0D;

            for (var stack : EntityUtils.findItemsInInventory(player, ItemRegistry.WHIRLING_BROOM.get())) {
                if (!(stack.getItem() instanceof WhirlingBroomItem item))
                    continue;

                var ability = item.getRelicData(player, stack).getAbilitiesData().getAbilityData("broom");

                if (!ability.getRankModifierData("flight_guard").isEnabled())
                    continue;

                totalReduction += Mth.clamp(ability.getStatData("damage_reduction").getValue(), 0D, 1D);
            }

            if (totalReduction > 0D)
                event.setAmount((float) Math.max(0D, baseDamage * (1D - Mth.clamp(totalReduction, 0D, 0.95D))));
        }

        @SubscribeEvent
        public static void onProjectileImpact(ProjectileImpactEvent event) {
            if (!(event.getRayTraceResult() instanceof EntityHitResult hitResult))
                return;

            if (hitResult.getEntity() instanceof Player player) {
                if (player.getVehicle() instanceof WhirlingBroomEntity) {
                    for (var stack : EntityUtils.findItemsInInventory(player, ItemRegistry.WHIRLING_BROOM.get())) {
                        if (!(stack.getItem() instanceof WhirlingBroomItem item))
                            continue;

                        var ability = item.getRelicData(player, stack).getAbilitiesData().getAbilityData("broom");

                        if (ability.getRankModifierData("projectile_phase").isEnabled())
                            event.setCanceled(true);

                        return;
                    }

                    return;
                }
            }

            if (hitResult.getEntity() instanceof WhirlingBroomEntity)
                event.setCanceled(true);
        }

        @EventBusSubscriber(modid = ReliquifiedArsNouveau.MODID, value = Dist.CLIENT)
        public static class ClientEvents {
            private static boolean ran$lastSprintRequested = false;

            @SubscribeEvent
            public static void onClientTickPost(ClientTickEvent.Post event) {
                var minecraft = Minecraft.getInstance();
                var player = minecraft.player;

                if (player == null) {
                    if (ran$lastSprintRequested) {
                        NetworkHandler.sendToServer(new C2SBroomSprintStatePacket(false));
                        ran$lastSprintRequested = false;
                    }
                    return;
                }

                if (player.getVehicle() instanceof WhirlingBroomEntity broom) {
                    var sprintRequested = minecraft.options.keySprint.isDown();

                    if (sprintRequested != ran$lastSprintRequested) {
                        NetworkHandler.sendToServer(new C2SBroomSprintStatePacket(sprintRequested));
                        ran$lastSprintRequested = sprintRequested;
                    }

                    if (broom.getStormDamage() > 0F && broom.getStormLifetime() > 0)
                        NetworkHandler.sendToServer(new C2SBroomStormElectricityPacket());
                } else if (ran$lastSprintRequested) {
                    NetworkHandler.sendToServer(new C2SBroomSprintStatePacket(false));
                    ran$lastSprintRequested = false;
                }
            }
        }
    }
}

