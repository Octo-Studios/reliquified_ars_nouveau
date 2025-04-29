package it.hurts.octostudios.reliquified_ars_nouveau.items.back;

import com.google.common.collect.Lists;
import com.hollingsworth.arsnouveau.common.entity.EntityDummy;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.NouveauRelicItem;
import it.hurts.octostudios.reliquified_ars_nouveau.items.base.loot.LootEntries;
import it.hurts.sskirillss.relics.client.models.items.CurioModel;
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
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
import java.util.*;

public class IllusionistsMantleItem extends NouveauRelicItem implements IRenderableCurio {
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("illusion")
                                .stat(StatData.builder("chance")
                                        .initialValue(0.1D, 0.15D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatData.builder("duration")
                                        .initialValue(5D, 10D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .build())
                .style(StyleData.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xff28122d)
                                .borderBottom(0xff28122d)
                                .textured(true)
                                .build())
                        .beams(BeamsData.builder()
                                .startColor(0xFF8f21cf)
                                .endColor(0x004b168b)
                                .build())
                        .build())
                .leveling(LevelingData.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .sources(LevelingSourcesData.builder()
                                .source(LevelingSourceData.abilityBuilder("illusion")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.CYAN)
                                        .build())
                                .build())
                        .build())
                .loot(LootData.builder()
                        .entry(LootEntries.ARS_NOUVEAU, LootEntries.ARS_NOUVEAU_LIKE)
                        .build())
                .build();
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player) || prevStack.getItem() == stack.getItem()
                || getEntities(stack).isEmpty())
            return;

        setEntities(stack, new ArrayList<>());
    }

    public void addEntities(ItemStack stack, UUID uuid) {
        var array = new ArrayList<>(getEntities(stack));

        array.add(uuid);

        setEntities(stack, array);
    }

    public void removeEntities(ItemStack stack, UUID uuid) {
        var array = new ArrayList<>(getEntities(stack));

        array.remove(uuid);

        setEntities(stack, array);
    }

    public void setEntities(ItemStack stack, List<UUID> list) {
        stack.set(RANDataComponentRegistry.WOLVES, list);
    }

    public List<UUID> getEntities(ItemStack stack) {
        return stack.getOrDefault(RANDataComponentRegistry.WOLVES, new ArrayList<>());
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

        PartDefinition cube_r1 = body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(37, 15).addBox(-5.501F, 0.3112F, -2.8444F, 11.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0003F, 0.2131F, 0.3331F, -0.1745F, 0.0F, 0.0F));

        PartDefinition cube_r2 = body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(34, 0).addBox(-4.5F, -2.2416F, 2.3602F, 9.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0003F, -0.0365F, 0.3386F, -0.4363F, 0.0F, 0.0F));

        PartDefinition cube_r3 = body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(77, 0).addBox(-5.5F, -1.0585F, 3.4436F, 11.0F, 24.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(9, 0).addBox(-5.5F, -0.5585F, 3.1936F, 11.0F, 24.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 36).addBox(-5.5F, -0.5585F, -2.8064F, 0.0F, 24.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(12, 36).addBox(5.5F, -0.5585F, -2.8064F, 0.0F, 24.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(3, 24).addBox(-5.5F, -0.5585F, -2.8064F, 11.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0003F, -0.0365F, 0.3386F, 0.2182F, 0.0F, 0.0F));

        PartDefinition cube_r4 = body.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(3, 30).addBox(-5.501F, 20.9365F, 11.0185F, 11.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0003F, -0.0365F, 0.3386F, -0.1309F, 0.0F, 0.0F));

        PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(24, 55).mirror().addBox(-2.0F, -2.5F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(5.751F, 2.2082F, 0.0538F));

        PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 55).addBox(-3.0F, -2.5F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.751F, 2.2082F, 0.0538F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public List<String> bodyParts() {
        return Lists.newArrayList("right_arm", "left_arm", "body");
    }

    @EventBusSubscriber
    public static class IllusionistsMantleEvent {
        @SubscribeEvent
        public static void onDeathPlayer(LivingDeathEvent event) {
            if (!(event.getEntity() instanceof Player player) || player.getCommandSenderWorld().isClientSide())
                return;

            var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.ILLUSIONISTS_MANTLE.value());

            if (!(stack.getItem() instanceof IllusionistsMantleItem relic) || relic.getEntities(stack).isEmpty())
                return;

            var level = (ServerLevel) player.getCommandSenderWorld();
            var illusion = relic.getEntities(stack).stream().map(level::getEntity).filter(Objects::nonNull).map(entity -> (EntityDummy) entity)
                    .max(Comparator.comparingDouble(entity -> entity.distanceToSqr(player)));

            if (illusion.isEmpty())
                return;

            event.setCanceled(true);

            var positionIllusion = illusion.get().position();

            illusion.get().discard();

            var illusionSecond = new EntityDummy(level);

            illusionSecond.ticksLeft = 200;
            illusionSecond.setPos(player.getX(), player.getY(), player.getZ());
            illusionSecond.setOwnerID(player.getUUID());

            level.addFreshEntity(illusionSecond);
            illusionSecond.kill();

            player.setHealth(2);
            player.teleportTo(positionIllusion.x(), positionIllusion.y, positionIllusion.z);
        }

        @SubscribeEvent
        public static void onInjuredEntity(LivingDamageEvent.Post event) {
            if (!(event.getEntity() instanceof Player player) || !(event.getSource().getEntity() instanceof LivingEntity attacker)
                    || player.getCommandSenderWorld().isClientSide())
                return;

            var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.ILLUSIONISTS_MANTLE.value());
            var random = player.getRandom();
            var level = (ServerLevel) player.getCommandSenderWorld();

            if (!(stack.getItem() instanceof IllusionistsMantleItem relic) || !relic.isAbilityUnlocked(stack, "illusion")
                    || relic.getStatValue(stack, "illusion", "chance") < random.nextFloat())
                return;

            var spawnPos = attacker.position().subtract(attacker.getLookAngle().normalize().scale(3));

            for (int i = 0; i < 10; i++) {
                if (!level.getBlockState(new BlockPos((int) spawnPos.x, (int) spawnPos.y, (int) spawnPos.z).above()).isSolid())
                    break;

                spawnPos = spawnPos.add(0, 1, 0);
            }

            var illusion = new EntityDummy(level);

            illusion.ticksLeft = (int) Math.round(relic.getStatValue(stack, "illusion", "duration") * 20);
            illusion.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            illusion.setOwnerID(player.getUUID());
            illusion.getPersistentData().putBoolean("SpawnedFromRelic", true);

            level.addFreshEntity(illusion);
            level.sendParticles(ParticleUtils.constructSimpleSpark(new Color(50 + random.nextInt(50), 150 + random.nextInt(106), 200 + random.nextInt(56)), 0.3F, 60, 0.95F),
                    illusion.getX(), illusion.getY() + 0.4, illusion.getZ(), 15, 0.1, 0.1, 0.1, 0.1);

            relic.addEntities(stack, illusion.getUUID());
            relic.spreadRelicExperience(player, stack, 1);
        }
    }
}
