package it.hurts.shatterbyte.reliquified_ars_nouveau.network;

import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.shatterbyte.reliquified_ars_nouveau.network.packets.S2CCloakAbsorptionSpherePacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class NetworkHandler {
    @SubscribeEvent
    public static void onRegisterPayloadHandler(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(ReliquifiedArsNouveau.MODID).versioned("1.0").optional();

        registrar.playToClient(S2CCloakAbsorptionSpherePacket.TYPE, S2CCloakAbsorptionSpherePacket.STREAM_CODEC, S2CCloakAbsorptionSpherePacket::handle);
    }

    public static <MSG extends CustomPacketPayload> void sendToClientsTrackingEntityAndSelf(MSG message, Entity entity) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, message);
    }
}
