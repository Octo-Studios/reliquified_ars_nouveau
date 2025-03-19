package it.hurts.octostudios.reliquified_ars_nouveau.items.body;

import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.NouveauRelicItem;
import it.hurts.octostudios.reliquified_ars_nouveau.items.base.loot.LootEntries;
import it.hurts.octostudios.reliquified_ars_nouveau.network.packets.WingStartFlyPacket;
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
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import top.theillusivec4.curios.api.SlotContext;

import java.awt.*;

public class WingWildStalkerItem extends NouveauRelicItem {
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("wings")
                                .stat(StatData.builder("strength")
                                        .initialValue(2D, 4D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.1)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(StatData.builder("charges")
                                        .initialValue(2D, 3D)
                                        .upgradeModifier(UpgradeOperation.ADD, 0.65)
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

        var stackFirst = EntityUtils.findEquippedCurios(player, ItemRegistry.WING_OF_TH_WILD_STALKER.value()).getFirst();

        if (stackFirst.getItem() != stack.getItem())
            return;

        var level = player.getCommandSenderWorld();

        if (level.isClientSide()) {
            if (!WingWildStalkerClientEvent.onDoubleJump)
                return;

            WingWildStalkerClientEvent.ticKCount++;

            if (WingWildStalkerClientEvent.ticKCount % 10 == 0) {
                WingWildStalkerClientEvent.onDoubleJump = false;
                WingWildStalkerClientEvent.ticKCount = 0;
            }
        } else {
            if (player.getKnownMovement().length() >= 2 && player.isFallFlying()) {
                var random = player.getRandom();
                var width = player.getBbWidth() / 2.0;
                var height = player.getBbHeight();

                for (int i = 0; i < 30; i++) {
                    var offsetX = (random.nextDouble() - 0.5) * width * 2;
                    var offsetY = random.nextDouble() * height;
                    var offsetZ = (random.nextDouble() - 0.5) * width * 2;

                    ((ServerLevel) level).sendParticles(ParticleUtils.constructSimpleSpark(new Color(150 + random.nextInt(106), random.nextInt(50), random.nextInt(50), random.nextInt(100 + random.nextInt(156))), 0.3F, 20, 0.95F),
                            player.getX() + offsetX, player.getY() + offsetY, player.getZ() + offsetZ, 1, 0.1, 0.1, 0.1, 0.1);
                }
            }
        }
    }

    public int getActualStatValue(ItemStack stack, String stat) {
        return (int) MathUtils.round(getStatValue(stack, "wings", stat), 0);
    }

    public int getCharge(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.CHARGE, 0);
    }

    public void setCharge(ItemStack stack, int charge) {
        stack.set(DataComponentRegistry.CHARGE, Math.max(charge, 0));
    }

    public void consumeCharge(ItemStack stack, int charge) {
        setCharge(stack, getCharge(stack) - charge);
    }

    @EventBusSubscriber(Dist.CLIENT)
    public static class WingWildStalkerClientEvent {
        private static boolean onDoubleJump = false;
        private static int ticKCount;

        @SubscribeEvent
        public static void onClientTick(InputEvent.Key event) {
            var minecraft = Minecraft.getInstance();
            var player = minecraft.player;

            if (player == null || minecraft.screen != null || event.getAction() != 1 || event.getKey() != minecraft.options.keyJump.getKey().getValue() ||
                    player.isInLiquid() || EntityUtils.findEquippedCurios(player, ItemRegistry.WING_OF_TH_WILD_STALKER.value()).isEmpty()
                    || player.mayFly())
                return;

            var stackFirst = EntityUtils.findEquippedCurios(player, ItemRegistry.WING_OF_TH_WILD_STALKER.value()).getFirst();

            if (!(stackFirst.getItem() instanceof WingWildStalkerItem relic) || !relic.isAbilityUnlocked(stackFirst, "wings"))
                return;

            if (!onDoubleJump)
                onDoubleJump = true;
            else {
                var deltaMovement = player.getDeltaMovement();

                if (!player.isFallFlying()) {
                    NetworkHandler.sendToServer(new WingStartFlyPacket(true));

                    player.setDeltaMovement(deltaMovement.add(player.getLookAngle()).add(0, 0.6, 0));
                } else if (relic.getCharge(stackFirst) > 0) {
                    player.setDeltaMovement(player.getDeltaMovement().add(player.getLookAngle().normalize().scale(relic.getStatValue(stackFirst, "wings", "strength") * 0.2F)));

                    NetworkHandler.sendToServer(new WingStartFlyPacket(false));

                    player.playSound(SoundEvents.ENDER_DRAGON_FLAP, 1.0F, 0.9F + player.getRandom().nextFloat() * 0.2F);
                }
            }
        }
    }
}
