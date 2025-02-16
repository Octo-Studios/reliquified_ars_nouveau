package it.hurts.octostudios.reliquified_ars_nouveau.items.ring;

import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import com.hollingsworth.arsnouveau.api.util.BlockUtil;
import com.hollingsworth.arsnouveau.common.block.tile.IntangibleAirTile;
import com.hollingsworth.arsnouveau.common.capability.ManaCap;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectIntangible;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
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
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.PacketPlayerMotion;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import it.hurts.sskirillss.relics.utils.data.WorldPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import top.theillusivec4.curios.api.SlotContext;

import java.awt.*;

public class RingOfTheSpectralWalker extends NouveauRelicItem {
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("spectral")
                                .active(CastData.builder()
                                        .type(CastType.CYCLICAL)
                                        .build())
                                .stat(StatData.builder("manacost")
                                        .initialValue(30D, 25D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, -0.04)
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
        if (!ability.equals("spectral") || player.getCommandSenderWorld().isClientSide())
            return;

        var level = player.getCommandSenderWorld();
        var playerBlockPos = player.blockPosition();
        var playerBlockState = level.getBlockState(playerBlockPos);
        var playerBlockStateAbove = level.getBlockState(playerBlockPos.above());
        var currentMana = new ManaCap(player).getCurrentMana();

        if (stage == CastStage.START && currentMana <= getManacost(stack))
            setToggled(stack, false);

        if (stage == CastStage.TICK && getToggled(stack) && currentMana >= getManacostInTick(stack)) {
            if ((playerBlockState.isAir() || !playerBlockState.getFluidState().isEmpty()) && (playerBlockStateAbove.isAir() || !playerBlockStateAbove.getFluidState().isEmpty())
                    && !playerBlockStateAbove.is(BlockRegistry.INTANGIBLE_AIR.get()))
                setPosition(stack, new WorldPosition(player));

            player.fallDistance = 0;

            if (playerBlockStateAbove.is(BlockRegistry.INTANGIBLE_AIR.get())) {
                var vec = player.getLookAngle().scale(0.4);

                if (vec.y >= 0.2)
                    vec = vec.scale(1.9);

                NetworkHandler.sendToClient(new PacketPlayerMotion(vec.x(), vec.y(), vec.z()), (ServerPlayer) player);

                player.startAutoSpinAttack(5, 0F, ItemStack.EMPTY);
            }

            var random = level.getRandom();

            for (VoxelShape voxelShape : level.getBlockCollisions(player, player.getBoundingBox().inflate(0.5).move(player.getKnownMovement().scale(2)))) {
                var box = voxelShape.bounds();
                var blockPos = new BlockPos((int) box.minX, (int) box.minY, (int) box.minZ);
                var state = level.getBlockState(blockPos);
                var intangible = EffectIntangible.INSTANCE;

                if (level.getBlockEntity(blockPos) == null && !state.isAir() && state.getBlock() != Blocks.BEDROCK && intangible.canBlockBeHarvested(new SpellStats.Builder().build(), level, blockPos)
                        && BlockUtil.destroyRespectsClaim(intangible.getPlayer(player, (ServerLevel) level), level, blockPos)) {
                    level.setBlockAndUpdate(blockPos, BlockRegistry.INTANGIBLE_AIR.defaultBlockState());

                    var tile = (IntangibleAirTile) level.getBlockEntity(blockPos);

                    if (tile == null)
                        return;

                    tile.stateID = Block.getId(state);
                    tile.maxLength = level.getBlockState(blockPos.above()).isAir() ? 5 : 20;

                    ((ServerLevel) level).sendParticles(ParticleUtils.constructSimpleSpark(new Color(100 + random.nextInt(156), random.nextInt(100 + random.nextInt(156)), random.nextInt(100 + random.nextInt(156))), 0.2F, 60, 0.95F),
                            player.getX(), player.getY() + 0.2F, player.getZ(), 3, 0.3, 0.1, 0.3, 0.1);
                }
            }
        } else {
            if (!playerBlockStateAbove.getCollisionShape(level, playerBlockPos.above()).isEmpty() || playerBlockStateAbove.is(BlockRegistry.INTANGIBLE_AIR.get())) {
                var targetPos = getPosition(stack).getPos();

                player.teleportTo(targetPos.x() + 0.5, targetPos.y() + player.getBbHeight(), targetPos.z() + 0.5);

                setToggled(stack, false);
            }
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player) || player.getCommandSenderWorld().isClientSide()
                || !isAbilityTicking(stack, "spectral"))
            return;

        var mana = new ManaCap(player);

        if (!getToggled(stack)) {
            if (mana.getCurrentMana() > getManacost(stack))
                setToggled(stack, true);
        } else
            mana.removeMana(getManacostInTick(stack));
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (newStack.getItem() == stack.getItem())
            return;

        setPosition(stack, null);
    }

    public double getManacostInTick(ItemStack stack) {
        return (getStatValue(stack, "spectral", "manacost")) / 20;
    }

    public int getManacost(ItemStack stack) {
        return (int) (getStatValue(stack, "spectral", "manacost"));
    }

    public void setPosition(ItemStack stack, WorldPosition val) {
        stack.set(DataComponentRegistry.WORLD_POSITION, val);
    }

    public WorldPosition getPosition(ItemStack stack) {
        return stack.get(DataComponentRegistry.WORLD_POSITION);
    }

    public void setToggled(ItemStack stack, boolean val) {
        stack.set(DataComponentRegistry.TOGGLED, val);
    }

    public boolean getToggled(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.TOGGLED, true);
    }

    @EventBusSubscriber
    public static class RingOfTheSpectralWalkerEvent {
        @SubscribeEvent
        public static void onLivingDeath(LivingIncomingDamageEvent event) {
            if (!(event.getEntity() instanceof Player player) || player.getCommandSenderWorld().isClientSide())
                return;

            var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.RING_OF_THE_SPECTRAL_WALKER.value());
            System.out.println(event.getSource().type());
            if (!(stack.getItem() instanceof RingOfTheSpectralWalker relic) || !relic.isAbilityTicking(stack, "spectral")
                    || !event.getSource().is(DamageTypes.IN_WALL))
                return;

            event.setCanceled(true);
        }
    }
}
