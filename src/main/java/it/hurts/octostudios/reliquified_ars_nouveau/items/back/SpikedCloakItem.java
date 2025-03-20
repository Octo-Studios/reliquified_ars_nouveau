package it.hurts.octostudios.reliquified_ars_nouveau.items.back;

import com.hollingsworth.arsnouveau.common.entity.EntityChimeraProjectile;
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
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
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
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import top.theillusivec4.curios.api.SlotContext;

import java.awt.*;

public class SpikedCloakItem extends NouveauRelicItem {
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("spikes")
                                .stat(StatData.builder("threshold")
                                        .initialValue(0.1D, 0.3D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.1)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatData.builder("damage")
                                        .initialValue(2D, 5D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.2)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(StatData.builder("time")
                                        .initialValue(8D, 12D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.15)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingData.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .sources(LevelingSourcesData.builder()
                                .source(LevelingSourceData.abilityBuilder("spikes")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.ORANGE)
                                        .build())
                                .build())
                        .build())
                .style(StyleData.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xff85543c)
                                .borderBottom(0xff85543c)
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
                || !isAbilityUnlocked(stack, "spikes"))
            return;

        if (getTime(stack) > 0)
            consumeTime(stack, 1);

        if (getTime(stack) <= 0)
            setCount(stack, 1);

        if (getCharges(stack) >= getActualActivatedHP(player, stack)) {
            var level = (ServerLevel) player.getCommandSenderWorld();
            var random = player.getRandom();

            for (int i = 0; i < 35; i++) {
                var modifier = 0.5F;
                var spike = new EntityChimeraProjectile(level);

                spike.setPos(player.getX(), player.getY(), player.getZ());
                spike.setOwner(player);
                spike.setDeltaMovement(MathUtils.randomFloat(random) * modifier, random.nextFloat() * modifier + 0.25F, MathUtils.randomFloat(random) * modifier);

                level.addFreshEntity(spike);
            }

            setCharges(stack, 0);

            ((ServerLevel) player.getCommandSenderWorld()).sendParticles(ParticleUtils.constructSimpleSpark(new Color(150 + random.nextInt(100), 100 + random.nextInt(80), 50 + random.nextInt(50)), 0.2F, 20, 0.85F),
                    player.getX(), player.getY(), player.getZ(), 50, 0.5, 1, 0.5, 0.1);
        }
    }

    public int getActualActivatedHP(Player player, ItemStack stack) {
        return (int) (player.getMaxHealth() * (1 - getStatValue(stack, "spikes", "threshold")));
    }

    public int getCharges(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.CHARGE, 0);
    }

    public void setCharges(ItemStack stack, int amount) {
        stack.set(DataComponentRegistry.CHARGE, Math.max(amount, 0));
    }

    public void addCharges(ItemStack stack, int amount) {
        setCharges(stack, getCharges(stack) + amount);
    }

    public int getCount(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.COUNT, 1);
    }

    public void setCount(ItemStack stack, int amount) {
        stack.set(DataComponentRegistry.COUNT, Math.max(amount, 1));
    }

    public void addCount(ItemStack stack, int amount) {
        setCount(stack, getCount(stack) + amount);
    }

    public int getTime(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.TIME, 0);
    }

    public void setTime(ItemStack stack, int amount) {
        stack.set(DataComponentRegistry.TIME, Math.max(amount, 0));
    }

    public void consumeTime(ItemStack stack, int amount) {
        setTime(stack, getTime(stack) - amount);
    }

    @EventBusSubscriber
    public static class SpikedCloakEvent {
        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            if (!(event.getEntity() instanceof Player player) || player.getCommandSenderWorld().isClientSide())
                return;

            var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.SPIKED_CLOAK.value());

            if (!(stack.getItem() instanceof SpikedCloakItem relic) || !relic.isAbilityUnlocked(stack, "spikes"))
                return;

            relic.setCharges(stack, 0);
        }

        @SubscribeEvent
        public static void onInjuredEntity(LivingDamageEvent.Pre event) {
            if (!(event.getEntity() instanceof Player player) || player.getCommandSenderWorld().isClientSide())
                return;

            var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.SPIKED_CLOAK.value());

            if (!(stack.getItem() instanceof SpikedCloakItem relic) || !relic.isAbilityUnlocked(stack, "spikes"))
                return;

            if (relic.getTime(stack) > 0) {
                relic.addCount(stack, 1);
                player.getCommandSenderWorld().playSound(null, player, SoundEvents.CAMEL_SADDLE, SoundSource.PLAYERS, 1.0F, 0.9F + player.getRandom().nextFloat() * 0.2F);
            }
            relic.setTime(stack, (int) MathUtils.round(relic.getStatValue(stack, "spikes", "time"), 0) * 20);
            relic.addCharges(stack, (int) event.getNewDamage());
        }
    }
}
