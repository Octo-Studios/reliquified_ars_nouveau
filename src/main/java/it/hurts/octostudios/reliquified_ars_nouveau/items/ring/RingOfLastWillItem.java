package it.hurts.octostudios.reliquified_ars_nouveau.items.ring;

import com.hollingsworth.arsnouveau.common.network.Networking;
import com.hollingsworth.arsnouveau.common.network.PacketClientRewindEffect;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.NouveauRelicItem;
import it.hurts.octostudios.reliquified_ars_nouveau.items.base.loot.LootEntries;
import it.hurts.sskirillss.relics.init.DataComponentRegistry;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import top.theillusivec4.curios.api.SlotContext;

import java.awt.*;

public class RingOfLastWillItem extends NouveauRelicItem {
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("hibernation")
                                .stat(StatData.builder("cooldown")
                                        .initialValue(60D, 50D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, -0.05)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingData.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .sources(LevelingSourcesData.builder()
                                .source(LevelingSourceData.abilityBuilder("hibernation")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.RED)
                                        .build())
                                .build())
                        .build())
                .style(StyleData.builder()
//                        .tooltip(TooltipData.builder()
//                                .borderTop(0xff607080)
//                                .borderBottom(0xff607080)
//                                .textured(true)
//                                .build())
                        .beams(BeamsData.builder()
                                .startColor(0xFFbf022b)
                                .endColor(0x00fc31b8)
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
                || getCooldown(stack) == 0)
            return;

        consumeCooldown(stack, 1);

        if (getCooldown(stack) == 0) {
            var level = (ServerLevel) player.getCommandSenderWorld();
            var random = level.getRandom();

            for (int i = 0; i < 100; i++) {
                double angle = 2 * Math.PI * i / 100;

                level.sendParticles(ParticleUtils.constructSimpleSpark(new Color(100 + random.nextInt(156), random.nextInt(100 + random.nextInt(156)), random.nextInt(100 + random.nextInt(156))), 0.3F, 60, 0.95F),
                        player.getX() + 1 * Math.cos(angle), player.getY() + 0.1F, player.getZ() + 1 * Math.sin(angle), 1, 0, 0.1, 0, 0.1);

                level.playSound(null, player, SoundEvents.TRIAL_SPAWNER_SPAWN_MOB, SoundSource.PLAYERS, 0.3F, 0.9F + random.nextFloat() * 0.2F);
            }
        }
    }

    public void consumeCooldown(ItemStack stack, int time) {
        setCooldown(stack, getCooldown(stack) - time);
    }

    public void setCooldown(ItemStack stack, int cooldown) {
        stack.set(DataComponentRegistry.COOLDOWN, Math.max(cooldown, 0));
    }

    public int getCooldown(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.COOLDOWN, 0);
    }

    @EventBusSubscriber
    public static class RingOfLastWillEvent {
        @SubscribeEvent
        public static void onDeathPlayer(LivingDeathEvent event) {
            if (!(event.getEntity() instanceof Player player) || player.getCommandSenderWorld().isClientSide())
                return;

            for (var stack : EntityUtils.findEquippedCurios(player, ItemRegistry.RING_OF_LAST_WILL.value())) {
                if (!(stack.getItem() instanceof RingOfLastWillItem relic) || !relic.isAbilityUnlocked(stack, "hibernation")
                        || relic.getCooldown(stack) > 0)
                    continue;

                var level = (ServerLevel) player.getCommandSenderWorld();
                var random = level.getRandom();

                level.playSound(null, player, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 0.9F + random.nextFloat() * 0.2F);

                player.setHealth(2);

                relic.setCooldown(stack, (int) MathUtils.round(relic.getStatValue(stack, "hibernation", "cooldown") * 20, 0));
                relic.spreadRelicExperience(player, stack, 1);

                Networking.sendToNearbyClient(level, player, new PacketClientRewindEffect(60, player));

                event.setCanceled(true);

                for (int i = 0; i < 50; i++)
                    level.sendParticles(ParticleUtils.constructSimpleSpark(new Color(100 + random.nextInt(156), random.nextInt(100 + random.nextInt(156)), random.nextInt(100 + random.nextInt(156))), 0.5F, 60, 0.95F),
                            player.getX(), player.getY() + 1.0, player.getZ(), 1, (random.nextDouble() - 0.5) * 3.0, random.nextDouble() * 1.5, (random.nextDouble() - 0.5) * 3.0, 0.05);

                break;
            }
        }
    }
}
