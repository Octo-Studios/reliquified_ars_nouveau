package it.hurts.octostudios.reliquified_ars_nouveau.items.hands;

import com.hollingsworth.arsnouveau.api.event.SpellCastEvent;
import com.hollingsworth.arsnouveau.api.event.SpellCostCalcEvent;
import com.hollingsworth.arsnouveau.api.spell.SpellCaster;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster;
import com.hollingsworth.arsnouveau.api.util.SpellUtil;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentSensitive;
import com.hollingsworth.arsnouveau.setup.registry.DataComponentRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
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
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import top.theillusivec4.curios.api.SlotContext;

import java.awt.*;

public class ArchmagesGloveItem extends NouveauRelicItem {
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("multicasted")
                                .stat(StatData.builder("chance")
                                        .initialValue(0.15D, 0.25D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.25)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingData.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .sources(LevelingSourcesData.builder()
                                .source(LevelingSourceData.abilityBuilder("multicasted")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.BLUE)
                                        .build())
                                .build())
                        .build())
                .style(StyleData.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xff2d2d58)
                                .borderBottom(0xff2d2d58)
                                .build())
                        .beams(BeamsData.builder()
                                .startColor(0xFF13d3e5)
                                .endColor(0x007384b4)
                                .build())
                        .build())
                .loot(LootData.builder()
                        .entry(LootEntries.ARS_NOUVEAU, LootEntries.ARS_NOUVEAU_LIKE)
                        .build())
                .build();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player) || getSpellCaster(stack) == null)
            return;

        addTime(stack, 1);

        if (getMultiCount(stack) == 0) {
            setTime(stack, 0);
            setMultiCount(stack, 0);
            setSpellCaster(stack, null);
        }

        if (getTime(stack) >= 4) {
            onCasted(stack, player, player.getCommandSenderWorld(), player.getUsedItemHand());
            consumeMultiCount(stack, 1);
            setTime(stack, 0);
        }
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player) || prevStack.getItem() == stack.getItem()
                || !stack.has(DataComponentRegistry.SPELL_CASTER))
            return;

        setTime(stack, 0);
        setMultiCount(stack, 0);
        setSpellCaster(stack, null);
    }

    public void onCasted(ItemStack stack, Player player, Level level, InteractionHand handIn) {
        var caster = getSpellCaster(stack);

        if (caster == null)
            return;

        var wrappedCaster = new PlayerCaster(player);
        var resolver = caster.getSpellResolver(new SpellContext(level, caster.getSpell(), player, wrappedCaster, stack), level, player, handIn);

        resolver.spellContext.setCasterTool(stack);

        var isSensitive = resolver.spell.getBuffsAtIndex(0, player, AugmentSensitive.INSTANCE) > 0;

        HitResult result = SpellUtil.rayTrace(player, (double) 0.5F + player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).getValue(), 0.0F, isSensitive);

        if (result instanceof EntityHitResult entityHitResult)
            if (resolver.onCastOnEntity(stack, entityHitResult.getEntity(), handIn))
                caster.playSound(player.getOnPos(), level, player, caster.getCurrentSound(), SoundSource.PLAYERS);

        if (result instanceof BlockHitResult) {
            if (resolver.onCast(stack, level))
                caster.playSound(player.getOnPos(), level, player, caster.getCurrentSound(), SoundSource.PLAYERS);
        }
    }

    public SpellCaster getSpellCaster(ItemStack stack) {
        return stack.get(DataComponentRegistry.SPELL_CASTER);
    }

    public void setSpellCaster(ItemStack stack, SpellCaster caster) {
        stack.set(DataComponentRegistry.SPELL_CASTER, caster);
    }

    public void addTime(ItemStack stack, int time) {
        setTime(stack, getTime(stack) + time);
    }

    public void setTime(ItemStack stack, int time) {
        stack.set(it.hurts.sskirillss.relics.init.DataComponentRegistry.TIME, Math.max(time, 0));
    }

    public int getTime(ItemStack stack) {
        return stack.getOrDefault(it.hurts.sskirillss.relics.init.DataComponentRegistry.TIME, 0);
    }

    public void consumeMultiCount(ItemStack stack, int charge) {
        stack.set(it.hurts.sskirillss.relics.init.DataComponentRegistry.PROGRESS, getMultiCount(stack) - charge);
    }

    public void setMultiCount(ItemStack stack, int progress) {
        stack.set(it.hurts.sskirillss.relics.init.DataComponentRegistry.PROGRESS, Math.max(progress, 0));
    }

    public int getMultiCount(ItemStack stack) {
        return stack.getOrDefault(it.hurts.sskirillss.relics.init.DataComponentRegistry.PROGRESS, 0);
    }

    @EventBusSubscriber
    public static class ArchmagesGloveEvent {
        @SubscribeEvent
        public static void onCostMana(SpellCostCalcEvent event) {
            if (!(event.context.getCaster() instanceof LivingCaster livingEntity))
                return;

            var entity = livingEntity.livingEntity;
            var stack = EntityUtils.findEquippedCurio(entity, ItemRegistry.ARCHMAGES_GLOVE.value());

            if (!(stack.getItem() instanceof ArchmagesGloveItem relic) || !event.context.getCasterTool().is(ItemRegistry.ARCHMAGES_GLOVE))
                return;

            event.currentCost = 0;

            if (livingEntity.livingEntity.getCommandSenderWorld().isClientSide())
                return;

            var level = entity.getCommandSenderWorld();
            var random = level.getRandom();

            level.playSound(null, entity, SoundEvents.ALLAY_ITEM_TAKEN, SoundSource.PLAYERS, 1F, 0.9F + random.nextFloat() * 0.2F);

            ((ServerLevel) level).sendParticles(ParticleUtils.constructSimpleSpark(new Color(100 + random.nextInt(156), random.nextInt(100 + random.nextInt(156)), random.nextInt(100 + random.nextInt(156))), 0.3F, 60, 0.95F),
                    entity.getX(), entity.getY() + 0.4, entity.getZ(), 30, 0.1, 0.1, 0.1, 0.1);
        }

        @SubscribeEvent
        public static void onCastSpell(SpellCastEvent event) {
            if (!(event.context.getCaster() instanceof LivingCaster livingEntity) || !(livingEntity.livingEntity instanceof Player player)
                    || event.context.getSpell().color().getColor() != 16718260)
                return;

            var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.ARCHMAGES_GLOVE.value());

            if (!(stack.getItem() instanceof ArchmagesGloveItem relic) || !relic.isAbilityUnlocked(stack, "multicasted")
                    || event.context.getCasterTool().is(ItemRegistry.ARCHMAGES_GLOVE))
                return;

            var multicast = Math.min(5, MathUtils.multicast(player.getRandom(), relic.getStatValue(stack, "multicasted", "chance")));

            if (multicast == 0)
                return;

            relic.spreadRelicExperience(player, stack, multicast);
            relic.setMultiCount(stack, multicast);
            relic.setSpellCaster(stack, new SpellCaster().setSpell(event.context.getSpell()));
        }
    }
}
