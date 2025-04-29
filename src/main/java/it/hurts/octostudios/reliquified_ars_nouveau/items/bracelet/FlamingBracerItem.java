package it.hurts.octostudios.reliquified_ars_nouveau.items.bracelet;

import com.google.common.collect.Lists;
import com.hollingsworth.arsnouveau.api.spell.Spell;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import com.hollingsworth.arsnouveau.api.spell.SpellResolver;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.client.particle.ParticleUtil;
import com.hollingsworth.arsnouveau.common.entity.Cinder;
import com.hollingsworth.arsnouveau.common.items.curios.ShapersFocus;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
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
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.util.List;

public class FlamingBracerItem extends NouveauRelicItem implements IRenderableCurio {
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("resistance")
                                .maxLevel(0)
                                .build())
                        .ability(AbilityData.builder("pyroclastic")
                                .stat(StatData.builder("chance")
                                        .initialValue(0.1D, 0.3D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.2)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 1))
                                        .build())
                                .stat(StatData.builder("count")
                                        .initialValue(2D, 4D)
                                        .upgradeModifier(UpgradeOperation.ADD, 0.5D)
                                        .formatValue(value -> (int) MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .build())
                .style(StyleData.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xffb6730f)
                                .borderBottom(0xffb6730f)
                                .textured(true)
                                .build())
                        .beams(BeamsData.builder()
                                .startColor(0xFFff2a0c)
                                .endColor(0x00e7ba2e)
                                .build())
                        .build())
                .leveling(LevelingData.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .sources(LevelingSourcesData.builder()
                                .source(LevelingSourceData.abilityBuilder("pyroclastic")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.ORANGE)
                                        .build())
                                .build())
                        .build())
                .loot(LootData.builder()
                        .entry(LootEntries.ARS_NOUVEAU, LootEntries.ARS_NOUVEAU_LIKE)
                        .build())
                .build();
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

        VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(renderTypeBuffer, RenderType.armorCutoutNoCull(getTexture(stack)), stack.hasFoil());

        model.renderToBuffer(matrixStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY);

        matrixStack.popPose();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LayerDefinition constructLayerDefinition() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.4F), 0.0F);
        PartDefinition partdefinition = mesh.getRoot();

        if (isSlim()) {
            PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(1, 0).addBox(-2.5F, 1.0F, -2.5F, 4.0F, 5.0F, 5.0F, new CubeDeformation(0.2F))
                    .texOffs(1, 10).addBox(-2.5F, 0.75F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.4F))
                    .texOffs(1, 17).addBox(-2.5F, 4.25F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.4F)), PartPose.offset(-5.0F, 2.5F, 0.0F));

            PartDefinition cube_r1 = right_arm.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(20, 4).addBox(-1.625F, 0.2374F, 0.2374F, 1.0F, 2.0F, 2.0F, new CubeDeformation(-0.1F))
                    .texOffs(20, 0).addBox(-1.625F, -2.2374F, -2.2374F, 1.0F, 2.0F, 2.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(-1.625F, 3.5F, 0.0F, -0.7854F, 0.0F, 0.0F));
        } else {
            PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, 1.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.2F))
                    .texOffs(0, 10).addBox(-3.5F, 0.75F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.4F))
                    .texOffs(0, 17).addBox(-3.5F, 4.25F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.4F)), PartPose.offset(-5.0F, 2.5F, 0.0F));

            PartDefinition cube_r1 = right_arm.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(20, 4).addBox(-1.875F, 0.2374F, 0.2374F, 1.0F, 2.0F, 2.0F, new CubeDeformation(-0.1F))
                    .texOffs(20, 0).addBox(-1.875F, -2.2374F, -2.2374F, 1.0F, 2.0F, 2.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(-2.375F, 3.5F, 0.0F, -0.7854F, 0.0F, 0.0F));
        }

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public ResourceLocation getTexture(ItemStack stack) {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        var suffix = isSlim() ? "_slim" : "_wide";

        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "textures/item/model/" + id.getPath() + suffix + ".png");
    }

    @OnlyIn(Dist.CLIENT)
    private boolean isSlim() {
        return Minecraft.getInstance().getSkinManager().getInsecureSkin(Minecraft.getInstance().getGameProfile()).model() == PlayerSkin.Model.SLIM;
    }

    @Override
    public List<String> bodyParts() {
        return Lists.newArrayList("right_arm");
    }

    @EventBusSubscriber
    public static class FlamingBracerItemEvent {
        @SubscribeEvent
        public static void onPlayerAttacking(AttackEntityEvent event) {
            var player = event.getEntity();
            var target = event.getTarget();

            if (!target.isAlive())
                return;

            var level = player.getCommandSenderWorld();

            for (var stack : EntityUtils.findEquippedCurios(player, ItemRegistry.FLAMING_BRACER.value())) {
                if (player.getCommandSenderWorld().isClientSide() || !(stack.getItem() instanceof FlamingBracerItem relic))
                    continue;

                var random = level.getRandom();

                if (!relic.isAbilityUnlocked(stack, "pyroclastic") || !target.isOnFire() || player.getAttackStrengthScale(0.5F) < 0.9F)
                    continue;

                relic.spreadRelicExperience(player, stack, 1);

                var context = new SpellContext(player.level(), new Spell(), player, new LivingCaster(player));
                var hit = target.getPosition(1);
                var resolver = new SpellResolver(context);

                var fireCount = Math.min(relic.getStatValue(stack, "pyroclastic", "count"), MathUtils.multicast(random, relic.getStatValue(stack, "pyroclastic", "chance")));

                for (int i = 0; i < fireCount; i++) {
                    var vec3 = new Vec3(hit.x() - Math.sin(random.nextInt(360)), hit.y(), hit.z() - Math.cos(random.nextInt(360)));
                    var fallingBlock = new Cinder(level, vec3.x(), vec3.y() + target.getBbHeight() / 2, vec3.z(), BlockRegistry.MAGIC_FIRE.defaultBlockState(), resolver);

                    fallingBlock.setDeltaMovement(vec3.x() - hit.x(), ParticleUtil.inRange(0.3, 0.5), vec3.z() - hit.z());
                    fallingBlock.setDeltaMovement(fallingBlock.getDeltaMovement().multiply(new Vec3(ParticleUtil.inRange(0.1, 0.5), 1, ParticleUtil.inRange(0.1, 0.5))));
                    fallingBlock.dropItem = false;
                    fallingBlock.hurtEntities = false;
                    fallingBlock.shooter = player;
                    fallingBlock.setOwner(player);
                    fallingBlock.getPersistentData().putBoolean("canTrail", true);

                    level.addFreshEntity(fallingBlock);

                    ShapersFocus.tryPropagateEntitySpell(fallingBlock, level, player, context, resolver);
                }
            }
        }
    }
}
