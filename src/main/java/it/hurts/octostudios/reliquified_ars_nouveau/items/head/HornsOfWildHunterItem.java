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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import top.theillusivec4.curios.api.SlotContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HornsOfWildHunterItem extends NouveauRelicItem {
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("summoner")
                                .stat(StatData.builder("damage")
                                        .initialValue(2D, 3D)
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
        if (!(slotContext.entity() instanceof Player player) || player.getCommandSenderWorld().isClientSide()
                || getWolves(stack).isEmpty())
            return;

        for (UUID wolfUUID : getWolves(stack)) {
            var wolf = ((ServerLevel) player.getCommandSenderWorld()).getEntity(wolfUUID);

            if (wolf == null)
                continue;

            if (player.distanceTo(wolf) > 32)
                wolf.teleportTo(player.getX(), player.getY(), player.getZ());
        }
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

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player) || player.getCommandSenderWorld().isClientSide()
                || prevStack.getItem() == stack.getItem())
            return;

        createWolves(player, stack, 2);
    }

    public void createWolves(Player player, ItemStack stack, int count) {
        var wolves = new ArrayList<UUID>();
        var level = player.getCommandSenderWorld();

        var oldWolfList = new ArrayList<>();

        for (UUID wolfUUID : getWolves(stack)) {
            var oldWolf = ((ServerLevel) player.getCommandSenderWorld()).getEntity(wolfUUID);

            if (oldWolf == null)
                continue;

            oldWolfList.add(oldWolf);
        }

        if (!oldWolfList.isEmpty())
            return;

        for (int i = 0; i < count; i++) {
            var wolf = new Wolf(EntityType.WOLF, level);

            wolf.setOwnerUUID(player.getUUID());
            wolf.setPos(player.getPosition(1));
            wolf.setTame(true, true);
            wolf.setAggressive(true);
            wolf.setInvulnerable(true);

            level.addFreshEntity(wolf);

            EntityUtils.applyAttribute(wolf, stack, Attributes.ATTACK_DAMAGE, (float) MathUtils.round(getStatValue(stack, "summoner", "damage"), 0), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

            wolves.add(wolf.getUUID());
        }

        setWolves(stack, wolves);
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
        public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
            var player = event.getEntity();

            if (player.getCommandSenderWorld().isClientSide())
                return;

            var level = (ServerLevel) player.getCommandSenderWorld();
            var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.HORNS_OF_THE_WILD_HUNTER.value());

            if (stacks.isEmpty())
                return;

            for (var stack : stacks) {
                if (!(stack.getItem() instanceof HornsOfWildHunterItem relic) || relic.getWolves(stack).isEmpty())
                    return;

                var oldWolfList = new ArrayList<>();

                for (UUID wolfUUID : relic.getWolves(stack)) {
                    var oldWolf = (Wolf) level.getServer().getLevel(event.getFrom()).getEntity(wolfUUID);

                    if (oldWolf == null)
                        continue;

                    oldWolfList.add(oldWolf);
                    oldWolf.discard();
                }

                relic.createWolves(player, stack, oldWolfList.size());
            }
        }
    }
}
