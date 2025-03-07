package it.hurts.octostudios.reliquified_ars_nouveau.items.ring;

import com.hollingsworth.arsnouveau.api.event.SpellCostCalcEvent;
import com.hollingsworth.arsnouveau.api.perk.PerkAttributes;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.NouveauRelicItem;
import it.hurts.octostudios.reliquified_ars_nouveau.items.base.loot.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicAttributeModifier;
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
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

public class ManaRingItem extends NouveauRelicItem {
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("empower")
                                .stat(StatData.builder("capacity")
                                        .initialValue(50D, 60D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.233D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(StatData.builder("regeneration")
                                        .initialValue(1D, 1.3D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.285D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingData.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .sources(LevelingSourcesData.builder()
                                .source(LevelingSourceData.abilityBuilder("empower")
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

    @Override
    public RelicAttributeModifier getRelicAttributeModifiers(ItemStack stack) {
        if (!isAbilityUnlocked(stack, "empower"))
            return super.getRelicAttributeModifiers(stack);

        return RelicAttributeModifier.builder()
                .attribute(new RelicAttributeModifier.Modifier(PerkAttributes.MAX_MANA, (int) getStatValue(stack, "empower", "capacity"), AttributeModifier.Operation.ADD_VALUE))
                .attribute(new RelicAttributeModifier.Modifier(PerkAttributes.MANA_REGEN_BONUS, (int) getStatValue(stack, "empower", "regeneration"), AttributeModifier.Operation.ADD_VALUE))
                .build();
    }

    @EventBusSubscriber
    public static class ManaRingEvent {
        @SubscribeEvent
        public static void onCostMana(SpellCostCalcEvent event) {
            if (!(event.context.getCaster() instanceof LivingCaster livingEntity))
                return;

            var entity = livingEntity.livingEntity;
            var level = entity.getCommandSenderWorld();

            var stack = EntityUtils.findEquippedCurio(entity, ItemRegistry.MANA_RING.value());

            if (level.isClientSide() || !(stack.getItem() instanceof ManaRingItem relic)
                    || !relic.isAbilityUnlocked(stack, "empower"))
                return;

            int cost = event.currentCost;
            relic.spreadRelicExperience(entity, stack, cost >= 20 ? cost / 20 : 0);
        }
    }
}
