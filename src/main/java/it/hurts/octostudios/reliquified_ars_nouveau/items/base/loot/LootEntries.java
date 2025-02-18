package it.hurts.octostudios.reliquified_ars_nouveau.items.base.loot;

import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootEntry;

public class LootEntries {
    public static final LootEntry ARS_NOUVEAU = LootEntry.builder()
            .dimension(".*")
            .biome("[\\w]+:.*(archwood|flashing|cascading|flourishing|blazing|esoteric|enigmatic|mysterious|obscur|crypt|occult|secret|arcan|mag|sorcer|wizard|enchantment|witch|spell|necroman|divinat|charm|alchem|ars)[\\w_\\/]*")
            .table("[\\w]+:chests\\/[\\w_\\/]*[\\w]+[\\w_\\/]*")
            .weight(600)
            .build();

    public static final LootEntry ARS_NOUVEAU_LIKE = LootEntry.builder()
            .dimension(".*")
            .biome(".*")
            .table("[\\w]+:chests\\/[\\w_\\/]*(esoteric|enigmatic|mysterious|obscur|crypt|occult|secret|arcan|mag|sorcer|wizard|enchantment|witch|spell|necroman|divinat|charm|alchem|arch|ars)[\\w_\\/]*",
                    "\\b\\w*ars\\w*:chests\\/[\\w_\\/]*[\\w]+[\\w_\\/]*")
            .weight(600)
            .build();
}