package it.hurts.octostudios.reliquified_ars_nouveau.items.body;

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
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

import java.awt.*;

public class WingWildStalkerItem extends NouveauRelicItem {
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("wings")
                                .stat(StatData.builder("time")
                                        .initialValue(6D, 8D)
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
                                .source(LevelingSourceData.abilityBuilder("wings")
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
        if (!(slotContext.entity() instanceof Player player))
            return;

        var isFlying = getToggled(stack);

        if (!player.getCommandSenderWorld().isClientSide()) {
            if (isFlying && getTime(stack) <= 0) {
                player.fallDistance = 0;

                if (getTime(stack) <= 0)
                    player.stopFallFlying();

            } else {
                if (isFlying) {
                    if (player.tickCount % 20 == 0)
                        consumeTime(stack, 1);

                    if (getTime(stack) <= getStatValue(stack, "wings", "time")) {
                        var random = player.getRandom();

                        ((ServerLevel) player.getCommandSenderWorld()).sendParticles(ParticleUtils.constructSimpleSpark(new Color(150 + random.nextInt(100), 150 + random.nextInt(100), 150 + random.nextInt(100)), 0.2F, 10, 0.85F),
                                player.getX(), player.getY(), player.getZ(), 10, 0.1, 0.1, 0.1, 0.1);
                    }
                }
            }

            if (player.onGround() || player.isInLiquid()) {
                setTime(stack, 0);
                setToggled(stack, false);
            }
        } else {
            if (isFlying && getTime(stack) <= 0)
                player.setDeltaMovement(player.getDeltaMovement().x, -0.4, player.getKnownMovement().z);
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player) || newStack.getItem() == stack.getItem()
                || !player.isFallFlying())
            return;

        player.stopFallFlying();
    }

    public int getTime(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.TIME, 0);
    }

    public void setTime(ItemStack stack, int time) {
        stack.set(DataComponentRegistry.TIME, Math.max(time, 0));
    }

    public void consumeTime(ItemStack stack, int time) {
        setTime(stack, getTime(stack) - time);
    }

    public boolean getToggled(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.TOGGLED, true);
    }

    public void setToggled(ItemStack stack, boolean value) {
        stack.set(DataComponentRegistry.TOGGLED, value);
    }
}
