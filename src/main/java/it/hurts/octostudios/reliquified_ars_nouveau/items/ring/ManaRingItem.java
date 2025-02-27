package it.hurts.octostudios.reliquified_ars_nouveau.items.ring;

import com.hollingsworth.arsnouveau.api.perk.PerkAttributes;
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
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.world.item.ItemStack;

public class ManaRingItem extends NouveauRelicItem {
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("empower")
                                .stat(StatData.builder("capacity")
                                        .initialValue(0.3D, 0.5D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.2)
                                        .formatValue(value -> MathUtils.round(value * 100, 1))
                                        .build())
                                .stat(StatData.builder("regeneration")
                                        .initialValue(0.5D, 0.7D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.1)
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
                                .startColor(0xFF13d3e5)
                                .endColor(0x007384b4)
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
                .attribute(new RelicAttributeModifier.Modifier(PerkAttributes.MAX_MANA, (float) getStatValue(stack, "empower", "capacity")))
                .attribute(new RelicAttributeModifier.Modifier(PerkAttributes.MANA_REGEN_BONUS, (float) getStatValue(stack, "empower", "regeneration")))
                .build();
    }
}
