package it.hurts.octostudios.reliquified_ars_nouveau;

import it.hurts.octostudios.reliquified_ars_nouveau.init.EntityRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import net.minecraft.world.entity.projectile.Projectile;
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

    public static void aaa(HitResult hitResult) {
        System.out.println(hitResult.getLocation());
    }

    public static void sss(Projectile projectile){
        System.out.println("EE");
    }
}