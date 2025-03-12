package it.hurts.octostudios.reliquified_ars_nouveau.items.head;

import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.NouveauRelicItem;
import it.hurts.octostudios.reliquified_ars_nouveau.items.base.loot.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.*;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemColor;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemShape;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.BeamsData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import top.theillusivec4.curios.api.SlotContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HornOfWildHunterItem extends NouveauRelicItem {
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("summoner")
                                .stat(StatData.builder("damage")
                                        .initialValue(4D, 5D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.2)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingData.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .sources(LevelingSourcesData.builder()
                                .source(LevelingSourceData.abilityBuilder("summoner")
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
        if (!(slotContext.entity() instanceof Player player) || player.getCommandSenderWorld().isClientSide() || player.tickCount < 20)
            return;

        var level = (ServerLevel) player.getCommandSenderWorld();
        var livingWolves = new ArrayList<>(getWolves(stack));

        for (int i = livingWolves.size() - 1; i >= 0; i--) {
            var wolf = level.getEntity(livingWolves.get(i));

            if (wolf == null)
                livingWolves.remove(i);
        }

        if (livingWolves.size() >= 2) {
            for (UUID wolfUUID : livingWolves) {
                var wolf = level.getEntity(wolfUUID);

                if (wolf == null)
                    continue;

                if (player.distanceTo(wolf) > 32 && !player.getBlockStateOn().isAir())
                    wolf.teleportTo(player.getX(), player.getY(), player.getZ());
            }
        } else {
            var random = player.getRandom();
            var angle = random.nextDouble() * 2 * Math.PI;
            var radius = 1 + random.nextInt(2);

            var offsetX = (int) Math.round(Math.cos(angle) * radius);
            var offsetZ = (int) Math.round(Math.sin(angle) * radius);

            var spawnPos = player.blockPosition().offset(offsetX, 0, offsetZ);

            for (int i = 0; i < 5; i++) {
                if (!hasCollision(level, spawnPos) && !hasCollision(level, spawnPos.above()))
                    break;

                spawnPos = spawnPos.above();
            }

            if (hasCollision(level, spawnPos) || hasCollision(level, spawnPos.above()))
                spawnPos = player.blockPosition();

            for (int i = 0; i < 2 - livingWolves.size(); i++) {
                var wolf = new Wolf(EntityType.WOLF, level);

                wolf.setOwnerUUID(player.getUUID());

                wolf.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);

                wolf.setTame(true, true);
                wolf.setAggressive(true);
                wolf.setInvulnerable(true);

                level.addFreshEntity(wolf);

                EntityUtils.applyAttribute(wolf, stack, Attributes.ATTACK_DAMAGE, (int) MathUtils.round(getStatValue(stack, "summoner", "damage"), 0), AttributeModifier.Operation.ADD_VALUE);

                var startPos = new Vec3(spawnPos.getX() + 0.5, spawnPos.getY() + wolf.getBbHeight() / 2, spawnPos.getZ() + 0.5);
                var endPos = player.position();

                int particleCount = 10;

                for (int j = 0; j <= particleCount; j++) {
                    double t = j / (double) particleCount;

                    level.sendParticles(ParticleUtils.constructSimpleSpark(new Color(150 + random.nextInt(100), 150 + random.nextInt(100), 150 + random.nextInt(100)), 0.3F, 40, 0.9F),
                            Mth.lerp(t, startPos.x, endPos.x), Mth.lerp(t, startPos.y, endPos.y), Mth.lerp(t, startPos.z, endPos.z), 10, 0.1, 0.1, 0.1, 0.01);
                }

                livingWolves.add(wolf.getUUID());
            }

            level.playSound(null, player, SoundEvents.WOLF_HOWL, SoundSource.PLAYERS, 0.3F, 0.9F + random.nextFloat() * 0.2F);

            setWolves(stack, livingWolves);
        }
    }

    private boolean hasCollision(Level level, BlockPos pos) {
        return level.getBlockState(pos).getCollisionShape(level, pos).max(Direction.Axis.Y) >= 0.25;
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player) || player.getCommandSenderWorld().isClientSide()
                || newStack.getItem() == stack.getItem() || getWolves(stack).isEmpty())
            return;

        for (int i = 0; i < 2; i++) {
            var entity = ((ServerLevel) player.getCommandSenderWorld()).getEntity(getWolves(stack).get(i));

            if (entity == null)
                continue;

            entity.discard();
        }
    }

    public void setWolves(ItemStack stack, List<UUID> list) {
        stack.set(RANDataComponentRegistry.WOLVES, list);
    }

    public List<UUID> getWolves(ItemStack stack) {
        return stack.getOrDefault(RANDataComponentRegistry.WOLVES, new ArrayList<>());
    }

    @EventBusSubscriber
    public static class HornsOfWildHunterEvent {
        @SubscribeEvent
        public static void onDogChangeDimension(EntityTravelToDimensionEvent event) {
            if (!(event.getEntity() instanceof Wolf wolf) || wolf.getCommandSenderWorld().isClientSide()
                    || !wolf.isTame() || !(wolf.getOwner() instanceof Player player))
                return;

            for (var stack : EntityUtils.findEquippedCurios(player, ItemRegistry.HORN_OF_THE_WILD_HUNTER.value())) {
                if (!(stack.getItem() instanceof HornOfWildHunterItem relic) || relic.getWolves(stack).isEmpty())
                    return;

                for (UUID wolfUUID : relic.getWolves(stack)) {
                    var oldWolf = ((ServerLevel) player.getCommandSenderWorld()).getEntity(wolfUUID);

                    if (oldWolf == null)
                        continue;

                    oldWolf.discard();
                }
            }
        }

        @SubscribeEvent
        public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
            var player = event.getEntity();

            if (player.getCommandSenderWorld().isClientSide())
                return;

            var level = (ServerLevel) player.getCommandSenderWorld();
            var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.HORN_OF_THE_WILD_HUNTER.value());

            if (stacks.isEmpty())
                return;

            for (var stack : stacks) {
                if (!(stack.getItem() instanceof HornOfWildHunterItem relic) || relic.getWolves(stack).isEmpty())
                    return;

                for (UUID wolfUUID : relic.getWolves(stack)) {
                    var oldWolf = level.getServer().getLevel(event.getFrom()).getEntity(wolfUUID);

                    if (oldWolf == null)
                        continue;

                    oldWolf.discard();
                }
            }
        }
    }
}
