package it.hurts.octostudios.reliquified_ars_nouveau.entities;

import it.hurts.octostudios.reliquified_ars_nouveau.init.EntityRegistry;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class RegistryAttributesHandler {
    @SubscribeEvent
    public static void onRegisterAttributes(EntityAttributeCreationEvent event) {
        event.put(EntityRegistry.WHIRLING_BROOM.get(), Mob.createMobAttributes().add(Attributes.STEP_HEIGHT, 5F).add(Attributes.MAX_HEALTH, 20F).build());
    }
}
