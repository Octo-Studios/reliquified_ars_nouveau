package it.hurts.octostudios.reliquified_ars_nouveau.items.back;

import com.hollingsworth.arsnouveau.common.entity.EntityDummy;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.NouveauRelicItem;
import it.hurts.octostudios.reliquified_ars_nouveau.items.base.loot.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.*;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemColor;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemShape;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.BeamsData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleData;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import top.theillusivec4.curios.api.SlotContext;

import java.awt.*;
import java.util.List;
import java.util.*;

public class IllusionistsMantleItem extends NouveauRelicItem {
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
//                        .tooltip(TooltipData.builder()
//                                .borderTop(0xff107087)
//                                .borderBottom(0xff673824)
//                                .textured(true)
//                                .build())
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

            player.setHealth(2);
            player.teleportTo(positionIllusion.x(), positionIllusion.y, positionIllusion.z);
            level.playSound(null, player, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.5F, 0.9F + player.getRandom().nextFloat() * 0.2F);
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
                    illusion.getX(), illusion.getY() + 0.4, illusion.getZ(), 30, 0.1, 0.1, 0.1, 0.1);

            relic.addEntities(stack, illusion.getUUID());
            relic.spreadRelicExperience(player, stack, 1);
        }
    }
}
