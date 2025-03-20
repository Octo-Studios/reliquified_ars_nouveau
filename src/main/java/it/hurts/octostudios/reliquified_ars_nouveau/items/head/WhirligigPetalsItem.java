package it.hurts.octostudios.reliquified_ars_nouveau.items.head;

import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.NouveauRelicItem;
import it.hurts.octostudios.reliquified_ars_nouveau.items.base.loot.LootEntries;
import it.hurts.octostudios.reliquified_ars_nouveau.network.packets.PetalsJumpPacket;
import it.hurts.sskirillss.relics.init.DataComponentRegistry;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.*;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemColor;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemShape;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.BeamsData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import top.theillusivec4.curios.api.SlotContext;

import java.awt.*;

public class WhirligigPetalsItem extends NouveauRelicItem {
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("petals")
                                .stat(StatData.builder("duration")
                                        .initialValue(3D, 5D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.1)
                                        .formatValue(value -> MathUtils.round(value / 20, 2))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingData.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .sources(LevelingSourcesData.builder()
                                .source(LevelingSourceData.abilityBuilder("petals")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.ORANGE)
                                        .build())
                                .build())
                        .build())
                .style(StyleData.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xff85543c)
                                .borderBottom(0xff85543c)
                                .build())
                        .beams(BeamsData.builder()
                                .startColor(0xFFef3398)
                                .endColor(0x00c31560)
                                .build())
                        .build())
                .loot(LootData.builder()
                        .entry(LootEntries.ARS_NOUVEAU, LootEntries.ARS_NOUVEAU_LIKE)
                        .build())
                .build();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player) || !isAbilityUnlocked(stack, "petals"))
            return;

        var statValue = getActualStatValue(player, stack);
        var level = player.getCommandSenderWorld();
        var isJumping = getToggled(stack);

        if (!level.isClientSide()) {
            if (isJumping)
                addTime(stack, 1);

            var knowYMovement = player.getKnownMovement().y;

            if (!hasSolidBlockOnRay(player, level, player.getAttributeValue(Attributes.SAFE_FALL_DISTANCE))
                    && !isJumping && knowYMovement <= knowYMovement * 0.6)
                setToggledSlowFall(stack, true);

            if (getTime(stack) >= statValue)
                setToggled(stack, false);

            if (player.onGround() || player.isInLiquid()) {
                setToggledSlowFall(stack, false);
                addTime(stack, -getTime(stack));
            }

            if (!player.isShiftKeyDown())
                player.fallDistance = 0;
        } else {
            if (!(player instanceof LocalPlayer localPlayer) || player.isFallFlying())
                return;

            var random = player.getRandom();
            var deltaMovement = player.getDeltaMovement();

            if (getToggledSlowFall(stack) && !localPlayer.isShiftKeyDown() && !localPlayer.getAbilities().flying) {
                player.setDeltaMovement(new Vec3(deltaMovement.x, -0.3, deltaMovement.z));

                double angle1 = (player.tickCount * 0.3F) % (2 * Math.PI);
                double angle2 = (angle1 + Math.PI) % (2 * Math.PI);

                double radius = 0.4;

                renderParticle(player, radius * Math.cos(angle1), Math.sin(angle1 * 2) * 0.1, radius * Math.sin(angle1));
                renderParticle(player, radius * Math.cos(angle2), Math.sin(angle2 * 2) * 0.1, radius * Math.sin(angle2));
            }

            if (!getToggled(stack))
                return;

            NetworkHandler.sendToServer(new PetalsJumpPacket(localPlayer.input.jumping));

            player.setDeltaMovement(new Vec3(deltaMovement.x, 0.4 + getTime(stack) * 0.1, deltaMovement.z));

            for (int i = 0; i < 10; i++) {
                double offsetX = (random.nextDouble() - 0.5) * 0.7;
                double offsetZ = (random.nextDouble() - 0.5) * 0.7;

                level.addParticle(ParticleUtils.constructSimpleSpark(new Color(100 + random.nextInt(56), 200 + random.nextInt(56), 50 + random.nextInt(56)), 0.3F, 20, 0.7F),
                        player.getX() + offsetX, player.getY() - 0.5, player.getZ() + offsetZ, 0, 0, 0);
            }
        }
    }

    public void renderParticle(Player player, double offsetX, double offsetY, double offsetZ) {
        var random = player.getRandom();

        player.getCommandSenderWorld().addParticle(ParticleUtils.constructSimpleSpark(new Color(100 + random.nextInt(56), 200 + random.nextInt(56), 50 + random.nextInt(56)), 0.3F, 20, 0.7F),
                player.getX() + offsetX, player.getY() + player.getBbHeight() + offsetY + 0.5, player.getZ() + offsetZ, 0, 0, 0);
    }

    public static boolean hasSolidBlockOnRay(Player player, Level level, double maxDistance) {
        var start = player.position();

        return level.getBlockState(level.clip(new ClipContext(start, start.subtract(0, maxDistance, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getBlockPos()).isSolid();
    }

    public double getActualStatValue(Player player, ItemStack stack) {
        var statValue = getStatValue(stack, "petals", "duration");

        if (player.hasEffect(MobEffects.JUMP)) {
            MobEffectInstance jumpBoost = player.getEffect(MobEffects.JUMP);

            if (jumpBoost != null)
                statValue += (jumpBoost.getAmplifier() + 1) * 4;
        }

        var count = EntityUtils.findEquippedCurios(player, ItemRegistry.WHIRLIGIG_PETALS.value()).size();

        statValue += count > 1 ? count * 4 : 0;

        return statValue;
    }

    public void addTime(ItemStack stack, int val) {
        stack.set(DataComponentRegistry.TIME, getTime(stack) + val);
    }

    public int getTime(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.TIME, 0);
    }

    public void setToggled(ItemStack stack, boolean val) {
        stack.set(DataComponentRegistry.TOGGLED, val);
    }

    public boolean getToggled(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.TOGGLED, false);
    }

    public void setToggledSlowFall(ItemStack stack, boolean val) {
        stack.set(RANDataComponentRegistry.TOGGLED_COOLDOWN, val);
    }

    public boolean getToggledSlowFall(ItemStack stack) {
        return stack.getOrDefault(RANDataComponentRegistry.TOGGLED_COOLDOWN, false);
    }

    @EventBusSubscriber
    public static class WhirligigPetalsEvent {
        @SubscribeEvent
        public static void onPlayerJumping(LivingEvent.LivingJumpEvent event) {
            if (!(event.getEntity() instanceof Player player))
                return;

            var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.WHIRLIGIG_PETALS.value());

            if (!(stack.getItem() instanceof WhirligigPetalsItem relic) || !relic.canPlayerUseAbility(player, stack, "petals"))
                return;

            relic.setToggled(stack, true);
        }
    }
}
