package it.hurts.octostudios.reliquified_ars_nouveau.network.packets;

import it.hurts.octostudios.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.body.WingWildStalkerItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
public class WingStartFlyPacket implements CustomPacketPayload {
    private final boolean toggled;

    public static final Type<WingStartFlyPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ReliquifiedArsNouveau.MODID, "flying"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WingStartFlyPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, WingStartFlyPacket::isToggled,
            WingStartFlyPacket::new);

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            var stackFirst = EntityUtils.findEquippedCurios(player, ItemRegistry.WING_OF_TH_WILD_STALKER.value()).getFirst();

            if (!(stackFirst.getItem() instanceof WingWildStalkerItem relic))
                return;

            if (toggled) {
                relic.setTime(stackFirst, (int) MathUtils.round(relic.getStatValue(stackFirst, "wings", "time"), 0) + 1);
                relic.setCharge(stackFirst, (int) MathUtils.round(relic.getStatValue(stackFirst, "wings", "charges"), 0));
            } else
                relic.consumeCharge(stackFirst, 1);
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
