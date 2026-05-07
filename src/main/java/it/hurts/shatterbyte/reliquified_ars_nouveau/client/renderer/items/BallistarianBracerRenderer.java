package it.hurts.shatterbyte.reliquified_ars_nouveau.client.renderer.items;

import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.shatterbyte.reliquified_ars_nouveau.client.models.items.ArchmageGloveSlimModel;
import it.hurts.shatterbyte.reliquified_ars_nouveau.client.models.items.ArchmageGloveWideModel;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.bracelet.BallistarianBracerItem;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.hands.ArchmageGloveItem;
import it.hurts.sskirillss.relics.client.renderer.items.base.IRelicRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class BallistarianBracerRenderer implements IRelicRenderer {
    public BallistarianBracerRenderer() {

    }

    @Override
    public <E extends LivingEntity, EM extends EntityModel<E>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<E, EM> parent, MultiBufferSource bufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        var relic = (BallistarianBracerItem) stack.getItem();

        if (!(slotContext.entity() instanceof Player player))
            return;

        var ability = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("ballistary");

        var bows = Math.max(1, (int) Math.round(Math.max(0D, ability.getStatData("projectile_count").getValue())));

        var time = player.tickCount + (Minecraft.getInstance().isPaused() ? 0 : partialTicks);

        var minecraft = net.minecraft.client.Minecraft.getInstance();
        var renderer = minecraft.getItemRenderer();

        var deltaX = (float) (Mth.lerp(partialTicks, player.xCloakO, player.xCloak) - Mth.lerp(partialTicks, player.xo, player.getX()));
        var deltaY = (float) (Mth.lerp(partialTicks, player.yCloakO, player.yCloak) - Mth.lerp(partialTicks, player.yo, player.getY()));
        var deltaZ = (float) (Mth.lerp(partialTicks, player.zCloakO, player.zCloak) - Mth.lerp(partialTicks, player.zo, player.getZ()));

        var amplitude = 0.5F;

        var bodyYaw = Mth.rotLerp(partialTicks, player.yBodyRotO, player.yBodyRot) * ((float) Math.PI / 180F);

        var sin = (float) Math.sin(bodyYaw);
        var cos = (float) Math.cos(bodyYaw);

        var forward = -deltaX * sin + deltaZ * cos;
        var side = deltaX * cos + deltaZ * sin;

        var dx = side * amplitude;
        var dy = -deltaY * amplitude;
        var dz = -forward * amplitude;

        var levAmp = 0.1F;
        var levSpeed = 0.1F;
        var levOffset = Mth.sin(time * levSpeed) * levAmp;

        poseStack.pushPose();

        poseStack.translate(dx, dy + levOffset, dz);

        for (var i = 0; i < bows; i++) {
            var progress = bows == 1 ? 0.5D : (double) i / (double) (bows - 1);
            var angle = -Math.PI * 0.5D + progress * Math.PI;
            var radius = 1.5D;
            var x = Math.sin(angle) * radius;
            var y = 0.25D + -Math.cos(angle);
            var z = Math.cos(angle) * radius;
            var inwardFactor = Math.abs(Math.sin(angle));
            var inwardYaw = (float) (Math.signum(x) * inwardFactor * 28D);

            poseStack.pushPose();

            poseStack.translate(x, y, z);

            poseStack.mulPose(Axis.YP.rotationDegrees(90F));
            poseStack.mulPose(Axis.YP.rotationDegrees(inwardYaw));
            poseStack.mulPose(Axis.ZN.rotationDegrees(45F));

            poseStack.scale(1.3F, 1.3F, 1.3F);

            poseStack.translate(0, Math.sin(time * 0.1F) * (i % 2 == 0 ? -1 : 1) * 0.05F, 0);

            renderer.renderStatic(player, ItemsRegistry.SPELL_BOW.get().getDefaultInstance(), ItemDisplayContext.FIXED, false, poseStack, bufferSource, player.level(), relic.getRelicData(player, stack).isFlawless() ? LightTexture.FULL_BRIGHT : light, OverlayTexture.NO_OVERLAY, 31 + i);

            poseStack.popPose();
        }

        poseStack.popPose();
    }
}