package it.hurts.octostudios.reliquified_ars_nouveau.client.renderer.models.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.octostudios.reliquified_ars_nouveau.client.renderer.models.parts.CuttingModel;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.sskirillss.relics.client.models.parts.HaloModel;
import it.hurts.sskirillss.relics.client.models.parts.WingsModel;
import it.hurts.sskirillss.relics.init.EffectRegistry;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class CuttingLayer <T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final CuttingModel cuttingModel;

    public CuttingLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);

        cuttingModel = new CuttingModel(CuttingModel.createBodyLayer().bakeRoot());
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int i, T livingEntity, float v, float v1, float v2, float v3, float v4, float v5) {
        var player = Minecraft.getInstance().player;
        var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.WHIRLISPRIG_PETALS.value());

        if (player == null || stack.isEmpty())
            return;

        poseStack.pushPose();

        ICurioRenderer.followBodyRotations(player, cuttingModel);

        cuttingModel.renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityTranslucentCull(CuttingModel.LAYER_LOCATION.getModel())), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }
}
