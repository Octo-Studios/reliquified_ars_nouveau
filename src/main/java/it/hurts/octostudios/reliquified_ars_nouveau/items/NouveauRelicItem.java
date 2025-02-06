package it.hurts.octostudios.reliquified_ars_nouveau.items;

import it.hurts.octostudios.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.sskirillss.relics.init.CreativeTabRegistry;
import it.hurts.sskirillss.relics.items.ItemBase;
import it.hurts.sskirillss.relics.items.misc.CreativeContentConstructor;
import it.hurts.sskirillss.relics.items.misc.ICreativeTabContent;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public abstract class NouveauRelicItem extends ItemBase implements ICurioItem, IRelicItem, ICreativeTabContent {
    public NouveauRelicItem() {
        super(new Item.Properties()
                .rarity(Rarity.RARE)
                .stacksTo(1));
    }

    @Override
    public String getConfigRoute() {
        return ReliquifiedArsNouveau.MODID;
    }

    @Override
    public void gatherCreativeTabContent(CreativeContentConstructor constructor) {
        constructor.entry(CreativeTabRegistry.RELICS_TAB.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS, this);
    }
}