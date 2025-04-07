package it.hurts.octostudios.reliquified_ars_nouveau.items.bracelet;

import it.hurts.octostudios.reliquified_ars_nouveau.entities.BallistarianBowEntity;
import it.hurts.octostudios.reliquified_ars_nouveau.init.EntityRegistry;
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
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
        if (!(slotContext.entity() instanceof Player player) || player.getCommandSenderWorld().isClientSide()
                || getEntities(stack).size() >= Math.round(getStatValue(stack, "striker", "count")))
            return;

        var level = (ServerLevel) player.getCommandSenderWorld();
        var index = getEntities(stack).size();
        var maxCount = (int) Math.round(getStatValue(stack, "striker", "count"));
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
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player) || prevStack.getItem() == stack.getItem()
                || getEntities(stack).isEmpty())
            return;

        setEntities(stack, new ArrayList<>());
    }

    public void addEntities(ItemStack stack, UUID uuid) {
        var array = new ArrayList<>(getEntities(stack));

        array.add(uuid);

        setEntities(stack, array);
    }

    public void removeEntities(ItemStack stack, UUID uuid) {
        var array = new ArrayList<>(getEntities(stack));

        array.remove(uuid);

        setEntities(stack, array);
    }

    public void setEntities(ItemStack stack, List<UUID> list) {
        stack.set(RANDataComponentRegistry.WOLVES, list);
    }

    public List<UUID> getEntities(ItemStack stack) {
        return stack.getOrDefault(RANDataComponentRegistry.WOLVES, new ArrayList<>());
    }

    //  @EventBusSubscriber
    // public static class BallistarianBracerEvent {
//        @SubscribeEvent
//        public static void onDistanceAttack(LivingDamageEvent.Post event) {
//            if (!(event.getSource().getEntity() instanceof Player player) || !(event.getSource().getDirectEntity() instanceof Projectile)
//                    || player.getCommandSenderWorld().isClientSide())
//                return;
//
//            var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.BALLISTARIAN_BRACER.value());
//            var level = (ServerLevel) player.getCommandSenderWorld();
//
//            if (!(stack.getItem() instanceof BallistarianBracerItem relic))
//                return;
//
//            int count = (int) Math.round(relic.getStatValue(stack, "striker", "count"));
//            double radius = 1.5;
//            Vec3 lookVec = player.getLookAngle().normalize();
//            Vec3 backVec = lookVec.scale(-1);
//            Vec3 rightVec = new Vec3(-lookVec.z, 0, lookVec.x);
//
//            double baseHeight = player.getEyeY();
//
//            for (int i = 0; i < count; i++) {
//                var bow = new BallistarianBowEntity(EntityRegistry.BALLISTARIAN_BOW.value(), level);
//
//                bow.setOwnerUUID(player.getUUID());
//                bow.setTarget(event.getEntity());
//
//                Vec3 offset;
//                double heightOffset;
//
//                if (i == 0) {
//                    offset = backVec.scale(radius);
//                    heightOffset = 0.6;
//                } else {
//                    int side = ((i % 2) == 0) ? 1 : -1;
//                    int indexFromCenter = (i + 1) / 2;
//
//                    offset = backVec.scale(radius * 0.9).add(rightVec.scale(side * indexFromCenter * 0.8));
//
//                    heightOffset = 0.6 - (0.15 * indexFromCenter);
//                }
//
//                Vec3 spawnPos = player.position().add(offset.x, baseHeight - player.getY() + heightOffset, offset.z);
//
//                bow.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
//
//                level.addFreshEntity(bow);
//            }
//
//        }
//}
}
