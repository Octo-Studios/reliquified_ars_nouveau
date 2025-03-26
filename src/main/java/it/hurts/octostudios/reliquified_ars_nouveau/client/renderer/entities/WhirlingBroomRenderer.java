package it.hurts.octostudios.reliquified_ars_nouveau.client.renderer.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.hurts.octostudios.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.octostudios.reliquified_ars_nouveau.client.renderer.models.entities.WhirlingBroomModel;
import it.hurts.octostudios.reliquified_ars_nouveau.entities.WhirlingBroomEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class WhirlingBroomRenderer extends EntityRenderer<WhirlingBroomEntity> {
    public WhirlingBroomRenderer(Context context) {
        super(context);
    }

    @Override
    public void render(@NotNull WhirlingBroomEntity broomEntity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(partialTick, broomEntity.yHeadRotO, broomEntity.yHeadRot)));
        poseStack.mulPose(Axis.YP.rotationDegrees(180));

        poseStack.translate(-0.03, -1, 0);

        new WhirlingBroomModel<>().renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutout(getTextureLocation(broomEntity))), packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(WhirlingBroomEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(ReliquifiedArsNouveau.MODID, "textures/entities/whirling_broom.png");
    }
}
