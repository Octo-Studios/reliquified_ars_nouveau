package it.hurts.octostudios.reliquified_ars_nouveau.items.base.loot;

import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootEntry;

public class LootEntries {
    public static final LootEntry ARS_NOUVEAU_BIOME = LootEntry.builder()
            .dimension(".*")
            .biome("[\\w]+:.*(archwood|flashing|cascading|flourishing|blazing)[\\w_\\/]*")
            .table("[\\w]+:chests\\/[\\w_\\/]*[\\w]+[\\w_\\/]*")
            .weight(1000)
            .build();

    public static final LootEntry ARS_NOUVEAU_STRUCTURES_LIKE = LootEntry.builder()
            .dimension(".*")
            .biome(".*")
            .table("[\\w]+:chests\\/[\\w_\\/]*(arcane_library|ruined_portal)[\\w_\\/]*")
            .weight(1000)
            .build();
}
