package it.hurts.octostudios.reliquified_ars_nouveau.items.charm;

import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster;
import com.hollingsworth.arsnouveau.common.entity.EntityOrbitProjectile;
import com.hollingsworth.arsnouveau.common.spell.method.MethodProjectile;
import it.hurts.octostudios.reliquified_ars_nouveau.items.ScribbleRelicItem;
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
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class EmblemOfDevotionItem extends ScribbleRelicItem {
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("devotion")
                                .stat(StatData.builder("periodicity")
                                        .initialValue(30D, 25D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, -0.06D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatData.builder("count")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(UpgradeOperation.ADD, 0.475D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingData.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .sources(LevelingSourcesData.builder()
                                .source(LevelingSourceData.abilityBuilder("devotion")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.ORANGE)
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
        if (!(slotContext.entity() instanceof Player player) || player.getCommandSenderWorld().isClientSide() || !isAbilityUnlocked(stack, "devotion")
                || getCharge(stack) >= MathUtils.round(getStatValue(stack, "devotion", "count"), 0))
            return;

        var level = player.getCommandSenderWorld();

        addTime(stack, 1);

        if (getTime(stack) >= (int) MathUtils.round(getStatValue(stack, "devotion", "periodicity"), 0) * 20) {
            addCharges(stack, 1);

            var context = new SpellContext(player.level(), getSpellCaster(stack).getSpell(), player, new PlayerCaster(player));
            var resolver = getSpellCaster(stack).getSpellResolver(context, level, player, player.getUsedItemHand());
            var stats = resolver.getCastStats();

            context.setCanceled(true);
            context.setCasterTool(stack);

            var wardProjectile = new EntityOrbitProjectile(level, resolver.getNewResolver(resolver.spellContext.makeChildContext().withSpell(context.getRemainingSpell()
                    .mutable().add(0, MethodProjectile.INSTANCE).immutable())));

            wardProjectile.setOffset((1 + getCharge(stack) * 40) + getCharge(stack) * 20);
            wardProjectile.setAccelerates((int) stats.getAccMultiplier());
            wardProjectile.setAoe((float) stats.getAoeMultiplier());
            wardProjectile.extendTimes = (int) (Math.round(getStatValue(stack, "devotion", "count")) + 1) * 20;
            wardProjectile.setTotal(getCharge(stack));
            wardProjectile.setColor(resolver.spellContext.getColors());
            wardProjectile.setOwner(player);

            level.addFreshEntity(wardProjectile);

            setTime(stack, 0);

            level.playSound(null, player, SoundEvents.ALLAY_ITEM_GIVEN, SoundSource.PLAYERS, 0.75F, 0.9F + player.getRandom().nextFloat() * 0.2F);
        }
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player) || player.getCommandSenderWorld().isClientSide()
                || prevStack.getItem() == stack.getItem() || getCharge(stack) < 0)
            return;

        setCharge(stack, 0);
        setTime(stack, 0);
    }

    public int getCharge(ItemStack stack) {
        return stack.getOrDefault(it.hurts.sskirillss.relics.init.DataComponentRegistry.CHARGE, 0);
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
    public int getCountGlyphInItem(ItemStack stack) {
        return 10;
    }
}
