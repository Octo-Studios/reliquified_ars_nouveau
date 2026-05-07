package it.hurts.shatterbyte.reliquified_ars_nouveau.network.packets;

import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.body.WingWildStalkerItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record C2SWingWildStalkerDashPacket() implements CustomPacketPayload {
    public static final Type<C2SWingWildStalkerDashPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ReliquifiedArsNouveau.MODID, "wing_wild_stalker_dash"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SWingWildStalkerDashPacket> STREAM_CODEC =
            StreamCodec.unit(new C2SWingWildStalkerDashPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SWingWildStalkerDashPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player) || player.level().isClientSide() || !player.isFallFlying())
                return;

            var totalStrength = 0D;
            var gameTime = player.level().getGameTime();

            for (var stack : EntityUtils.findEquippedCurios(player, ItemRegistry.WING_OF_TH_WILD_STALKER.get())) {
                if (!(stack.getItem() instanceof WingWildStalkerItem item))
                    continue;

                var relicData = item.getRelicData(player, stack);
                var ability = relicData.getAbilitiesData().getAbilityData("wild_flight");

                if (!ability.getRankModifierData("aerial_dash").isEnabled())
                    continue;

                var cooldownUntil = stack.getOrDefault(RANDataComponentRegistry.WING_WILD_STALKER_DASH_COOLDOWN_UNTIL.get(), 0L);

                if (cooldownUntil > gameTime)
                    continue;

                var dashStrength = Math.max(0D, ability.getStatData("dash_strength").getValue());

                if (dashStrength <= 0D)
                    continue;

                totalStrength += dashStrength;
                stack.set(RANDataComponentRegistry.WING_WILD_STALKER_FLIGHT_TICKS.get(), 0);
                var cooldownTicks = Math.max(1L, Math.round(Math.max(0D, ability.getStatData("dash_cooldown").getValue()) * 20D));
                stack.set(RANDataComponentRegistry.WING_WILD_STALKER_DASH_COOLDOWN_UNTIL.get(), gameTime + cooldownTicks);
                relicData.getLevelingData().addExperience("wild_flight", "dash", 1D);
                ability.getStatisticData().getMetricData("dashes_used").addValue(1D);
            }

            if (totalStrength <= 0D)
                return;
        });
    }
}

