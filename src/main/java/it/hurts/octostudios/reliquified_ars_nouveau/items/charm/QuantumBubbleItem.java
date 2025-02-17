package it.hurts.octostudios.reliquified_ars_nouveau.items.charm;

import com.hollingsworth.arsnouveau.common.entity.BubbleEntity;
import it.hurts.octostudios.reliquified_ars_nouveau.items.NouveauRelicItem;
import it.hurts.sskirillss.relics.init.DataComponentRegistry;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.*;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemColor;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemShape;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.sync.S2CEntityMotionPacket;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import top.theillusivec4.curios.api.SlotContext;

import java.awt.*;

public class QuantumBubbleItem extends NouveauRelicItem {
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("stasis")
                                .stat(StatData.builder("duration")
                                        .initialValue(5D, 8D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.1D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatData.builder("levitation")
                                        .initialValue(2D, 4D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.1D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatData.builder("cooldown")
                                        .initialValue(15D, 13D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, -0.035D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingData.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .sources(LevelingSourcesData.builder()
                                .source(LevelingSourceData.abilityBuilder("stasis")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.ORANGE)
                                        .build())
                                .build())
                        .build())
                .loot(LootData.builder()
                        .entry(LootEntries.AQUATIC, LootEntries.VILLAGE)
                        .build())
                .build();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player) || player.getCommandSenderWorld().isClientSide()
                || !isAbilityUnlocked(stack, "stasis"))
            return;

        var level = (ServerLevel) player.getCommandSenderWorld();

        if (getTime(stack) > getDurationAbilities(stack) && getToggled(stack)) {
            addCooldown(stack, 1);

            if (getCooldown(stack) >= getCooldownAbilities(stack)) {
                var random = level.getRandom();

                for (int i = 0; i < 100; i++) {
                    double angle = 2 * Math.PI * i / 100;
                    double x = player.getX() + 1 * Math.cos(angle);
                    double z = player.getZ() + 1 * Math.sin(angle);

                    level.sendParticles(ParticleUtils.constructSimpleSpark(new Color(random.nextInt(100), 100 + random.nextInt(156), 150 + random.nextInt(106)), 0.3F, 60, 0.95F),
                            x, player.getY() + 0.1F, z, 1, 0, 0.1, 0, 0.1);
                }

                level.playSound(null, player, SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, SoundSource.PLAYERS, 1.0F, 0.9F + player.getRandom().nextFloat() * 0.2F);

                setTime(stack, 0);
                setCooldown(stack, 0);
                setToggled(stack, false);
            }
        } else {
            if (getToggled(stack)) {
                addTime(stack, 1);

                if (player.tickCount % 15 == 0)
                    level.playSound(null, player, SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.PLAYERS, 1.0F, 0.9F + player.getRandom().nextFloat() * 0.2F);

                level.sendParticles(ParticleTypes.BUBBLE_POP, player.getX(), player.getY() + player.getBbHeight() / 2, player.getZ(), 3, 0.5, 0.5, 0.5, 0.01);
                level.sendParticles(ParticleTypes.BUBBLE, player.getX(), player.getY() + player.getBbHeight() / 2, player.getZ(), 6, 0.5, 0.5, 0.5, 0.01);
            }

            for (Projectile projectile : level.getEntitiesOfClass(Projectile.class, player.getBoundingBox().inflate(2F).move(player.getKnownMovement().scale(2F)),
                    projectile -> !(projectile instanceof BubbleEntity) && projectile.getVehicle() == null)) {
                if (projectile.getOwner() != null && projectile.getOwner().getUUID().equals(player.getUUID())
                        || projectile instanceof AbstractArrow abstractArrow && abstractArrow.inGround)
                    continue;

                setToggled(stack, true);

                var bubble = new BubbleEntity(level, (int) (getStatValue(stack, "stasis", "levitation") * 20), 0);

                bubble.setPos(projectile.getX(), projectile.getY(), projectile.getZ());
                bubble.setOwner(player);
                bubble.getPersistentData().putBoolean("canTrail", true);

                projectile.setPos(bubble.position());
                projectile.startRiding(bubble, true);

                level.addFreshEntity(bubble);
            }
        }
    }

    public int getCooldownAbilities(ItemStack stack) {
        return (int) (getStatValue(stack, "stasis", "cooldown") * 20);
    }

    public int getDurationAbilities(ItemStack stack) {
        return (int) (getStatValue(stack, "stasis", "duration") * 20);
    }

    public void setToggled(ItemStack stack, boolean val) {
        stack.set(DataComponentRegistry.TOGGLED, val);
    }

    public boolean getToggled(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.TOGGLED, false);
    }

    public void addCooldown(ItemStack stack, int time) {
        setCooldown(stack, getCooldown(stack) + time);
    }

    public int getCooldown(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.COOLDOWN, 0);
    }

    public void setCooldown(ItemStack stack, int val) {
        stack.set(DataComponentRegistry.COOLDOWN, Math.max(val, 0));
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
