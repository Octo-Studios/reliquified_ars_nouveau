package it.hurts.octostudios.reliquified_ars_nouveau.client.renderer.models.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class WhirlingBroomModel<T extends Entity> extends EntityModel<T> {
    private final ModelPart bone;

    public WhirlingBroomModel() {
        var meshdefinition = new MeshDefinition();
        var partdefinition = meshdefinition.getRoot();

        var bb_main = partdefinition.addOrReplaceChild("broom", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -3.0F, -19.0F, 3.0F, 3.0F, 32.0F, new CubeDeformation(0.0F))
                .texOffs(70, 0).addBox(-3.0F, -5.0F, 6.0F, 7.0F, 7.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(0, 70).addBox(-3.0F, -5.0F, 6.0F, 7.0F, 7.0F, 15.0F, new CubeDeformation(0.4F))
                .texOffs(0, 35).addBox(-1.0F, -3.0F, -19.0F, 3.0F, 3.0F, 32.0F, new CubeDeformation(0.2F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        bb_main.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(70, 18).addBox(-1.5F, -2.0F, -2.0F, 3.0F, 4.0F, 4.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(0.5F, -1.6F, -18.0F, -0.7854F, 0.0F, 0.0F));

        bone = LayerDefinition.create(meshdefinition, 128, 128).bakeRoot();
    }

    @Override
    public void setupAnim(@NotNull T entity, float v, float v1, float v2, float v3, float v4) {

    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer buffer, int packedLight, int packedOverlay, int seed) {
        bone.render(poseStack, buffer, packedLight, packedOverlay, seed);
    }
}
