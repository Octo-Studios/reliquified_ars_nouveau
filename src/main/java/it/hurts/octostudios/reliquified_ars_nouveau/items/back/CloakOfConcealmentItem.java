package it.hurts.octostudios.reliquified_ars_nouveau.items.back;

import com.google.common.collect.Lists;
import com.hollingsworth.arsnouveau.common.capability.ManaCap;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

public class CloakOfConcealmentItem extends NouveauRelicItem implements IRenderableCurio {
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("absorption")
                                .stat(StatData.builder("absorbing")
                                        .initialValue(200D, 100D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, -0.05D)
                                        .formatValue(value -> (int) MathUtils.round(value / 2, 0))
                                        .build())
                                .stat(StatData.builder("cooldown")
                                        .initialValue(60D, 30D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, -0.065)
                                        .formatValue(value -> (int) MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .build())
                .style(StyleData.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xff107087)
                                .borderBottom(0xff673824)
                                .textured(true)
                                .build())
                        .beams(BeamsData.builder()
                                .startColor(0xFFf2ee10)
                                .endColor(0x00083a64)
                                .build())
                        .build())
                .leveling(LevelingData.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .sources(LevelingSourcesData.builder()
                                .source(LevelingSourceData.abilityBuilder("absorption")
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
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!((slotContext.entity()) instanceof Player player) || player.getCommandSenderWorld().isClientSide())
            return;

        if (!getToggled(stack) && player.tickCount % 20 == 0)
            addTime(stack, 1);

        if (new ManaCap(player).getCurrentMana() <= 3 || getTime(stack) > 0)
            setToggled(stack, false);

        if (getTime(stack) >= getStatValue(stack, "absorption", "cooldown")) {
            setToggled(stack, true);
            setTime(stack, 0);

            var level = player.getCommandSenderWorld();
            var height = player.getBbHeight();
            var random = level.getRandom();

            for (double angle = 0; angle < 360; angle += 60) {
                var rad = Math.toRadians(angle);
                var lineDirection = new Vec3(Math.cos(rad), 0, Math.sin(rad));

                for (double t = -1; t <= 1; t += 0.08) {
                    var centerParticlePos = player.position().add(lineDirection.scale(1F)).add(lineDirection.scale((1 - t * t * 2) * player.getBbWidth() * 0.8)).add(0, height / 2 + t * height * 0.8, 0);

                    spawnParticle((ServerLevel) level, centerParticlePos, new Color(150 + random.nextInt(50), 50 + random.nextInt(50), 100 + random.nextInt(50)));
                }
            }

            level.playSound(null, player, SoundEvents.PUFFER_FISH_DEATH, SoundSource.PLAYERS, 1.0F, 0.9F + random.nextFloat() * 0.2F);
        }
    }

    public void addTime(ItemStack stack, int time) {
        setTime(stack, getTime(stack) + time);
    }

    public void setTime(ItemStack stack, int time) {
        stack.set(DataComponentRegistry.TIME, Math.max(time, 0));
    }

    public int getTime(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.TIME, 0);
    }

    public void setToggled(ItemStack stack, boolean val) {
        stack.set(DataComponentRegistry.TOGGLED, val);
    }

    public boolean getToggled(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.TOGGLED, true);
    }

    private static void spawnParticle(ServerLevel level, Vec3 pos, Color color) {
        level.sendParticles(ParticleUtils.constructSimpleSpark(color, 0.35F, 40, 0.9F),
                pos.x, pos.y, pos.z, 1, 0.01, 0.01, 0.01, 0.01);
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
        MeshDefinition meshdefinition = HumanoidModel.createMesh(new CubeDeformation(0.4F), 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(-0.0003F, 0.3556F, 0.0307F));

        PartDefinition cube_r1 = body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(44, 24).addBox(-5.501F, 0.3112F, -2.8444F, 11.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0003F, 0.2131F, 0.3331F, -0.1745F, 0.0F, 0.0F));

        PartDefinition cube_r2 = body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -2.2416F, 2.3602F, 9.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0003F, -0.0365F, 0.3386F, -0.4363F, 0.0F, 0.0F));

        PartDefinition cube_r3 = body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(22, 15).addBox(-5.5F, -1.0585F, 3.4436F, 11.0F, 24.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 15).addBox(-5.5F, -0.5585F, 3.1936F, 11.0F, 24.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(12, 39).addBox(-5.5F, -0.5585F, -2.8064F, 0.0F, 24.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 39).addBox(5.5F, -0.5585F, -2.8064F, 0.0F, 24.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(32, 6).addBox(-5.5F, -0.5585F, -2.8064F, 11.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0003F, -0.0365F, 0.3386F, 0.2182F, 0.0F, 0.0F));

        PartDefinition cube_r4 = body.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(32, 0).addBox(-5.501F, 20.9365F, 11.0185F, 11.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0003F, -0.0365F, 0.3386F, -0.1309F, 0.0F, 0.0F));

        PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(24, 39).addBox(-2.0F, -2.5F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(5.751F, 2.2082F, 0.0538F));

        PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(44, 12).addBox(-3.0F, -2.5F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.751F, 2.2082F, 0.0538F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public List<String> bodyParts() {
        return Lists.newArrayList("right_arm", "left_arm", "body");
    }

    @EventBusSubscriber
    public static class CloakOfConcealmentItemEvent {
        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            if (!(event.getEntity() instanceof Player player) || player.getCommandSenderWorld().isClientSide())
                return;

            var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.CLOAK_OF_CONCEALMENT.value());

            if (!(stack.getItem() instanceof CloakOfConcealmentItem relic) || !relic.isAbilityUnlocked(stack, "absorption"))
                return;

            relic.setTime(stack, 0);
            relic.setToggled(stack, true);
        }

        @SubscribeEvent
        public static void onArmorHurt(ArmorHurtEvent event) {
            if (!(event.getEntity() instanceof Player player) || player.getCommandSenderWorld().isClientSide())
                return;

            var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.CLOAK_OF_CONCEALMENT.value());

            if (!(stack.getItem() instanceof CloakOfConcealmentItem relic) || !relic.isAbilityUnlocked(stack, "absorption")
                    || !relic.getToggled(stack))
                return;

            event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onInjuredEntity(LivingDamageEvent.Pre event) {
            if (!(event.getEntity() instanceof Player player) || player.getCommandSenderWorld().isClientSide())
                return;

            var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.CLOAK_OF_CONCEALMENT.value());

            if (!(stack.getItem() instanceof CloakOfConcealmentItem relic) || !relic.isAbilityUnlocked(stack, "absorption")
                    || !relic.getToggled(stack))
                return;

            var mana = new ManaCap(player);
            var statValue = MathUtils.round(relic.getStatValue(stack, "absorption", "absorbing"), 0);

            if (mana.getCurrentMana() < statValue / 2)
                return;

            var damage = event.getNewDamage();
            var absorbanceDamage = Math.min(damage, (mana.getCurrentMana() * 2) / statValue);

            mana.removeMana(absorbanceDamage * (statValue / 2));

            event.setNewDamage((float) (damage - absorbanceDamage));

            spawnBarrierParticles(player, event.getSource().getEntity(), (ServerLevel) player.getCommandSenderWorld());

            if (damage >= 3)
                relic.spreadRelicExperience(player, stack, 1);
        }

        public static void spawnBarrierParticles(Player player, @Nullable Entity attacker, ServerLevel level) {
            var playerPos = player.position();
            var width = player.getBbWidth();
            var height = player.getBbHeight();
            var random = level.getRandom();
            var color = new Color(100 + random.nextInt(50), 50 + random.nextInt(50), 150 + random.nextInt(50));

            if (attacker != null) {
                Vec3 attackDirection = attacker.getPosition(1).subtract(playerPos).normalize();
                Vec3 perpendicularDirection = new Vec3(-attackDirection.z, 0, attackDirection.x).normalize().scale(0.4);

                for (double t = -1; t <= 1; t += 0.08) {
                    Vec3 centerParticlePos = playerPos.add(attackDirection.scale(1 - t * t * 2 * width * 0.8)).add(0, height / 2 + t * height + 1 * 0.8, 0);

                    spawnParticle(level, centerParticlePos, color);

                    if (Math.abs(t) <= 0.7) {
                        spawnParticle(level, centerParticlePos.add(perpendicularDirection), color);
                        spawnParticle(level, centerParticlePos.subtract(perpendicularDirection), color);
                    }
                }
            } else {
                for (double angle = 0; angle < 360; angle += 60) {
                    var rad = Math.toRadians(angle);
                    var lineDirection = new Vec3(Math.cos(rad), 0, Math.sin(rad));

                    for (double t = -1; t <= 1; t += 0.08) {
                        var centerParticlePos = playerPos.add(lineDirection.scale(1F)).add(lineDirection.scale(1 - t * t * 2 * width * 0.8)).add(0, height / 2 + t * height + 1 * 0.8, 0);

                        spawnParticle(level, centerParticlePos, color);
                    }
                }
            }
        }
    }
}
