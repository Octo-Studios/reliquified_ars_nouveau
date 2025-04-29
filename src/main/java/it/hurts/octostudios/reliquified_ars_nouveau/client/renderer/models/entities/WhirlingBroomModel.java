package it.hurts.octostudios.reliquified_ars_nouveau.client.renderer.models.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class WhirlingBroomModel<T extends Entity> extends EntityModel<T> {
    private final ModelPart bone;

    public WhirlingBroomModel() {
        var meshdefinition = new MeshDefinition();
        var partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.1983F, 5.477F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition cube_r1 = body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(54, 60).addBox(3.8505F, -8.2643F, 1.5F, -3.0F, -4.0F, -3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 31).addBox(1.3505F, -15.2643F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(20, 31).addBox(-1.9491F, 3.4486F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 1.3963F, -1.5708F));

        PartDefinition cube_r2 = body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(53, 58).addBox(1.8479F, 0.9072F, 1.5F, -3.0F, -4.0F, -3.0F, new CubeDeformation(0.0F))
                .texOffs(28, 30).addBox(-0.6521F, -6.0928F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 1.3963F, 1.5708F));

        PartDefinition cube_r3 = body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-3.8017F, 10.523F, -4.0F, 8.0F, 7.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 15).addBox(-3.3017F, 9.523F, -3.5F, 7.0F, 9.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(28, 15).addBox(-3.3017F, 15.523F, -3.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(28, 22).addBox(-2.3017F, 6.523F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition cube_r4 = body.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(32, 7).addBox(11.7295F, -11.4133F, -1.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.6109F, -1.5708F));

        bone = LayerDefinition.create(meshdefinition, 64, 64).bakeRoot();
    }

    @Override
    public void setupAnim(@NotNull T entity, float v, float v1, float v2, float v3, float v4) {

    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer buffer, int packedLight, int packedOverlay, int seed) {
        bone.render(poseStack, buffer, packedLight, packedOverlay, seed);
    }
}
