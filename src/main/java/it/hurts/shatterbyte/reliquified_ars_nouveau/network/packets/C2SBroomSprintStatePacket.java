package it.hurts.shatterbyte.reliquified_ars_nouveau.network.packets;

import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.shatterbyte.reliquified_ars_nouveau.entities.WhirlingBroomEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record C2SBroomSprintStatePacket(boolean sprinting) implements CustomPacketPayload {
    public static final Type<C2SBroomSprintStatePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ReliquifiedArsNouveau.MODID, "broom_sprint_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SBroomSprintStatePacket> STREAM_CODEC =
            StreamCodec.of((buffer, packet) -> buffer.writeBoolean(packet.sprinting), buffer -> new C2SBroomSprintStatePacket(buffer.readBoolean()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SBroomSprintStatePacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player) || player.level().isClientSide())
                return;

            if (player.getVehicle() instanceof WhirlingBroomEntity broom)
                broom.setBoostRequested(packet.sprinting);
        });
    }
}
