package it.hurts.octostudios.reliquified_ars_nouveau.items;

import it.hurts.octostudios.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;

public abstract class NouveauRelicItem extends RelicItem {
    @Override
    public String getConfigRoute() {
        return ReliquifiedArsNouveau.MODID;
    }
}