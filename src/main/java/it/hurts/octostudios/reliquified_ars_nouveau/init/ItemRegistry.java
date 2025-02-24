package it.hurts.octostudios.reliquified_ars_nouveau.init;

import it.hurts.octostudios.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.octostudios.reliquified_ars_nouveau.items.NouveauRelicItem;
import it.hurts.octostudios.reliquified_ars_nouveau.items.back.CloakOfConcealmentItem;
import it.hurts.octostudios.reliquified_ars_nouveau.items.bracelet.FlamingBracerItem;
import it.hurts.octostudios.reliquified_ars_nouveau.items.charm.EmblemOfDefenseItem;
import it.hurts.octostudios.reliquified_ars_nouveau.items.charm.QuantumBubbleItem;
import it.hurts.octostudios.reliquified_ars_nouveau.items.ring.RingOfTheSpectralWalker;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, ReliquifiedArsNouveau.MODID);

    public static final DeferredHolder<Item, NouveauRelicItem> CLOAK_OF_CONCEALMENT = ITEMS.register("cloak_of_concealment", CloakOfConcealmentItem::new);
    public static final DeferredHolder<Item, NouveauRelicItem> FLAMING_BRACER = ITEMS.register("flaming_bracer", FlamingBracerItem::new);
    public static final DeferredHolder<Item, NouveauRelicItem> RING_OF_THE_SPECTRAL_WALKER = ITEMS.register("ring_of_the_spectral_walker", RingOfTheSpectralWalker::new);
    public static final DeferredHolder<Item, NouveauRelicItem> QUANTUM_BUBBLE = ITEMS.register("quantum_bubble", QuantumBubbleItem::new);

    public static final DeferredHolder<Item, NouveauRelicItem> EMBLEM_OF_DEFENSE = ITEMS.register("emblem_of_defense", EmblemOfDefenseItem::new);

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}