package it.hurts.shatterbyte.reliquified_ars_nouveau.network.packets;

import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.body.WingWildStalkerItem;
import it.hurts.sskirillss.relics.entities.ChainedElectricityEntity;
import it.hurts.sskirillss.relics.init.RelicsEntities;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record C2SWingStormElectricityPacket() implements CustomPacketPayload {
    public static final Type<C2SWingStormElectricityPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ReliquifiedArsNouveau.MODID, "wing_storm_electricity"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SWingStormElectricityPacket> STREAM_CODEC =
            StreamCodec.unit(new C2SWingStormElectricityPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SWingStormElectricityPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player) || player.level().isClientSide() || !player.isFallFlying())
                return;

            for (var stack : EntityUtils.findEquippedCurios(player, ItemRegistry.WING_OF_TH_WILD_STALKER.get())) {
                if (!(stack.getItem() instanceof WingWildStalkerItem item))
                    continue;

                var relicData = item.getRelicData(player, stack);
                var ability = relicData.getAbilitiesData().getAbilityData("wild_flight");
                var synergy = relicData.getAbilitiesData().getSynergyData("electricity");

                if (!synergy.isUnlocked() || synergy.getMode().equals("disabled"))
                    continue;

                var damage = Math.max(0D, synergy.getStatData("damage").getValue());
                var lifetime = Math.max(1, (int) synergy.getStatData("lifetime").getValue());

                if (damage <= 0D || lifetime <= 0)
                    continue;

                var previous = item.getLastElectricityEntity(player, stack);
                var position = player.position();
                var distanceToPreviousSqr = previous == null ? 0D : previous.position().distanceToSqr(position);

                if (previous != null && distanceToPreviousSqr < 1D)
                    continue;

                var electricity = new ChainedElectricityEntity(RelicsEntities.KINETIC_ELECTRICITY.get(), player.level());

                electricity.setDamage((float) damage);
                electricity.setLifetime(lifetime);
                electricity.setFlawless(relicData.isFlawless());
                electricity.setPos(position);
                electricity.setOwner(player);

                if (previous != null && distanceToPreviousSqr <= 100D)
                    electricity.setPreviousEntity(previous);

                player.level().addFreshEntity(electricity);
                stack.set(RANDataComponentRegistry.WING_WILD_STALKER_LAST_ELECTRICITY_ID.get(), electricity.getId());
            }
        });
    }
}

