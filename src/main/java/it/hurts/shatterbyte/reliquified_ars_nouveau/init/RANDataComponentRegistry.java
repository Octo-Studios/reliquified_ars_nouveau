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
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> SPIKED_CLOAK_RESISTANCE_UNTIL = DATA_COMPONENTS.register("spiked_cloak_resistance_until",
            () -> DataComponentType.<Long>builder().persistent(Codec.LONG).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> QUANTUM_BUBBLE_CHARGES = DATA_COMPONENTS.register("quantum_bubble_charges",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> QUANTUM_BUBBLE_REGEN_TICKS = DATA_COMPONENTS.register("quantum_bubble_regen_ticks",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Double>> MANA_RING_DEBT = DATA_COMPONENTS.register("mana_ring_debt",
            () -> DataComponentType.<Double>builder().persistent(Codec.DOUBLE).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<java.util.List<Double>>> RING_OF_THRIFT_REFUND_AMOUNTS = DATA_COMPONENTS.register("ring_of_thrift_refund_amounts",
            () -> DataComponentType.<java.util.List<Double>>builder().persistent(Codec.DOUBLE.listOf()).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<java.util.List<Long>>> RING_OF_THRIFT_REFUND_TIMES = DATA_COMPONENTS.register("ring_of_thrift_refund_times",
            () -> DataComponentType.<java.util.List<Long>>builder().persistent(Codec.LONG.listOf()).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> RING_OF_THRIFT_COMBO_STACKS = DATA_COMPONENTS.register("ring_of_thrift_combo_stacks",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> RING_OF_THRIFT_COMBO_UNTIL = DATA_COMPONENTS.register("ring_of_thrift_combo_until",
            () -> DataComponentType.<Long>builder().persistent(Codec.LONG).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ARCHMAGE_GLOVE_FAILED_MULTICASTS = DATA_COMPONENTS.register("archmage_glove_failed_multicasts",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> ARCHMAGE_GLOVE_MULTICAST_ACTIVE_UNTIL = DATA_COMPONENTS.register("archmage_glove_multicast_active_until",
            () -> DataComponentType.<Long>builder().persistent(Codec.LONG).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> EMBLEM_OF_DEFENSE_RESISTANCE_UNTIL = DATA_COMPONENTS.register("emblem_of_defense_resistance_until",
            () -> DataComponentType.<Long>builder().persistent(Codec.LONG).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> EMBLEM_OF_ASSAULT_DAMAGE_UNTIL = DATA_COMPONENTS.register("emblem_of_assault_damage_until",
            () -> DataComponentType.<Long>builder().persistent(Codec.LONG).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> EMBLEM_OF_DEVOTION_NEXT_SPAWN_TICK = DATA_COMPONENTS.register("emblem_of_devotion_next_spawn_tick",
            () -> DataComponentType.<Long>builder().persistent(Codec.LONG).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> EMBLEM_OF_DEVOTION_STACK_ID = DATA_COMPONENTS.register("emblem_of_devotion_stack_id",
            () -> DataComponentType.<String>builder().persistent(Codec.STRING).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ARCHITECTS_STAFF_CHARGES = DATA_COMPONENTS.register("architects_staff_charges",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ARCHITECTS_STAFF_REGEN_TICKS = DATA_COMPONENTS.register("architects_staff_regen_ticks",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WorldPosition>> ARCHITECTS_STAFF_FIRST_POINT = DATA_COMPONENTS.register("architects_staff_first_point",
            () -> DataComponentType.<WorldPosition>builder().persistent(WorldPosition.CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> ARCHITECTS_STAFF_BRIDGE_ACTIVE_UNTIL = DATA_COMPONENTS.register("architects_staff_bridge_active_until",
            () -> DataComponentType.<Long>builder().persistent(Codec.LONG).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> ARCHITECTS_STAFF_DISAPPEAR_LOCK_UNTIL = DATA_COMPONENTS.register("architects_staff_disappear_lock_until",
            () -> DataComponentType.<Long>builder().persistent(Codec.LONG).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> RING_OF_LAST_WILL_REVENGE_TARGET = DATA_COMPONENTS.register("ring_of_last_will_revenge_target",
            () -> DataComponentType.<String>builder().persistent(Codec.STRING).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> RING_OF_LAST_WILL_REWIND_UNTIL = DATA_COMPONENTS.register("ring_of_last_will_rewind_until",
            () -> DataComponentType.<Long>builder().persistent(Codec.LONG).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> RING_OF_LAST_WILL_COOLDOWN_UNTIL = DATA_COMPONENTS.register("ring_of_last_will_cooldown_until",
            () -> DataComponentType.<Long>builder().persistent(Codec.LONG).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> RING_OF_LAST_WILL_LAST_ELECTRICITY_ID = DATA_COMPONENTS.register("ring_of_last_will_last_electricity_id",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> RING_OF_LAST_WILL_ILLUSION_ID = DATA_COMPONENTS.register("ring_of_last_will_illusion_id",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> WING_WILD_STALKER_LAST_ELECTRICITY_ID = DATA_COMPONENTS.register("wing_wild_stalker_last_electricity_id",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> WING_WILD_STALKER_FLIGHT_TICKS = DATA_COMPONENTS.register("wing_wild_stalker_flight_ticks",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> WING_WILD_STALKER_DASH_COOLDOWN_UNTIL = DATA_COMPONENTS.register("wing_wild_stalker_dash_cooldown_until",
            () -> DataComponentType.<Long>builder().persistent(Codec.LONG).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> WHIRLING_BROOM_LAST_ELECTRICITY_ID = DATA_COMPONENTS.register("whirling_broom_last_electricity_id",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> WHIRLING_BROOM_COOLDOWN_UNTIL = DATA_COMPONENTS.register("whirling_broom_cooldown_until",
            () -> DataComponentType.<Long>builder().persistent(Codec.LONG).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Float>> WHIRLING_BROOM_CURRENT_HEALTH = DATA_COMPONENTS.register("whirling_broom_current_health",
            () -> DataComponentType.<Float>builder().persistent(Codec.FLOAT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> WHIRLING_BROOM_HEALTH_REGEN_TICKS = DATA_COMPONENTS.register("whirling_broom_health_regen_ticks",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> WHIRLING_BROOM_STACK_ID = DATA_COMPONENTS.register("whirling_broom_stack_id",
            () -> DataComponentType.<String>builder().persistent(Codec.STRING).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> WHIRLISPRIG_PETALS_CHARGES = DATA_COMPONENTS.register("whirlisprig_petals_charges",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> WHIRLISPRIG_PETALS_REGEN_TICKS = DATA_COMPONENTS.register("whirlisprig_petals_regen_ticks",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> WHIRLISPRIG_PETALS_LIFT_TICKS = DATA_COMPONENTS.register("whirlisprig_petals_lift_ticks",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> WHIRLISPRIG_PETALS_RANGED_BONUS_UNTIL = DATA_COMPONENTS.register("whirlisprig_petals_ranged_bonus_until",
            () -> DataComponentType.<Long>builder().persistent(Codec.LONG).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> WHIRLISPRIG_PETALS_ACTIVE = DATA_COMPONENTS.register("whirlisprig_petals_active",
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> WHIRLISPRIG_PETALS_BONUS_STACKS = DATA_COMPONENTS.register("whirlisprig_petals_bonus_stacks",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<java.util.List<String>>> HORN_OF_THE_WILD_HUNTER_WOLVES = DATA_COMPONENTS.register("horn_of_the_wild_hunter_wolves",
            () -> DataComponentType.<java.util.List<String>>builder().persistent(Codec.STRING.listOf()).build());

    public static void register(IEventBus bus) {
        DATA_COMPONENTS.register(bus);
    }
}
