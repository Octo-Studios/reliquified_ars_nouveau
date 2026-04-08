package it.hurts.shatterbyte.reliquified_ars_nouveau.items.base;

import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public abstract class RANRelicItem extends RelicItem {
    public RANRelicItem(Item.Properties properties) {
        super(properties);
    }

    public RANRelicItem() {
        super(new Item.Properties()
                .rarity(Rarity.EPIC)
                .stacksTo(1));
    }

    @Override
    public String getConfigRoute() {
        return ReliquifiedArsNouveau.MODID;
    }
}