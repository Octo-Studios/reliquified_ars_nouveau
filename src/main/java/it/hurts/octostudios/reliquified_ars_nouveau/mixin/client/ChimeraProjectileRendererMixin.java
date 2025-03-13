package it.hurts.octostudios.reliquified_ars_nouveau.mixin.client;

import com.hollingsworth.arsnouveau.client.renderer.entity.ChimeraProjectileRenderer;
import com.hollingsworth.arsnouveau.common.entity.EntityChimeraProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChimeraProjectileRenderer.class)
public abstract class ChimeraProjectileRendererMixin {

    @Inject(method = "render*", at = @At("HEAD"))
    private void onRender(EntityChimeraProjectile entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, CallbackInfo ci) {
        Vec3 motion = entityIn.getDeltaMovement();

        if (motion.lengthSqr() > 0.0001) {
            float yaw = (float) (Mth.atan2(motion.z, motion.x) * (180F / Math.PI)) - 90.0F;
            float pitch = (float) (Mth.atan2(motion.y, motion.horizontalDistance()) * (180F / Math.PI));

            matrixStackIn.mulPose(Axis.YP.rotationDegrees(yaw));
            matrixStackIn.mulPose(Axis.ZP.rotationDegrees(pitch));
        }
    }
}
