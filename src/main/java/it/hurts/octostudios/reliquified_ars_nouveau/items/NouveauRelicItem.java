package it.hurts.octostudios.reliquified_ars_nouveau.items;

import it.hurts.octostudios.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class NouveauRelicItem extends RelicItem {
    @Override
    public String getConfigRoute() {
        return ReliquifiedArsNouveau.MODID;
    }
}