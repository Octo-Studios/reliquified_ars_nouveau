package it.hurts.octostudios.reliquified_ars_nouveau.items.back;

import com.hollingsworth.arsnouveau.common.capability.ManaCap;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.NouveauRelicItem;
import it.hurts.sskirillss.relics.init.DataComponentRegistry;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.*;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemColor;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemShape;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import top.theillusivec4.curios.api.SlotContext;

public class CloakOfConcealmentItem extends NouveauRelicItem {
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("absorption")
                                .stat(StatData.builder("consumption")
                                        .initialValue(25D, 20D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, -0.075)
                                        .formatValue(value -> (int) MathUtils.round(value, 1))
                                        .build())
                                .stat(StatData.builder("cooldown")
                                        .initialValue(27D, 22D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, -0.05)
                                        .formatValue(value -> (int) MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingData.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .sources(LevelingSourcesData.builder()
                                .source(LevelingSourceData.abilityBuilder("absorption")
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
        if (!((slotContext.entity()) instanceof Player player) || player.getCommandSenderWorld().isClientSide())
            return;

        if (new ManaCap(player).getCurrentMana() < 1 || getTime(stack) > 0)
            addTime(stack, 1);

        if (getTime(stack) >= getStatValue(stack, "absorption", "consumption"))
            setTime(stack, 0);
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

    @EventBusSubscriber
    public static class CloakOfConcealmentItemEvent {
        @SubscribeEvent
        public static void onInjuredEntity(LivingDamageEvent.Pre event) {
            if (!(event.getEntity() instanceof Player player) || player.getCommandSenderWorld().isClientSide())
                return;

            var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.CLOAK_OF_CONCEALMENT.value());

            if (!(stack.getItem() instanceof CloakOfConcealmentItem relic) || !relic.isAbilityUnlocked(stack, "absorption")
                    || relic.getTime(stack) > 1)
                return;

            var mana = new ManaCap(player);
            var statValue = relic.getStatValue(stack, "absorption", "consumption");
            var damageToBlock = (int) Math.min(mana.getCurrentMana() / statValue, event.getNewDamage());

            mana.removeMana(damageToBlock * statValue);

            event.setNewDamage(event.getNewDamage() - damageToBlock);
        }
    }
}
