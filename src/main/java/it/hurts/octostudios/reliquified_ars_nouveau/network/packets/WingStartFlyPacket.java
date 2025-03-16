package it.hurts.octostudios.reliquified_ars_nouveau.network.packets;

import it.hurts.octostudios.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.body.WingWildStalkerItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

@Data
@AllArgsConstructor
public class WingStartFlyPacket implements CustomPacketPayload {
    public static final Type<WingStartFlyPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ReliquifiedArsNouveau.MODID, "flying"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WingStartFlyPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull WingStartFlyPacket packet) {
        }

        @Nonnull
        @Override
        public WingStartFlyPacket decode(@Nonnull RegistryFriendlyByteBuf buf) {
            return new WingStartFlyPacket();
        }
    };

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            var stackFirst = EntityUtils.findEquippedCurios(player, ItemRegistry.WING_OF_TH_WILD_STALKER.value()).getFirst();

            if (!(stackFirst.getItem() instanceof WingWildStalkerItem relic) || !relic.getToggled(stackFirst))
                return;

            relic.setTime(stackFirst, (int) MathUtils.round(relic.getStatValue(stackFirst, "wings", "time"), 0) + 1);
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
