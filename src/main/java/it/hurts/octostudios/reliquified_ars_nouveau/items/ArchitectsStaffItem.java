package it.hurts.octostudios.reliquified_ars_nouveau.items;

import com.hollingsworth.arsnouveau.client.particle.ParticleColor;
import com.hollingsworth.arsnouveau.common.block.MageBlock;
import com.hollingsworth.arsnouveau.common.block.tile.MageBlockTile;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.base.loot.LootEntries;
import it.hurts.sskirillss.relics.init.CreativeTabRegistry;
import it.hurts.sskirillss.relics.init.DataComponentRegistry;
import it.hurts.sskirillss.relics.items.misc.CreativeContentConstructor;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.*;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemColor;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemShape;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.BeamsData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleData;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;

import java.awt.*;

public class ArchitectsStaffItem extends NouveauRelicItem {
    private Vec3 startPos = Vec3.ZERO;

    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("designer")
                                .stat(StatData.builder("periodicity")
                                        .initialValue(20D, 15D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, -0.05D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatData.builder("count")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(UpgradeOperation.ADD, 0.5D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(StatData.builder("distance")
                                        .initialValue(16D, 32D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.275D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingData.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .sources(LevelingSourcesData.builder()
                                .source(LevelingSourceData.abilityBuilder("designer")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.CYAN)
                                        .build())
                                .build())
                        .build())
                .style(StyleData.builder()
//                        .tooltip(TooltipData.builder()
//                                .borderTop(0xffdda524)
//                                .borderBottom(0xffdda524)
//                                .textured(true)
//                                .build())
                        .beams(BeamsData.builder()
                                .startColor(0xFFfcfc88)
                                .endColor(0x00c31560)
                                .build())
                        .build())
                .loot(LootData.builder()
                        .entry(LootEntries.ARS_NOUVEAU, LootEntries.ARS_NOUVEAU_LIKE)
                        .build())
                .build();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        var stack = player.getItemInHand(usedHand);

        if (level.isClientSide() || getCharge(stack) < 1)
            return InteractionResultHolder.pass(stack);

        var view = player.getViewVector(0);
        var eyeVec = player.getEyePosition(0);

        var distance = (int) Math.round(getStatValue(stack, "designer", "distance"));
        var ray = level.clip(new ClipContext(eyeVec, eyeVec.add(view.x * distance, view.y * distance, view.z * distance), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

        this.startPos = Vec3.atLowerCornerOf(ray.getBlockPos());

        player.startUsingItem(usedHand);

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        if (!(livingEntity instanceof Player player) || level.isClientSide())
            return;

        var view = player.getViewVector(0);
        var eyeVec = player.getEyePosition(0);

        var distance = 35;
        var ray = level.clip(new ClipContext(eyeVec, eyeVec.add(view.x * distance, view.y * distance, view.z * distance), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

        var start = BlockPos.containing(startPos);
        var end = ray.getBlockPos();

        int steps = Math.max(Math.max(Math.abs(end.getX() - start.getX()), Math.abs(end.getY() - start.getY())), Math.abs(end.getZ() - start.getZ()));
        var color = ParticleColor.makeRandomColor(255, 255, 255, player.getRandom());

        for (int i = 0; i <= steps; i++) {
            var t = i / (double) steps;

            var blockPos = new BlockPos((int) Mth.lerp(t, start.getX(), end.getX()), (int) Mth.lerp(t, start.getY(), end.getY()), (int) Mth.lerp(t, start.getZ(), end.getZ()));

            if (level.getBlockState(blockPos).isSolid())
                continue;

            level.setBlockAndUpdate(blockPos, BlockRegistry.MAGE_BLOCK.get().defaultBlockState().setValue(MageBlock.TEMPORARY, true));

            if (level.getBlockEntity(blockPos) instanceof MageBlockTile tile) {
                tile.color = color;
                tile.lengthModifier = (1200 - 300) / 100.0;
                tile.isPermanent = false;

                var centerBlock = blockPos.getCenter();

                level.playSound(null, player, SoundEvents.AMETHYST_CLUSTER_STEP, SoundSource.PLAYERS, 0.75F, 0.7F + player.getRandom().nextFloat() * 0.2F);
                level.sendBlockUpdated(blockPos, level.getBlockState(blockPos), level.getBlockState(blockPos), 2);
                ((ServerLevel) level).sendParticles(ParticleUtils.constructSimpleSpark(new Color(color.getColor()), 0.3F, 70, 0.9F),
                        centerBlock.x(), centerBlock.y(), centerBlock.z(), 5, 0.1, 0.1, 0.1, 0.1);
            }
        }

        spreadRelicExperience(player, stack, 1);

        addCharges(stack, -1);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!(entity instanceof Player player) || getCharge(stack) > Math.round(getStatValue(stack, "designer", "count")))
            return;

        var periodicity = (int) Math.round(getStatValue(stack, "designer", "periodicity")) * 20;

        addTime(stack, 1);

        if (getTime(stack) >= periodicity) {
            addCharges(stack, 1);

            setTime(stack, 0);
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getCharge(stack) < getMaxCharges(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        float max = getMaxCharges(stack);
        float current = getCharge(stack);
        return Math.round(13.0F * current / max);
    }

    @Override
    public void gatherCreativeTabContent(CreativeContentConstructor constructor) {
        ItemStack stack = this.getDefaultInstance();

        setCharge(stack, (int) Math.round(getStatValue(stack, "designer", "count")));

        constructor.entry(CreativeTabRegistry.RELICS_TAB.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS, stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Mth.hsvToRgb((float) getCharge(stack) / getMaxCharges(stack) / 3.0F, 1.0F, 1.0F);
    }

    public int getMaxCharges(ItemStack stack) {
        return (int) Math.round(getStatValue(stack, "designer", "count"));
    }

    public int getCharge(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.CHARGE, 0);
    }

    public void setCharge(ItemStack stack, int charge) {
        stack.set(DataComponentRegistry.CHARGE, Math.max(charge, 0));
    }

    public void addCharges(ItemStack stack, int charge) {
        setCharge(stack, getCharge(stack) + charge);
    }

    public void addTime(ItemStack stack, int time) {
        setTime(stack, getTime(stack) + time);
    }

    public int getTime(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.TIME, 0);
    }

    public void setTime(ItemStack stack, int val) {
        stack.set(DataComponentRegistry.TIME, Math.max(val, 0));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return false;
    }
}
