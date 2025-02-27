package it.hurts.octostudios.reliquified_ars_nouveau.items.charm;

import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.ScribbleRelicItem;
import it.hurts.octostudios.reliquified_ars_nouveau.items.base.loot.LootEntries;
import it.hurts.sskirillss.relics.init.DataComponentRegistry;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.*;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemColor;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemShape;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import top.theillusivec4.curios.api.SlotContext;

import java.awt.*;

public class EmblemOfDefenseItem extends ScribbleRelicItem {
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("repulse")
                                .stat(StatData.builder("cooldown")
                                        .initialValue(20D, 15D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, -0.04D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingData.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .sources(LevelingSourcesData.builder()
                                .source(LevelingSourceData.abilityBuilder("repulse")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.CYAN)
                                        .build())
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
                || getTime(stack) == 0)
            return;

        var random = player.getRandom();
        var level = (ServerLevel) player.getCommandSenderWorld();

        consumeTime(stack, 1);

        if (getTime(stack) == 0) {
            for (int i = 0; i < 100; i++) {
                double angle = 2 * Math.PI * i / 100;
                double x = player.getX() + 1 * Math.cos(angle);
                double z = player.getZ() + 1 * Math.sin(angle);

                level.playSound(null, player, SoundEvents.ILLUSIONER_PREPARE_MIRROR, SoundSource.PLAYERS, 0.8F, 0.9F + player.getRandom().nextFloat() * 0.2F);

                level.sendParticles(ParticleUtils.constructSimpleSpark(new Color(50 + random.nextInt(100), 0, 150 + random.nextInt(100)), 0.3F, 60, 0.95F),
                        x, player.getY() + player.getBbHeight() / 2, z, 1, 0, 0.1, 0, 0.1);
            }
        }
    }

    public void consumeTime(ItemStack stack, int time) {
        setTime(stack, getTime(stack) - time);
    }

    public void setTime(ItemStack stack, int time) {
        stack.set(DataComponentRegistry.TIME, Math.max(time, 0));
    }

    public int getTime(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.TIME, 0);
    }

    @EventBusSubscriber
    public static class EmblemOfDefenseEvent {
        @SubscribeEvent
        public static void onAttacked(LivingDamageEvent.Post event) {
            if (!(event.getEntity() instanceof Player player) || player.getCommandSenderWorld().isClientSide()
                    || !(event.getSource().getEntity() instanceof LivingEntity source) || source.getUUID().equals(player.getUUID()))
                return;

            var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.EMBLEM_OF_DEFENSE.value());

            if (!(stack.getItem() instanceof EmblemOfDefenseItem relic) || relic.getTime(stack) != 0)
                return;

            relic.setTime(stack, (int) (relic.getStatValue(stack, "repulse", "cooldown") * 20));
            relic.onAutoCastedSpell(player, source, stack, new Color(100, 0, 255));
        }
    }
}
