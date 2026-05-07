package it.hurts.shatterbyte.reliquified_ars_nouveau.network.packets;

import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.shatterbyte.reliquified_ars_nouveau.entities.WhirlingBroomEntity;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.WhirlingBroomItem;
import it.hurts.sskirillss.relics.entities.ChainedElectricityEntity;
import it.hurts.sskirillss.relics.init.RelicsEntities;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record C2SBroomStormElectricityPacket() implements CustomPacketPayload {
    public static final Type<C2SBroomStormElectricityPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ReliquifiedArsNouveau.MODID, "broom_storm_electricity"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SBroomStormElectricityPacket> STREAM_CODEC =
            StreamCodec.unit(new C2SBroomStormElectricityPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SBroomStormElectricityPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player) || player.level().isClientSide() || !(player.getVehicle() instanceof WhirlingBroomEntity broom))
                return;

            var stackId = broom.getPersistentData().getString(WhirlingBroomItem.BROOM_STACK_ID_TAG);
            var damage = Math.max(0F, broom.getStormDamage());
            var lifetime = Math.max(0, broom.getStormLifetime());

            if (stackId.isBlank() || damage <= 0F || lifetime <= 0)
                return;

            for (var stack : EntityUtils.findItemsInInventory(player, ItemRegistry.WHIRLING_BROOM.get())) {
                if (!(stack.getItem() instanceof WhirlingBroomItem))
                    continue;

                if (!stackId.equals(stack.getOrDefault(RANDataComponentRegistry.WHIRLING_BROOM_STACK_ID.get(), "")))
                    continue;

                var previous = WhirlingBroomItem.getLastElectricityEntity(player, stack);
                var position = player.position();
                var distanceToPreviousSqr = previous == null ? 0D : previous.position().distanceToSqr(position);

                if (previous != null && distanceToPreviousSqr < 1D)
                    continue;

                var electricity = new ChainedElectricityEntity(RelicsEntities.KINETIC_ELECTRICITY.get(), player.level());

                electricity.setDamage(damage);
                electricity.setLifetime(lifetime);
                electricity.setFlawless(broom.isStormFlawless());
                electricity.setPos(position);
                electricity.setOwner(player);

                if (previous != null && distanceToPreviousSqr <= 100D)
                    electricity.setPreviousEntity(previous);

                player.level().addFreshEntity(electricity);
                stack.set(RANDataComponentRegistry.WHIRLING_BROOM_LAST_ELECTRICITY_ID.get(), electricity.getId());
            }
        });
    }
}

