package it.hurts.shatterbyte.reliquified_ars_nouveau.init;

import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.shatterbyte.reliquified_ars_nouveau.entities.BallistarianPhantomProjectileEntity;
import it.hurts.shatterbyte.reliquified_ars_nouveau.entities.WhirlingBroomEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EntityRegistry {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, ReliquifiedArsNouveau.MODID);
    public static final DeferredHolder<EntityType<?>, EntityType<WhirlingBroomEntity>> WHIRLING_BROOM = ENTITIES.register("whirling_broom",
            () -> EntityType.Builder.of(WhirlingBroomEntity::new, MobCategory.MISC)
                    .sized(1.1F, 0.6F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("whirling_broom"));
    public static final DeferredHolder<EntityType<?>, EntityType<BallistarianPhantomProjectileEntity>> BALLISTARIAN_PHANTOM_PROJECTILE =
            ENTITIES.register("ballistarian_phantom_projectile",
                    () -> EntityType.Builder.of(BallistarianPhantomProjectileEntity::new, MobCategory.MISC)
                            .sized(0.1F, 0.1F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("ballistarian_phantom_projectile"));

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }

    @EventBusSubscriber(modid = ReliquifiedArsNouveau.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void onRegisterAttributes(EntityAttributeCreationEvent event) {
            event.put(WHIRLING_BROOM.get(), Mob.createMobAttributes()
                    .add(Attributes.MAX_HEALTH, 2D)
                    .build());
        }
    }
}
