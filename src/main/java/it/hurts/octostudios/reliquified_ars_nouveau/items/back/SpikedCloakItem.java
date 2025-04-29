package it.hurts.octostudios.reliquified_ars_nouveau.items.back;

import com.google.common.collect.Lists;
import com.hollingsworth.arsnouveau.common.entity.EntityChimeraProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.NouveauRelicItem;
import it.hurts.octostudios.reliquified_ars_nouveau.items.base.loot.LootEntries;
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
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.awt.*;
import java.util.List;

public class SpikedCloakItem extends NouveauRelicItem implements IRenderableCurio {
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("spikes")
                                .stat(StatData.builder("threshold")
                                        .initialValue(0.1D, 0.3D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.1)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatData.builder("damage")
                                        .initialValue(2D, 5D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.2)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(StatData.builder("time")
                                        .initialValue(8D, 12D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.15)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingData.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .sources(LevelingSourcesData.builder()
                                .source(LevelingSourceData.abilityBuilder("spikes")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.YELLOW)
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
                                .startColor(0xFFce4e30)
                                .endColor(0x0087113e)
                                .build())
                        .build())
                .loot(LootData.builder()
                        .entry(LootEntries.ARS_NOUVEAU, LootEntries.ARS_NOUVEAU_LIKE)
                        .build())
                .build();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player) || player.getCommandSenderWorld().isClientSide()
                || !isAbilityUnlocked(stack, "spikes"))
            return;

        if (getTime(stack) > 0)
            consumeTime(stack, 1);

        if (getTime(stack) <= 0)
            setCount(stack, 1);

        if (getCharges(stack) >= getActualActivatedHP(player, stack)) {
            var level = (ServerLevel) player.getCommandSenderWorld();
            var random = player.getRandom();

            for (int i = 0; i < 35; i++) {
                var modifier = 0.5F;
                var spike = new EntityChimeraProjectile(level);

                spike.setPos(player.getX(), player.getY(), player.getZ());
                spike.setOwner(player);
                spike.setDeltaMovement(MathUtils.randomFloat(random) * modifier, random.nextFloat() * modifier + 0.25F, MathUtils.randomFloat(random) * modifier);

                level.addFreshEntity(spike);
            }

            spreadRelicExperience(player, stack, 1);
            setCharges(stack, 0);
            setTime(stack, (int) MathUtils.round(getStatValue(stack, "spikes", "time"), 0) * 20);

            if (getTime(stack) > 0) {
                addCount(stack, 1);
                player.getCommandSenderWorld().playSound(null, player, SoundEvents.CAMEL_SADDLE, SoundSource.PLAYERS, 1.0F, 0.9F + player.getRandom().nextFloat() * 0.2F);
            }

            ((ServerLevel) player.getCommandSenderWorld()).sendParticles(ParticleUtils.constructSimpleSpark(new Color(150 + random.nextInt(100), 100 + random.nextInt(80), 50 + random.nextInt(50)), 0.2F, 20, 0.85F),
                    player.getX(), player.getY(), player.getZ(), 50, 0.5, 1, 0.5, 0.1);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack matrixStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        CurioModel model = getModel(stack);

        matrixStack.pushPose();

        var entity = slotContext.entity();

        model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        ICurioRenderer.followBodyRotations(entity, model);

        var vertexconsumer = ItemRenderer.getArmorFoilBuffer(renderTypeBuffer, RenderType.entityCutout(getTexture(stack)), stack.hasFoil());

        model.renderToBuffer(matrixStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY);

        matrixStack.popPose();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LayerDefinition constructLayerDefinition() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.4F), 0.0F);
        PartDefinition partdefinition = mesh.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(-0.0003F, 0.3556F, 0.0307F));

        PartDefinition cube_r1 = body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(9, 66).addBox(-5.5F, -11.8353F, 2.3134F, 11.0F, 24.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(24, 36).addBox(0.0F, -6.3002F, 2.1205F, 0.0F, 19.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0003F, 10.132F, 3.7627F, 0.2182F, 0.0F, 0.0F));

        PartDefinition cube_r2 = body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(50, 57).addBox(-3.6596F, -9.4247F, 7.7051F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0003F, 10.132F, 3.7627F, 1.0717F, 0.0628F, 1.0142F));

        PartDefinition cube_r3 = body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(36, 57).addBox(4.1336F, -8.9426F, 7.7051F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0003F, 10.132F, 3.7627F, 1.0944F, 0.0124F, 0.0537F));

        PartDefinition cube_r4 = body.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(50, 50).addBox(1.6596F, -9.4247F, 7.7051F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0003F, 10.132F, 3.7627F, 1.0717F, -0.0628F, -1.0142F));

        PartDefinition cube_r5 = body.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(50, 43).addBox(-6.1336F, -8.9426F, 7.7051F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0003F, 10.132F, 3.7627F, 1.0944F, -0.0124F, -0.0537F));

        PartDefinition cube_r6 = body.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(46, 29).addBox(-4.796F, 4.597F, 5.6375F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0003F, 10.132F, 3.7627F, -0.2618F, -0.4363F, 0.0F));

        PartDefinition cube_r7 = body.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(46, 22).addBox(-4.3378F, 0.3899F, 4.8797F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0003F, 10.132F, 3.7627F, 0.3038F, -0.762F, -0.2132F));

        PartDefinition cube_r8 = body.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(50, 36).addBox(-3.9929F, -5.1577F, 4.721F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0003F, 10.132F, 3.7627F, 0.8727F, 0.0F, -0.4363F));

        PartDefinition cube_r9 = body.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(36, 50).addBox(1.9929F, -5.1577F, 4.721F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.9997F, 10.132F, 3.7627F, 0.8727F, 0.0F, 0.4363F));

        PartDefinition cube_r10 = body.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(36, 43).addBox(2.796F, 4.597F, 5.6375F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.9997F, 10.132F, 3.7627F, -0.2618F, 0.4363F, 0.0F));

        PartDefinition cube_r11 = body.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(36, 36).addBox(2.3378F, 0.3899F, 4.8797F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.9997F, 10.132F, 3.7627F, 0.3038F, 0.762F, 0.2132F));

        PartDefinition cube_r12 = body.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(37, 15).addBox(-5.501F, 0.3112F, -2.8444F, 11.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0003F, 0.2131F, 0.3331F, -0.1745F, 0.0F, 0.0F));

        PartDefinition cube_r13 = body.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(34, 0).addBox(-4.5F, -2.2416F, 2.3602F, 9.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0003F, -0.0365F, 0.3386F, -0.4363F, 0.0F, 0.0F));

        PartDefinition cube_r14 = body.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(9, 0).addBox(-5.5F, -0.5585F, 3.1936F, 11.0F, 24.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 36).addBox(-5.5F, -0.5585F, -2.8064F, 0.0F, 24.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(12, 36).addBox(5.5F, -0.5585F, -2.8064F, 0.0F, 24.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(3, 24).addBox(-5.5F, -0.5585F, -2.8064F, 11.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0003F, -0.0365F, 0.3386F, 0.2182F, 0.0F, 0.0F));

        PartDefinition cube_r15 = body.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(3, 30).addBox(-5.501F, 20.9365F, 11.0185F, 11.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0003F, -0.0365F, 0.3386F, -0.1309F, 0.0F, 0.0F));

        PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(64, 52).mirror().addBox(-2.0F, -2.5F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(5.751F, 2.2082F, 0.0538F));

        PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(64, 52).addBox(-3.0F, -2.5F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.751F, 2.2082F, 0.0538F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public List<String> bodyParts() {
        return Lists.newArrayList("right_arm", "left_arm", "body");
    }

    public int getActualActivatedHP(Player player, ItemStack stack) {
        return (int) (player.getMaxHealth() * (1 - getStatValue(stack, "spikes", "threshold")));
    }

    public int getCharges(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.CHARGE, 0);
    }

    public void setCharges(ItemStack stack, int amount) {
        stack.set(DataComponentRegistry.CHARGE, Math.max(amount, 0));
    }

    public void addCharges(ItemStack stack, int amount) {
        setCharges(stack, getCharges(stack) + amount);
    }

    public int getCount(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.COUNT, 1);
    }

    public void setCount(ItemStack stack, int amount) {
        stack.set(DataComponentRegistry.COUNT, Math.max(amount, 1));
    }

    public void addCount(ItemStack stack, int amount) {
        setCount(stack, getCount(stack) + amount);
    }

    public int getTime(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.TIME, 0);
    }

    public void setTime(ItemStack stack, int amount) {
        stack.set(DataComponentRegistry.TIME, Math.max(amount, 0));
    }

    public void consumeTime(ItemStack stack, int amount) {
        setTime(stack, getTime(stack) - amount);
    }

    @EventBusSubscriber
    public static class SpikedCloakEvent {
        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            if (!(event.getEntity() instanceof Player player) || player.getCommandSenderWorld().isClientSide())
                return;

            var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.SPIKED_CLOAK.value());

            if (!(stack.getItem() instanceof SpikedCloakItem relic) || !relic.isAbilityUnlocked(stack, "spikes"))
                return;

            relic.setCharges(stack, 0);
        }

        @SubscribeEvent
        public static void onInjuredEntity(LivingDamageEvent.Pre event) {
            if (!(event.getEntity() instanceof Player player) || player.getCommandSenderWorld().isClientSide())
                return;

            var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.SPIKED_CLOAK.value());

            if (!(stack.getItem() instanceof SpikedCloakItem relic) || !relic.isAbilityUnlocked(stack, "spikes"))
                return;

            relic.addCharges(stack, (int) event.getNewDamage());
        }
    }
}
