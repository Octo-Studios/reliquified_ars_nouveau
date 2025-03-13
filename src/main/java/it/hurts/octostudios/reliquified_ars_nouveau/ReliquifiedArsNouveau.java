package it.hurts.octostudios.reliquified_ars_nouveau;

import com.hollingsworth.arsnouveau.common.entity.EntityChimeraProjectile;
import it.hurts.octostudios.reliquified_ars_nouveau.init.EntityRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mod(ReliquifiedArsNouveau.MODID)
public class ReliquifiedArsNouveau {
    public static final String MODID = "reliquified_ars_nouveau";

    public ReliquifiedArsNouveau(IEventBus bus) {
        bus.addListener(this::setupCommon);

        ItemRegistry.register(bus);
        EntityRegistry.register(bus);
        RANDataComponentRegistry.register(bus);
    }

    public static void rrr(EntityChimeraProjectile spike) {

        Vec3 motion = spike.getDeltaMovement();

        //spike.setDeltaMovement(motion.x, -0.6, motion.z);
    }

    private void setupCommon(final FMLCommonSetupEvent event) {

    }
}