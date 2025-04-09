package it.hurts.octostudios.reliquified_ars_nouveau.items.bracelet;

import it.hurts.octostudios.reliquified_ars_nouveau.entities.BallistarianBowEntity;
import it.hurts.octostudios.reliquified_ars_nouveau.entities.MagicShellEntity;
import it.hurts.octostudios.reliquified_ars_nouveau.init.EntityRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.NouveauRelicItem;
import it.hurts.octostudios.reliquified_ars_nouveau.items.base.loot.LootEntries;
import it.hurts.sskirillss.relics.init.DataComponentRegistry;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.*;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemColor;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemShape;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.BeamsData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleData;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import top.theillusivec4.curios.api.SlotContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BallistarianBracerItem extends NouveauRelicItem {
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("striker")
                                .stat(StatData.builder("damage")
                                        .initialValue(2D, 5D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.1D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatData.builder("count")
                                        .initialValue(2D, 4D)
                                        .upgradeModifier(UpgradeOperation.ADD, 0.3D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(StatData.builder("chance")
                                        .initialValue(0.5D, 0.3D)
                                        .upgradeModifier(UpgradeOperation.ADD, -0.025D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingData.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .sources(LevelingSourcesData.builder()
                                .source(LevelingSourceData.abilityBuilder("striker")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.ORANGE)
                                        .build())
                                .build())
                        .build())
                .style(StyleData.builder()
//                        .tooltip(TooltipData.builder()
//                                .borderTop(0xffdda524)
//                                .borderBottom(0xffdda524)
//                                .textured(true)
//                                .build())
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
        if (!(slotContext.entity() instanceof Player player) || player.getCommandSenderWorld().isClientSide())
            return;

        var level = (ServerLevel) player.getCommandSenderWorld();
        var entities = new ArrayList<>(getEntities(stack));
        var index = entities.size();
        var maxCount = (int) Math.round(getStatValue(stack, "striker", "count"));

        if (entities.size() < maxCount)
            if (getCooldown(stack) == 0) {
                var normalizedLookAngle = player.getLookAngle().normalize();
                var bow = new BallistarianBowEntity(EntityRegistry.BALLISTARIAN_BOW.value(), level);

                var pair = bow.calculateOffsetAndHeight(index, maxCount, normalizedLookAngle);
                var offset = pair.getLeft();
                var spawnPos = player.position().add(offset.x, player.getEyeY() - player.getY() + pair.getRight(), offset.z);

                bow.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
                bow.setOwnerUUID(player.getUUID());
                bow.rotatedBowAngle(normalizedLookAngle, maxCount, index);

                level.addFreshEntity(bow);

                addEntities(stack, bow.getUUID());
            } else {
                addCooldown(stack, -1);
            }

        if (!entities.isEmpty())
            for (int i = entities.size() - 1; i >= 0; i--) {
                var bow = level.getEntity(entities.get(i));

                if (bow == null) {
                    entities.remove(i);
                    setEntities(stack, entities);
                }
            }
    }

    public void addEntities(ItemStack stack, UUID uuid) {
        var array = new ArrayList<>(getEntities(stack));

        array.add(uuid);

        setEntities(stack, array);
    }

    public void addCooldown(ItemStack stack, int time) {
        setCooldown(stack, getCooldown(stack) + time);
    }

    public void setCooldown(ItemStack stack, int cooldown) {
        stack.set(DataComponentRegistry.COOLDOWN, Math.max(cooldown, 0));
    }

    public int getCooldown(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.COOLDOWN, 0);
    }

    public void setEntities(ItemStack stack, List<UUID> list) {
        stack.set(RANDataComponentRegistry.WOLVES, list);
    }

    public List<UUID> getEntities(ItemStack stack) {
        return stack.getOrDefault(RANDataComponentRegistry.WOLVES, new ArrayList<>());
    }

    @EventBusSubscriber
    public static class BallistarianBracerEvent {
        @SubscribeEvent
        public static void onProjectileImpactEvent(ProjectileImpactEvent event) {
            if (event.getRayTraceResult() instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof BallistarianBowEntity)
                event.setCanceled(true);
        }
    }
}
