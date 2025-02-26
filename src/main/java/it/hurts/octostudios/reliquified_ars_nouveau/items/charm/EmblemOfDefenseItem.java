package it.hurts.octostudios.reliquified_ars_nouveau.items.charm;

import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.ScribbleRelicItem;
import it.hurts.octostudios.reliquified_ars_nouveau.items.base.loot.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.*;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemColor;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemShape;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import top.theillusivec4.curios.api.SlotContext;

public class EmblemOfDefenseItem extends ScribbleRelicItem {
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("repulse")
                                .stat(StatData.builder("cooldown")
                                        .initialValue(30D, 20D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, -0.035D)
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
    }

    @EventBusSubscriber
    public static class EmblemOfDefenseEvent {
        @SubscribeEvent
        public static void onAttacked(LivingDamageEvent.Post event) {
            if (!(event.getEntity() instanceof Player player) || player.getCommandSenderWorld().isClientSide()
                    || !(event.getSource().getEntity() instanceof LivingEntity source) || source.getUUID().equals(player.getUUID()))
                return;

            var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.EMBLEM_OF_DEFENSE.value());

            if (!(stack.getItem() instanceof EmblemOfDefenseItem relic))
                return;

            relic.onAutoCastedSpell(player, source, stack);
        }
    }
}
