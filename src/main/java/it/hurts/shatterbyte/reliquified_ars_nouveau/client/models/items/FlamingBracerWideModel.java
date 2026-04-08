package it.hurts.shatterbyte.reliquified_ars_nouveau.client.models.items;

import com.google.common.collect.ImmutableList;
import it.hurts.sskirillss.relics.Relics;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class FlamingBracerWideModel extends HumanoidModel<LivingEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "flaming_bracer_wide"), "flaming_bracer_wide");

    public ModelPart rightArmPart;
    public ModelPart leftArmPart;

    public FlamingBracerWideModel(ModelPart root) {
        super(root);

        this.rightArmPart = root.getChild("right_arm");
        this.leftArmPart = root.getChild("left_arm");
    }

    public static LayerDefinition constructLayerDefinition() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(new CubeDeformation(0.4F), 0F);
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, 1.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.2F))
                .texOffs(0, 10).addBox(-3.5F, 0.75F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.4F))
                .texOffs(0, 17).addBox(-3.5F, 4.25F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.4F)), PartPose.offset(-5.0F, 2.5F, 0.0F));

        right_arm.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(20, 4).addBox(-1.875F, 0.2374F, 0.2374F, 1.0F, 2.0F, 2.0F, new CubeDeformation(-0.1F))
                .texOffs(20, 0).addBox(-1.875F, -2.2374F, -2.2374F, 1.0F, 2.0F, 2.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(-2.375F, 3.5F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.5F, 1.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.2F)).mirror(false)
                .texOffs(0, 10).mirror().addBox(-1.5F, 0.75F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.4F)).mirror(false)
                .texOffs(0, 17).mirror().addBox(-1.5F, 4.25F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.4F)).mirror(false), PartPose.offset(5.0F, 2.5F, 0.0F));

        left_arm.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(20, 4).mirror().addBox(0.875F, 0.2374F, 0.2374F, 1.0F, 2.0F, 2.0F, new CubeDeformation(-0.1F)).mirror(false)
                .texOffs(20, 0).mirror().addBox(0.875F, -2.2374F, -2.2374F, 1.0F, 2.0F, 2.0F, new CubeDeformation(-0.1F)).mirror(false), PartPose.offsetAndRotation(2.375F, 3.5F, 0.0F, -0.7854F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.rightArmPart, this.leftArmPart);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of();
    }
}