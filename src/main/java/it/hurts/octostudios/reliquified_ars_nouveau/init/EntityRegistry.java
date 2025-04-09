package it.hurts.octostudios.reliquified_ars_nouveau.init;

import it.hurts.octostudios.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.octostudios.reliquified_ars_nouveau.entities.MagicShellEntity;
import it.hurts.octostudios.reliquified_ars_nouveau.entities.WhirlingBroomEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EntityRegistry {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, ReliquifiedArsNouveau.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<WhirlingBroomEntity>> WHIRLING_BROOM = ENTITIES.register("whirling_broom", () ->
            EntityType.Builder.of(WhirlingBroomEntity::new, MobCategory.MISC).sized(1F, 1F).build("whirling_broom"));

//    public static final DeferredHolder<EntityType<?>, EntityType<BallistarianBowEntity>> BALLISTARIAN_BOW = ENTITIES.register("ballistarian_bow", () ->
//            EntityType.Builder.of(BallistarianBowEntity::new, MobCategory.MISC).sized(0.6F, 1.1F).build("ballistarian_bow"));

    public static final DeferredHolder<EntityType<?>, EntityType<MagicShellEntity>> MAGIC_SHELL = ENTITIES.register("magic_shell", () ->
            EntityType.Builder.of(MagicShellEntity::new, MobCategory.MISC).sized(0.75F, 0.75F).build("magic_shell"));

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }
}