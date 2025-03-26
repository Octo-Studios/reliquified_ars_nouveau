package it.hurts.octostudios.reliquified_ars_nouveau.network.packets;

import com.hollingsworth.arsnouveau.common.capability.ManaCap;
import it.hurts.octostudios.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.back.WhirlingBroomItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ActivatedBoostBroomPacket(boolean toggled) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ActivatedBoostBroomPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ReliquifiedArsNouveau.MODID, "flying_boost"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ActivatedBoostBroomPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ActivatedBoostBroomPacket::toggled,
            ActivatedBoostBroomPacket::new);

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();

            var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.WHIRLING_BROOM.value());

            if (!(stack.getItem() instanceof WhirlingBroomItem relic))
                return;

            var mana = new ManaCap(player);
            var manaCostInTick = relic.getStatValue(stack, "broom", "manacost") / 10;

            if (toggled && mana.getCurrentMana() >= manaCostInTick) {
                if (!player.isCreative())
                    mana.removeMana(manaCostInTick);

                relic.setToggled(stack, true);
            } else
                relic.setToggled(stack, false);
        });
    }


    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}