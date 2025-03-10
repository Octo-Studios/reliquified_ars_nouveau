package it.hurts.octostudios.reliquified_ars_nouveau.init;

import com.hollingsworth.arsnouveau.api.spell.SpellCaster;
import com.mojang.serialization.Codec;
import it.hurts.octostudios.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.octostudios.reliquified_ars_nouveau.items.hands.MulticastedComponent;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class RANDataComponentRegistry {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ReliquifiedArsNouveau.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<MulticastedComponent>>> MULTICASTED = DATA_COMPONENTS.register("multicasted",
            () -> DataComponentType.<List<MulticastedComponent>>builder()
                    .persistent(Codec.list(MulticastedComponent.CODEC))
                    .build()
    );

    public static void register(IEventBus bus) {
        DATA_COMPONENTS.register(bus);
    }
}
