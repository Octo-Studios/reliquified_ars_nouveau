package it.hurts.shatterbyte.reliquified_ars_nouveau.init;

import com.mojang.serialization.Codec;
import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.sskirillss.relics.utils.data.WorldPosition;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RANDataComponentRegistry {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ReliquifiedArsNouveau.MODID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WorldPosition>> STAFF_OF_THE_SPECTRAL_WALKER_POSITION = DATA_COMPONENTS.register("staff_of_the_spectral_walker_position",
            () -> DataComponentType.<WorldPosition>builder().persistent(WorldPosition.CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> STAFF_OF_THE_SPECTRAL_WALKER_CHARGES = DATA_COMPONENTS.register("staff_of_the_spectral_walker_charges",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> STAFF_OF_THE_SPECTRAL_WALKER_MOVE_TICKS = DATA_COMPONENTS.register("staff_of_the_spectral_walker_move_ticks",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> STAFF_OF_THE_SPECTRAL_WALKER_REGEN_TICKS = DATA_COMPONENTS.register("staff_of_the_spectral_walker_regen_ticks",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> STAFF_OF_THE_SPECTRAL_WALKER_WAS_IN_SPECTRAL = DATA_COMPONENTS.register("staff_of_the_spectral_walker_was_in_spectral",
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> STAFF_OF_THE_SPECTRAL_WALKER_FALL_IMMUNITY_TICKS = DATA_COMPONENTS.register("staff_of_the_spectral_walker_fall_immunity_ticks",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> CLOAK_OF_CONCEALMENT_LAST_TRIGGER_TICK = DATA_COMPONENTS.register("cloak_of_concealment_last_trigger_tick",
            () -> DataComponentType.<Long>builder().persistent(Codec.LONG).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> QUANTUM_BUBBLE_CHARGES = DATA_COMPONENTS.register("quantum_bubble_charges",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> QUANTUM_BUBBLE_REGEN_TICKS = DATA_COMPONENTS.register("quantum_bubble_regen_ticks",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ARCHMAGE_GLOVE_FAILED_MULTICASTS = DATA_COMPONENTS.register("archmage_glove_failed_multicasts",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> ARCHMAGE_GLOVE_MULTICAST_ACTIVE_UNTIL = DATA_COMPONENTS.register("archmage_glove_multicast_active_until",
            () -> DataComponentType.<Long>builder().persistent(Codec.LONG).build());

    public static void register(IEventBus bus) {
        DATA_COMPONENTS.register(bus);
    }
}
