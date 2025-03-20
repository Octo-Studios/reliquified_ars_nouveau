package it.hurts.octostudios.reliquified_ars_nouveau.network.packets;

import it.hurts.octostudios.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.head.WhirligigPetalsItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PetalsJumpPacket(boolean toggled) implements CustomPacketPayload {
    public static final Type<PetalsJumpPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ReliquifiedArsNouveau.MODID, "power_jump"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PetalsJumpPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, PetalsJumpPacket::toggled,
            PetalsJumpPacket::new);

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.WHIRLIGIG_PETALS.value());

            for (var stack : stacks) {
                if (!(stack.getItem() instanceof WhirligigPetalsItem relic))
                    return;

                if (relic.getTime(stack) >= relic.getActualStatValue(player, stack) - 1F)
                    relic.spreadRelicExperience(player, stack, 1);

                relic.setToggled(stack, toggled);
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}