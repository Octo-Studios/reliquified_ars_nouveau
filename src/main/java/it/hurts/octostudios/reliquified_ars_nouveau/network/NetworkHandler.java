package it.hurts.octostudios.reliquified_ars_nouveau.network;

import it.hurts.octostudios.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.octostudios.reliquified_ars_nouveau.network.packets.PetalsJumpPacket;
import it.hurts.octostudios.reliquified_ars_nouveau.network.packets.WingStartFlyPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class NetworkHandler {
    @SubscribeEvent
    public static void onRegisterPayloadHandler(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(ReliquifiedArsNouveau.MODID).versioned("1.0").optional();

        registrar.playToServer(WingStartFlyPacket.TYPE, WingStartFlyPacket.STREAM_CODEC, WingStartFlyPacket::handle);
        registrar.playToServer(PetalsJumpPacket.TYPE, PetalsJumpPacket.STREAM_CODEC, PetalsJumpPacket::handle);
    }
}