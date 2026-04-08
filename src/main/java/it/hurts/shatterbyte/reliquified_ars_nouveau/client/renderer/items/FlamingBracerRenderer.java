package it.hurts.shatterbyte.reliquified_ars_nouveau.client.renderer.items;

import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.shatterbyte.reliquified_ars_nouveau.client.models.items.FlamingBracerSlimModel;
import it.hurts.shatterbyte.reliquified_ars_nouveau.client.models.items.FlamingBracerWideModel;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.bracelet.FlamingBracerItem;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class FlamingBracerRenderer implements IRelicRenderer {
    private final FlamingBracerSlimModel slimModel;
    private final FlamingBracerWideModel wideModel;

    public FlamingBracerRenderer() {
        this.slimModel = new FlamingBracerSlimModel(Minecraft.getInstance().getEntityModels().bakeLayer(FlamingBracerSlimModel.LAYER));
        this.wideModel = new FlamingBracerWideModel(Minecraft.getInstance().getEntityModels().bakeLayer(FlamingBracerWideModel.LAYER));
    }

    @Override
    public <E extends LivingEntity, EM extends EntityModel<E>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<E, EM> parent, MultiBufferSource bufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        var entity = slotContext.entity();
        var relic = (FlamingBracerItem) stack.getItem();

        var isSlim = Minecraft.getInstance().getSkinManager().getInsecureSkin(Minecraft.getInstance().getGameProfile()).model() == PlayerSkin.Model.SLIM;

        var model = isSlim ? slimModel : wideModel;

        poseStack.pushPose();

        model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        ICurioRenderer.followBodyRotations(entity, model);

        model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutout(ResourceLocation.fromNamespaceAndPath(ReliquifiedArsNouveau.MODID, "textures/item/model/flaming_bracer_" + (isSlim ? "slim" : "wide") + ".png"))), relic.getRelicData(entity, stack).isFlawless() ? LightTexture.FULL_BRIGHT : light, OverlayTexture.NO_OVERLAY);

        model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutout(ResourceLocation.fromNamespaceAndPath(ReliquifiedArsNouveau.MODID, "textures/item/model/flaming_bracer_gem.png"))), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }
}