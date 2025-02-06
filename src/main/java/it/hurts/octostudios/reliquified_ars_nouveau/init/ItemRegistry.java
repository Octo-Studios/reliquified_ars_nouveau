package it.hurts.octostudios.reliquified_ars_nouveau.init;

import it.hurts.octostudios.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.octostudios.reliquified_ars_nouveau.items.back.CloakOfConcealmentItem;
import it.hurts.octostudios.reliquified_ars_nouveau.items.NouveauRelicItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, ReliquifiedArsNouveau.MODID);

    public static final DeferredHolder<Item, NouveauRelicItem> CLOAK_OF_CONCEALMENT = ITEMS.register("cloak_of_concealment", CloakOfConcealmentItem::new);

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}