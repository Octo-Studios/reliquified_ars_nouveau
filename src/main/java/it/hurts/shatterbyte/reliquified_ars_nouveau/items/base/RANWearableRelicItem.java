package it.hurts.shatterbyte.reliquified_ars_nouveau.items.base;

import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.sskirillss.relics.items.relics.base.WearableRelicItem;

public abstract class RANWearableRelicItem extends WearableRelicItem {
    @Override
    public String getConfigRoute() {
        return ReliquifiedArsNouveau.MODID;
    }
}