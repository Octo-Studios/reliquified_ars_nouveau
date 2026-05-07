package it.hurts.shatterbyte.reliquified_ars_nouveau.network;

import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.shatterbyte.reliquified_ars_nouveau.network.packets.C2SBroomStormElectricityPacket;
import it.hurts.shatterbyte.reliquified_ars_nouveau.network.packets.C2SBroomSprintStatePacket;
import it.hurts.shatterbyte.reliquified_ars_nouveau.network.packets.C2SWingStormElectricityPacket;
import it.hurts.shatterbyte.reliquified_ars_nouveau.network.packets.C2SWingWildStalkerDashPacket;
import it.hurts.shatterbyte.reliquified_ars_nouveau.network.packets.S2CCloakAbsorptionSpherePacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public class NetworkHandler {
    @SubscribeEvent
    public static void onRegisterPayloadHandler(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(ReliquifiedArsNouveau.MODID).versioned("1.0").optional();

        registrar.playToClient(S2CCloakAbsorptionSpherePacket.TYPE, S2CCloakAbsorptionSpherePacket.STREAM_CODEC, S2CCloakAbsorptionSpherePacket::handle);
        registrar.playToServer(C2SWingWildStalkerDashPacket.TYPE, C2SWingWildStalkerDashPacket.STREAM_CODEC, C2SWingWildStalkerDashPacket::handle);
        registrar.playToServer(C2SBroomSprintStatePacket.TYPE, C2SBroomSprintStatePacket.STREAM_CODEC, C2SBroomSprintStatePacket::handle);
        registrar.playToServer(C2SWingStormElectricityPacket.TYPE, C2SWingStormElectricityPacket.STREAM_CODEC, C2SWingStormElectricityPacket::handle);
        registrar.playToServer(C2SBroomStormElectricityPacket.TYPE, C2SBroomStormElectricityPacket.STREAM_CODEC, C2SBroomStormElectricityPacket::handle);
    }

    public static <MSG extends CustomPacketPayload> void sendToServer(MSG message) {
        PacketDistributor.sendToServer(message);
    }

    public static <MSG extends CustomPacketPayload> void sendToClient(MSG message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public static <MSG extends CustomPacketPayload> void sendToClientsTrackingEntity(MSG message, Entity entity) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, message);
    }

    public static <MSG extends CustomPacketPayload> void sendToClientsTrackingEntityAndSelf(MSG message, Entity entity) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, message);
    }

    public static <MSG extends CustomPacketPayload> void sendToClientsTrackingChunk(MSG message, ServerLevel level, ChunkPos chunkPos) {
        PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos, message);
    }
}
