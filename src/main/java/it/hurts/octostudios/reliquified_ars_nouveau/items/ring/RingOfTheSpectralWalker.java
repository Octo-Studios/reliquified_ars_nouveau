package it.hurts.octostudios.reliquified_ars_nouveau.items.ring;

import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import com.hollingsworth.arsnouveau.api.util.BlockUtil;
import com.hollingsworth.arsnouveau.common.block.tile.IntangibleAirTile;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectIntangible;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.NouveauRelicItem;
import it.hurts.sskirillss.relics.init.DataComponentRegistry;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.CastData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastStage;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastType;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.*;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemColor;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemShape;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.style.BeamsData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.VoxelShape;
import top.theillusivec4.curios.api.SlotContext;

import java.awt.*;

public class RingOfTheSpectralWalker extends NouveauRelicItem {
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("spectral")
                                .active(CastData.builder()
                                        .type(CastType.TOGGLEABLE)
                                        .build())
                                .stat(StatData.builder("duration")
                                        .initialValue(4D, 6D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.15)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatData.builder("cooldown")
                                        .initialValue(18D, 15D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, -0.05D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingData.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .sources(LevelingSourcesData.builder()
                                .source(LevelingSourceData.abilityBuilder("spectral")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.ORANGE)
                                        .build())
                                .build())
                        .build())
                .style(StyleData.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xff2d2d58)
                                .borderBottom(0xff2d2d58)
                                .textured(true)
                                .build())
                        .beams(BeamsData.builder()
                                .startColor(0xFF84fc40)
                                .endColor(0x000b222d)
                                .build())
                        .build())
                .loot(LootData.builder()
                        .entry(LootEntries.AQUATIC, LootEntries.VILLAGE)
                        .build())
                .build();
    }

    @Override
    public void castActiveAbility(ItemStack stack, Player player, String ability, CastType type, CastStage stage) {
        if (!ability.equals("spectral") || player.getCommandSenderWorld().isClientSide() || stage == CastStage.START)
            return;

        var level = (ServerLevel) player.getCommandSenderWorld();

        if (stage == CastStage.TICK && getTime(stack) <= getDuration(stack)) {

            var random = level.getRandom();

            for (VoxelShape voxelShape : level.getBlockCollisions(player, player.getBoundingBox().inflate(0.5).move(player.getKnownMovement().scale(2)))) {
                var box = voxelShape.bounds();

                if (box.maxY <= player.getBoundingBox().minY)
                    continue;

                var blockPos = new BlockPos((int) box.minX, (int) box.minY, (int) box.minZ);
                var intangible = EffectIntangible.INSTANCE;
                var state = level.getBlockState(blockPos);

                if (level.getBlockEntity(blockPos) == null && !state.isAir() && state.getBlock() != Blocks.BEDROCK && intangible.canBlockBeHarvested(new SpellStats.Builder().build(), level, blockPos)
                        && BlockUtil.destroyRespectsClaim(intangible.getPlayer(player, level), level, blockPos)) {
                    level.setBlockAndUpdate(blockPos, BlockRegistry.INTANGIBLE_AIR.defaultBlockState());

                    var tile = (IntangibleAirTile) level.getBlockEntity(blockPos);

                    if (tile == null)
                        return;

                    tile.stateID = Block.getId(state);
                    tile.maxLength = 100;

                    level.sendParticles(ParticleUtils.constructSimpleSpark(new Color(100 + random.nextInt(156), random.nextInt(100 + random.nextInt(156)), random.nextInt(100 + random.nextInt(156))), 0.3F, 60, 0.95F),
                            player.getX(), player.getY() + 0.2F, player.getZ(), 3, 0.3, 0.1, 0.3, 0.1);
                }
            }
        } else {
            if (!isAbilityOnCooldown(stack, "spectral"))
                setAbilityCooldown(stack, "spectral", (int) (getCooldownAbilities(stack) * ((double) getTime(stack) / getDuration(stack))));

            setToggled(stack, false);

            setTime(stack, 0);
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player) || player.getCommandSenderWorld().isClientSide())
            return;

        if (isAbilityTicking(stack, "spectral"))
            addTime(stack, 1);

        var level = (ServerLevel) player.getCommandSenderWorld();
        var playerBlockPos = player.blockPosition();

        if (getToggled(stack) || !isAbilityOnCooldown(stack, "spectral") ||
                !level.getBlockState(playerBlockPos.above()).is(BlockRegistry.INTANGIBLE_AIR.get()))
            return;

        var random = player.getRandom();
        var oldPos = player.position();

        for (int i = 0; i <= 15; i++) {
            int x = (int) player.getX() + (random.nextInt(i * 2 + 1) - i);
            int y = (int) (player.getY() + (random.nextInt(i * 2 + 1) - i / 2.0));
            int z = (int) player.getZ() + (random.nextInt(i * 2 + 1) - i);

            var targetPos = new BlockPos(x, y, z);

            while (targetPos.getY() > level.getMinBuildHeight() && !level.getBlockState(targetPos.below()).blocksMotion())
                targetPos = targetPos.below();

            if (!level.isEmptyBlock(targetPos.above()))
                continue;

            level.sendParticles(ParticleTypes.PORTAL, oldPos.x, oldPos.y + 1, oldPos.z, 40, -0.1F, 0, 0, 0.1);

            player.teleportTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);

            setToggled(stack, true);

            level.sendParticles(ParticleTypes.PORTAL, targetPos.getX() + 0.5, targetPos.getY() + 1, targetPos.getZ() + 0.5, 40, 0.5, 0.5, 0.5, 0.1);
        }
    }

    public int getCooldownAbilities(ItemStack stack) {
        return (int) (getStatValue(stack, "spectral", "cooldown") * 20);
    }

    public int getDuration(ItemStack stack) {
        return (int) (getStatValue(stack, "spectral", "duration") * 20);
    }

    public void setToggled(ItemStack stack, boolean val) {
        stack.set(DataComponentRegistry.TOGGLED, val);
    }

    public boolean getToggled(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.TOGGLED, false);
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
}
