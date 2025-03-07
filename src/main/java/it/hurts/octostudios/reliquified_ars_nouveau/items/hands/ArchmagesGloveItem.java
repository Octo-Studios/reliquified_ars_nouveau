package it.hurts.octostudios.reliquified_ars_nouveau.items.hands;

import com.google.common.collect.Lists;
import com.hollingsworth.arsnouveau.api.event.SpellCastEvent;
import com.hollingsworth.arsnouveau.api.event.SpellCostCalcEvent;
import com.hollingsworth.arsnouveau.api.spell.SpellCaster;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster;
import com.hollingsworth.arsnouveau.api.util.SpellUtil;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentSensitive;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArchmagesGloveItem extends NouveauRelicItem implements IRenderableCurio {
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("multicasted")
                                .stat(StatData.builder("chance")
                                        .initialValue(0.15D, 0.25D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.25)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingData.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .sources(LevelingSourcesData.builder()
                                .source(LevelingSourceData.abilityBuilder("multicasted")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.BLUE)
                                        .build())
                                .build())
                        .build())
                .style(StyleData.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xff2d2d58)
                                .borderBottom(0xff2d2d58)
                                .build())
                        .beams(BeamsData.builder()
                                .startColor(0xFFef3398)
                                .endColor(0x00c31560)
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
                || !stack.has(RANDataComponentRegistry.MULTICASTED))
            return;

        var multicastedListComponent = getListMulticasted(stack);

        if (multicastedListComponent == null || multicastedListComponent.isEmpty())
            return;

        for (int i = 0; i < multicastedListComponent.size(); i++) {
            var multicastedComponent = multicastedListComponent.get(i);
            var spellCaster = multicastedComponent.spellCaster();
            var tickSpell = multicastedComponent.tickSpell();
            var multicastCount = multicastedComponent.multicastCount();

            if (multicastCount == 0) {
                List<MulticastedComponent> newList = new ArrayList<>(multicastedListComponent);
                newList.remove(i);

                setListMulticasted(stack, newList);

                return;
            }

            multicastedListComponent.set(i, new MulticastedComponent(multicastCount, tickSpell - 1, spellCaster));

            if (tickSpell == 0) {
                onCasted(stack, player, player.getCommandSenderWorld(), player.getUsedItemHand(), spellCaster);
                multicastedListComponent.set(i, new MulticastedComponent(multicastCount - 1, 4, spellCaster));
            }
        }
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player) || prevStack.getItem() == stack.getItem()
                || !stack.has(RANDataComponentRegistry.MULTICASTED))
            return;

        setListMulticasted(stack, Collections.emptyList());
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

        PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.5F, 1.0F, 0.25F));

        PartDefinition perchatka = right_arm.addOrReplaceChild("perchatka", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -2.5F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 11).addBox(0.0F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(16, 11).addBox(3.0F, 0.5F, -3.5F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.005F)), PartPose.offset(-3.0F, 7.7F, -0.25F));

        PartDefinition bone = perchatka.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7854F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public List<String> bodyParts() {
        return Lists.newArrayList("right_arm");
    }

    public void onCasted(ItemStack stack, Player player, Level level, InteractionHand handIn, SpellCaster spellCaster) {
        var wrappedCaster = new PlayerCaster(player);
        var resolver = spellCaster.getSpellResolver(new SpellContext(level, spellCaster.getSpell(), player, wrappedCaster, stack), level, player, handIn);

        resolver.spellContext.setCasterTool(stack);

        var isSensitive = resolver.spell.getBuffsAtIndex(0, player, AugmentSensitive.INSTANCE) > 0;

        HitResult result = SpellUtil.rayTrace(player, (double) 0.5F + player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).getValue(), 0.0F, isSensitive);

        if (result instanceof EntityHitResult entityHitResult)
            if (resolver.onCastOnEntity(stack, entityHitResult.getEntity(), handIn))
                spellCaster.playSound(player.getOnPos(), level, player, spellCaster.getCurrentSound(), SoundSource.PLAYERS);

        if (result instanceof BlockHitResult) {
            if (resolver.onCast(stack, level))
                spellCaster.playSound(player.getOnPos(), level, player, spellCaster.getCurrentSound(), SoundSource.PLAYERS);
        }
    }

    public List<MulticastedComponent> getListMulticasted(ItemStack stack) {
        return stack.get(RANDataComponentRegistry.MULTICASTED);
    }

    public void setListMulticasted(ItemStack stack, List<MulticastedComponent> multicasted) {
        stack.set(RANDataComponentRegistry.MULTICASTED, multicasted);
    }

    @EventBusSubscriber
    public static class ArchmagesGloveEvent {
        @SubscribeEvent
        public static void onCostMana(SpellCostCalcEvent event) {
            if (!(event.context.getCaster() instanceof LivingCaster livingEntity))
                return;

            var entity = livingEntity.livingEntity;
            var stack = EntityUtils.findEquippedCurio(entity, ItemRegistry.ARCHMAGES_GLOVE.value());

            if (!(stack.getItem() instanceof ArchmagesGloveItem relic) || !event.context.getCasterTool().is(ItemRegistry.ARCHMAGES_GLOVE))
                return;

            event.currentCost = 0;

            if (livingEntity.livingEntity.getCommandSenderWorld().isClientSide())
                return;

            var level = entity.getCommandSenderWorld();
            var random = level.getRandom();

            level.playSound(null, entity, SoundEvents.ALLAY_ITEM_TAKEN, SoundSource.PLAYERS, 1F, 0.9F + random.nextFloat() * 0.2F);

            ((ServerLevel) level).sendParticles(ParticleUtils.constructSimpleSpark(new Color(100 + random.nextInt(156), random.nextInt(100 + random.nextInt(156)), random.nextInt(100 + random.nextInt(156))), 0.3F, 60, 0.95F),
                    entity.getX(), entity.getY() + 0.4, entity.getZ(), 30, 0.1, 0.1, 0.1, 0.1);
        }

        @SubscribeEvent
        public static void onCastSpell(SpellCastEvent event) {
            if (!(event.context.getCaster() instanceof LivingCaster livingEntity) || !(livingEntity.livingEntity instanceof Player player)
                    || event.context.getSpell().color().getColor() != 16718260)
                return;

            var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.ARCHMAGES_GLOVE.value());

            if (!(stack.getItem() instanceof ArchmagesGloveItem relic) || !relic.isAbilityUnlocked(stack, "multicasted")
                    || event.context.getCasterTool().is(ItemRegistry.ARCHMAGES_GLOVE))
                return;

            var multicast = Math.min(5, MathUtils.multicast(player.getRandom(), relic.getStatValue(stack, "multicasted", "chance")));

            if (multicast == 0)
                return;

            relic.spreadRelicExperience(player, stack, multicast);

            List<MulticastedComponent> lists = new ArrayList<>(relic.getListMulticasted(stack) == null ? Collections.emptyList() : relic.getListMulticasted(stack));

            lists.add(new MulticastedComponent(multicast, 4, new SpellCaster().setSpell(event.context.getSpell())));

            relic.setListMulticasted(stack, lists);
        }
    }
}
