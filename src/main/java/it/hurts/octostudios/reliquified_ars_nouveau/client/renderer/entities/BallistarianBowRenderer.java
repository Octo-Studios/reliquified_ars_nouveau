package it.hurts.octostudios.reliquified_ars_nouveau.client.renderer.entities;

import com.hollingsworth.arsnouveau.ArsNouveau;
import com.hollingsworth.arsnouveau.client.particle.ParticleColor;
import com.hollingsworth.arsnouveau.client.particle.ParticleUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.hurts.octostudios.reliquified_ars_nouveau.client.renderer.models.entities.BallistarianBowModel;
import it.hurts.octostudios.reliquified_ars_nouveau.entities.BallistarianBowEntity;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.Color;

public class BallistarianBowRenderer extends GeoEntityRenderer<BallistarianBowEntity> {
    public BallistarianBowRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BallistarianBowModel());
    }

    @Override
    public void renderRecursively(PoseStack poseStack, BallistarianBowEntity bow, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int color) {
        poseStack.pushPose();

        poseStack.translate(0, 0.1F, 0);

        float yaw = bow.yRotO + (bow.getYRot() - bow.yRotO) * partialTick;
        float pitch = bow.xRotO + (bow.getXRot() - bow.xRotO) * partialTick;

//        poseStack.mulPose(Axis.YP.rotation((float) Math.toRadians(-yaw)));
//        poseStack.mulPose(Axis.XP.rotation((float) Math.toRadians(-pitch)));
        poseStack.mulPose(Axis.YP.rotationDegrees(-90));

        if (bone.getName().equals("gem"))
            super.renderRecursively(poseStack, bow, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, color);
        else
            super.renderRecursively(poseStack, bow, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, Color.WHITE.argbInt());

        poseStack.popPose();
    }

    @Override
    public Color getRenderColor(BallistarianBowEntity bow, float partialTick, int packedLight) {
        var color = ParticleColor.defaultParticleColor();

        return Color.ofRGBA(color.getRed(), color.getGreen(), color.getBlue(), 0.75f);
    }

    @Override
    public void renderFinal(PoseStack poseStack, BallistarianBowEntity bow, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay, int color) {
        if (model.getBone("bow_top").isEmpty() || model.getBone("gem").isEmpty()
                || model.getBone("bow_bot").isEmpty())
            return;

        var gem = model.getBone("gem").get();
        var outerAngle = (bow.tickCount + partialTick) % 360;

        gem.setRotX(outerAngle);
        gem.setRotY(outerAngle);

        var gemPos = gem.getWorldPosition();
        var level = bow.getCommandSenderWorld();

        if (bow.tickCount % 5 == 0) {
            var pointSphere = ParticleUtil.pointInSphere().scale(0.3f);

            gemPos.add(pointSphere.x, pointSphere.y, pointSphere.z);

            level.addParticle(ParticleUtils.constructSimpleSpark(java.awt.Color.MAGENTA, 0.2F, 20, 0.7F),
                    gemPos.x(), gemPos.y(), gemPos.z(), 0, 0, 0);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(BallistarianBowEntity bow) {
        return ArsNouveau.prefix("textures/item/spellbow.png");
    }
}
