package it.hurts.octostudios.reliquified_ars_nouveau.client.renderer.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.octostudios.reliquified_ars_nouveau.entities.WhirlingBroomEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class WhirlingBroomRenderer extends EntityRenderer<WhirlingBroomEntity> {
    public WhirlingBroomRenderer(Context context) {
        super(context);
    }

    @Override
    public void render(WhirlingBroomEntity p_entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

    }

    @Override
    public ResourceLocation getTextureLocation(WhirlingBroomEntity entity) {
        return null;
    }
}
