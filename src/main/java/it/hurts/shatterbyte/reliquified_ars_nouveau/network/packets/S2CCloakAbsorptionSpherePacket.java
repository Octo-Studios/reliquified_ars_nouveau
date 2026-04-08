package it.hurts.shatterbyte.reliquified_ars_nouveau.network.packets;

import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.shatterbyte.reliquified_ars_nouveau.client.renderer.misc.CloakAbsorptionSphereRenderer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2CCloakAbsorptionSpherePacket implements CustomPacketPayload {
    private final int entityId;
    private final boolean partialAbsorption;

    public S2CCloakAbsorptionSpherePacket(int entityId, boolean partialAbsorption) {
        this.entityId = entityId;
        this.partialAbsorption = partialAbsorption;
    }

    public int getEntityId() {
        return entityId;
    }

    public boolean isPartialAbsorption() {
        return partialAbsorption;
    }

    public static final Type<S2CCloakAbsorptionSpherePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ReliquifiedArsNouveau.MODID, "cloak_absorption_sphere"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CCloakAbsorptionSpherePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, S2CCloakAbsorptionSpherePacket::getEntityId,
                    ByteBufCodecs.BOOL, S2CCloakAbsorptionSpherePacket::isPartialAbsorption,
                    S2CCloakAbsorptionSpherePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> CloakAbsorptionSphereRenderer.add(entityId, partialAbsorption));
    }
}
