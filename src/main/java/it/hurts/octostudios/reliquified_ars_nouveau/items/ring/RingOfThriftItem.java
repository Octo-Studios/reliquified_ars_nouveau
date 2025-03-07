package it.hurts.octostudios.reliquified_ars_nouveau.items.ring;

import com.hollingsworth.arsnouveau.api.event.SpellCostCalcEvent;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.capability.ManaCap;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.awt.*;

public class RingOfThriftItem extends NouveauRelicItem {
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("thrift")
                                .stat(StatData.builder("chance")
                                        .initialValue(0.05D, 0.15D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.235)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingData.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .sources(LevelingSourcesData.builder()
                                .source(LevelingSourceData.abilityBuilder("thrift")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.BLUE)
                                        .build())
                                .build())
                        .build())
                .style(StyleData.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xff2d2d58)
                                .borderBottom(0xff2d2d58)
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

    @EventBusSubscriber
    public static class ScribbleRelicEvent {
        @SubscribeEvent
        public static void onCostMana(SpellCostCalcEvent event) {
            if (!(event.context.getCaster() instanceof LivingCaster livingEntity))
                return;

            var entity = livingEntity.livingEntity;
            var level = entity.getCommandSenderWorld();
            var random = level.getRandom();

            var stack = EntityUtils.findEquippedCurio(entity, ItemRegistry.RING_OF_THRIFT.value());

            if (level.isClientSide() || !(stack.getItem() instanceof RingOfThriftItem relic) || !relic.isAbilityUnlocked(stack, "thrift")
                    || event.currentCost > new ManaCap(entity).getCurrentMana()
                    || random.nextDouble() > relic.getStatValue(stack, "thrift", "chance"))
                return;

            relic.spreadRelicExperience(entity, stack, 1);

            event.currentCost = 0;

            level.playSound(null, entity, SoundEvents.ALLAY_THROW, SoundSource.PLAYERS, 1F, 0.9F + random.nextFloat() * 0.2F);

            ((ServerLevel) level).sendParticles(ParticleUtils.constructSimpleSpark(new Color(100 + random.nextInt(156), random.nextInt(100 + random.nextInt(156)), random.nextInt(100 + random.nextInt(156))), 0.3F, 60, 0.95F),
                    entity.getX(), entity.getY() + 0.4, entity.getZ(), 30, 0.1, 0.1, 0.1, 0.1);
        }
    }
}
