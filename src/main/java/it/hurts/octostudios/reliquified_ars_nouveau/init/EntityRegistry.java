package it.hurts.octostudios.reliquified_ars_nouveau.init;

import it.hurts.octostudios.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.octostudios.reliquified_ars_nouveau.entities.WhirlingBroomEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EntityRegistry {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, ReliquifiedArsNouveau.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<WhirlingBroomEntity>> WHIRLING_BROOM = ENTITIES.register("broom", () ->
            EntityType.Builder.of(WhirlingBroomEntity::new, MobCategory.MISC).sized(1F, 1F).build("broom"));

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }
}