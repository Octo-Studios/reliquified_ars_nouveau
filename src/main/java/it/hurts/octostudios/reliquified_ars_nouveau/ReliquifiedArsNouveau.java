package it.hurts.octostudios.reliquified_ars_nouveau;

import com.hollingsworth.arsnouveau.common.entity.EntityOrbitProjectile;
import it.hurts.octostudios.reliquified_ars_nouveau.init.EntityRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.charm.EmblemOfDevotionItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(ReliquifiedArsNouveau.MODID)
public class ReliquifiedArsNouveau {
    public static final String MODID = "reliquified_ars_nouveau";

    public ReliquifiedArsNouveau(IEventBus bus) {
        bus.addListener(this::setupCommon);

        ItemRegistry.register(bus);
        EntityRegistry.register(bus);
        RANDataComponentRegistry.register(bus);
    }

    private void setupCommon(final FMLCommonSetupEvent event) {

    }
}