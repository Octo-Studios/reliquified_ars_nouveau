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

public class ArchmageGloveSlimModel extends HumanoidModel<LivingEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "archmage_glove_slim"), "archmage_glove_slim");

    public ModelPart rightArmPart;
    public ModelPart leftArmPart;

    public ArchmageGloveSlimModel(ModelPart root) {
        super(root);

        this.rightArmPart = root.getChild("right_arm");
        this.leftArmPart = root.getChild("left_arm");
    }

    public static LayerDefinition constructLayerDefinition() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(new CubeDeformation(0.4F), 0F);
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(1, 0).addBox(-3.0F, 4.0F, -3.0F, 5.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(1, 18).addBox(-2.5F, 6.0F, -2.5F, 4.0F, 5.0F, 5.0F, new CubeDeformation(0.25F))
                .texOffs(20, 8).addBox(-3.5F, 5.975F, -1.5F, 2.0F, 3.0F, 3.0F, new CubeDeformation(-0.4F))
                .texOffs(1, 8).addBox(-2.5F, 6.0F, -2.5F, 4.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(1, 0).mirror().addBox(-2.0F, 4.0F, -3.0F, 5.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(1, 18).mirror().addBox(-1.5F, 6.0F, -2.5F, 4.0F, 5.0F, 5.0F, new CubeDeformation(0.25F)).mirror(false)
                .texOffs(20, 8).mirror().addBox(1.5F, 5.975F, -1.5F, 2.0F, 3.0F, 3.0F, new CubeDeformation(-0.4F)).mirror(false)
                .texOffs(1, 8).mirror().addBox(-1.5F, 6.0F, -2.5F, 4.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(5.0F, 2.0F, 0.0F));

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