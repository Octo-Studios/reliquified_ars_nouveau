package it.hurts.shatterbyte.reliquified_ars_nouveau;

import com.hollingsworth.arsnouveau.api.spell.AbstractAugment;
import com.hollingsworth.arsnouveau.api.registry.GlyphRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.EntityRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.spell.augment.AugmentMulticast;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(ReliquifiedArsNouveau.MODID)
public class ReliquifiedArsNouveau {
    public static final String MODID = "reliquified_ars_nouveau";

    public ReliquifiedArsNouveau(IEventBus bus) {
        bus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) -> event.enqueueWork(() -> GlyphRegistry.getSpellpartMap().values().forEach(spellPart -> {
            if (!(spellPart instanceof AbstractAugment))
                spellPart.compatibleAugments.add(AugmentMulticast.INSTANCE);
        })));

        ItemRegistry.register(bus);
        EntityRegistry.register(bus);
        RANDataComponentRegistry.register(bus);

        GlyphRegistry.registerSpell(AugmentMulticast.INSTANCE);
    }
}
