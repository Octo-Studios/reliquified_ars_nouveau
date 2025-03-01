package it.hurts.octostudios.reliquified_ars_nouveau.items.charm;

import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.ScribbleRelicItem;
import it.hurts.octostudios.reliquified_ars_nouveau.items.base.loot.LootEntries;
import it.hurts.sskirillss.relics.init.DataComponentRegistry;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.*;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemColor;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemShape;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import top.theillusivec4.curios.api.SlotContext;

import java.awt.*;

public class EmblemOfAssaultItem extends ScribbleRelicItem {
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("effort")
                                .stat(StatData.builder("cooldown")
                                        .initialValue(25D, 20D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, -0.025D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingData.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .sources(LevelingSourcesData.builder()
                                .source(LevelingSourceData.abilityBuilder("effort")
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
        if (!(slotContext.entity() instanceof Player player) || player.getCommandSenderWorld().isClientSide()
                || getTime(stack) == 0)
            return;

        var random = player.getRandom();
        var level = (ServerLevel) player.getCommandSenderWorld();

        consumeTime(stack, 1);

        if (getTime(stack) == 0) {
            for (int i = 0; i < 100; i++) {
                double angle = 2 * Math.PI * i / 100;
                double x = player.getX() + 1 * Math.cos(angle);
                double z = player.getZ() + 1 * Math.sin(angle);

                level.playSound(null, player, SoundEvents.ILLUSIONER_PREPARE_BLINDNESS, SoundSource.PLAYERS, 0.5F, 0.9F + random.nextFloat() * 0.2F);

                level.sendParticles(ParticleUtils.constructSimpleSpark(new Color(150 + random.nextInt(100), 0, 50 + random.nextInt(50)), 0.3F, 60, 0.95F),
                        x, player.getY() + player.getBbHeight() / 2, z, 1, 0, 0.1, 0, 0.1);
            }
        }
    }

    public void consumeTime(ItemStack stack, int time) {
        setTime(stack, getTime(stack) - time);
    }

    public void setTime(ItemStack stack, int time) {
        stack.set(DataComponentRegistry.TIME, Math.max(time, 0));
    }

    public int getTime(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.TIME, 0);
    }

    @EventBusSubscriber
    public static class EmblemOfAssaultEvent {
        @SubscribeEvent
        public static void onAttacked(AttackEntityEvent event) {
            var player = event.getEntity();

            if (player.getCommandSenderWorld().isClientSide() || !(event.getTarget() instanceof LivingEntity target)
                    || target.getUUID().equals(player.getUUID()) || player.getAttackStrengthScale(0.5F) < 0.9F)
                return;

            for (var stack : EntityUtils.findEquippedCurios(player, ItemRegistry.EMBLEM_OF_ASSAULT.value())) {
                if (!(stack.getItem() instanceof EmblemOfAssaultItem relic) || relic.getTime(stack) != 0 || !relic.isAbilityUnlocked(stack, "effort")
                        || relic.getSpellList(relic.getSpellCaster(stack).getSpell().recipe()).isEmpty())
                    continue;


                relic.spreadRelicExperience(player, stack, 1);
                relic.setTime(stack, (int) (relic.getStatValue(stack, "effort", "cooldown") * 20));
                relic.onAutoCastedSpell(player, target, stack, new Color(255, 0, 100));
            }
        }
    }
}
