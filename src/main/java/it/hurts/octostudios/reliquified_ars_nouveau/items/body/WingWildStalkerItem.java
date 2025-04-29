package it.hurts.octostudios.reliquified_ars_nouveau.items.body;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.NouveauRelicItem;
import it.hurts.octostudios.reliquified_ars_nouveau.items.base.loot.LootEntries;
import it.hurts.octostudios.reliquified_ars_nouveau.network.packets.WingStartFlyPacket;
import it.hurts.sskirillss.relics.client.models.items.CurioModel;
import it.hurts.sskirillss.relics.init.DataComponentRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRenderableCurio;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.*;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemColor;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemShape;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.BeamsData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.awt.*;
import java.util.List;

public class WingWildStalkerItem extends NouveauRelicItem implements IRenderableCurio {
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("wings")
                                .stat(StatData.builder("strength")
                                        .initialValue(2D, 4D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.1)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(StatData.builder("charges")
                                        .initialValue(2D, 3D)
                                        .upgradeModifier(UpgradeOperation.ADD, 0.65)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingData.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .sources(LevelingSourcesData.builder()
                                .source(LevelingSourceData.abilityBuilder("wings")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.CYAN)
                                        .build())
                                .build())
                        .build())
                .style(StyleData.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xff85543c)
                                .borderBottom(0xff85543c)
                                .textured(true)
                                .build())
                        .beams(BeamsData.builder()
                                .startColor(0xFFdf2427)
                                .endColor(0x00811c1e)
                                .build())
                        .build())
                .loot(LootData.builder()
                        .entry(LootEntries.ARS_NOUVEAU, LootEntries.ARS_NOUVEAU_LIKE)
                        .build())
                .build();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player) || player.getCommandSenderWorld().isClientSide())
            return;

        var stackFirst = EntityUtils.findEquippedCurios(player, ItemRegistry.WING_OF_TH_WILD_STALKER.value()).getFirst();

        if (stackFirst.getItem() != stack.getItem())
            return;

        if (player.isInLiquid() && player.isFallFlying())
            player.stopFallFlying();

        if (player.isFallFlying() && player.tickCount % 20 == 0)
            spreadRelicExperience(player, stack, 1);

        if (player.getKnownMovement().length() >= 2 && player.isFallFlying()) {
            var random = player.getRandom();
            var width = player.getBbWidth() / 2.0;
            var height = player.getBbHeight();

            for (int i = 0; i < 30; i++) {
                var offsetX = (random.nextDouble() - 0.5) * width * 2;
                var offsetY = random.nextDouble() * height;
                var offsetZ = (random.nextDouble() - 0.5) * width * 2;

                ((ServerLevel) player.getCommandSenderWorld()).sendParticles(ParticleUtils.constructSimpleSpark(new Color(150 + random.nextInt(106), random.nextInt(50), random.nextInt(50), random.nextInt(100 + random.nextInt(156))), 0.3F, 20, 0.95F),
                        player.getX() + offsetX, (player.getY() - 0.5) + offsetY, player.getZ() + offsetZ, 1, 0.1, 0.1, 0.1, 0.1);
            }
        }
    }

    public int getActualStatValue(ItemStack stack, String stat) {
        return (int) MathUtils.round(getStatValue(stack, "wings", stat), 0);
    }

    public int getCharge(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.CHARGE, 0);
    }

    public void setCharge(ItemStack stack, int charge) {
        stack.set(DataComponentRegistry.CHARGE, Math.max(charge, 0));
    }

    public void consumeCharge(ItemStack stack, int charge) {
        setCharge(stack, getCharge(stack) - charge);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack matrixStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        CurioModel model = getModel(stack);

        matrixStack.pushPose();

        LivingEntity entity = slotContext.entity();

        model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        ICurioRenderer.followBodyRotations(entity, model);
        ICurioRenderer.rotateIfSneaking(matrixStack, entity);

        VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(renderTypeBuffer, RenderType.armorCutoutNoCull(getTexture(stack)), stack.hasFoil());

        model.renderToBuffer(matrixStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY);

        matrixStack.popPose();
    }

    @Override
    public List<String> bodyParts() {
        return Lists.newArrayList("back");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LayerDefinition constructLayerDefinition() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.4F), 0.0F);
        PartDefinition partdefinition = mesh.getRoot();

        PartDefinition back = partdefinition.addOrReplaceChild("back", CubeListBuilder.create(), PartPose.offset(11.1726F, -2.4038F, 5.2295F));

        PartDefinition cube_r1 = back.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 15).addBox(-2.1367F, -7.2408F, -0.6113F, 2.0F, 15.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0956F, -0.2405F, -0.3988F));

        PartDefinition cube_r2 = back.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(24, 23).addBox(1.0018F, 6.3573F, 6.3213F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.0269F, -0.0929F, 1.1949F));

        PartDefinition cube_r3 = back.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(24, 15).addBox(4.8646F, -2.3078F, -0.6113F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2579F, 0.0168F, -1.6504F));

        PartDefinition cube_r4 = back.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(16, 23).addBox(-2.9741F, -0.7807F, -0.6363F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.9741F, -5.7807F, 0.3887F, 20.0F, 15.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.106F, -0.2362F, 0.4108F));

        PartDefinition cube_r5 = back.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(8, 15).addBox(5.7938F, 0.1053F, -0.6113F, 2.0F, 14.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2375F, -0.103F, -1.1849F));

        PartDefinition cube_r6 = back.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(16, 15).addBox(1.0018F, 4.3794F, -0.6113F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2416F, -0.0929F, 1.1949F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @EventBusSubscriber(Dist.CLIENT)
    public static class WingWildStalkerClientEvent {
        @SubscribeEvent
        public static void onClientTick(InputEvent.Key event) {
            var minecraft = Minecraft.getInstance();
            var player = minecraft.player;

            if (player == null || minecraft.screen != null || event.getAction() != 1 || event.getKey() != minecraft.options.keyJump.getKey().getValue()
                    || player.isInLiquid() || EntityUtils.findEquippedCurios(player, ItemRegistry.WING_OF_TH_WILD_STALKER.value()).isEmpty()
                    || player.mayFly())
                return;

            var stackFirst = EntityUtils.findEquippedCurios(player, ItemRegistry.WING_OF_TH_WILD_STALKER.value()).getFirst();

            if (!(stackFirst.getItem() instanceof WingWildStalkerItem relic) || !relic.isAbilityUnlocked(stackFirst, "wings"))
                return;

            var deltaMovement = player.getDeltaMovement();

            if (!player.isFallFlying() && !player.onGround()) {
                NetworkHandler.sendToServer(new WingStartFlyPacket(true));

                player.setDeltaMovement(deltaMovement.add(player.getLookAngle()).add(0, 0.6, 0));

                player.playSound(SoundEvents.ENDER_DRAGON_FLAP, 1.0F, 0.9F + player.getRandom().nextFloat() * 0.2F);
            } else if (relic.getCharge(stackFirst) > 0 && player.isFallFlying()) {
                player.setDeltaMovement(player.getDeltaMovement().add(player.getLookAngle().normalize().scale(relic.getStatValue(stackFirst, "wings", "strength") * 0.2F)));

                NetworkHandler.sendToServer(new WingStartFlyPacket(false));

                player.playSound(SoundEvents.ENDER_DRAGON_FLAP, 1.0F, 0.9F + player.getRandom().nextFloat() * 0.2F);
            }
        }
    }
}
