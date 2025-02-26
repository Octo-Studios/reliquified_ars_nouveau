package it.hurts.octostudios.reliquified_ars_nouveau.items;

import com.hollingsworth.arsnouveau.api.event.SpellCostCalcEvent;
import com.hollingsworth.arsnouveau.api.item.ICasterTool;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster;
import com.hollingsworth.arsnouveau.client.gui.SpellTooltip;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import com.hollingsworth.arsnouveau.setup.config.Config;
import com.hollingsworth.arsnouveau.setup.registry.DataComponentRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.spell.EffectNoManaCost;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

public abstract class ScribbleRelicItem extends NouveauRelicItem implements ICasterTool {
    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level world, @NotNull Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (!stack.has(DataComponentRegistry.SPELL_CASTER))
            stack.set(DataComponentRegistry.SPELL_CASTER, new SpellCaster().setSpell(new Spell()));
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack stack) {
        var caster = this.getSpellCaster(stack);

        if (caster == null)
            return Optional.empty();

        List<AbstractSpellPart> recipe = getSpell(caster).stream().filter(spell -> !(spell instanceof AbstractCastMethod) && !spell.equals(EffectNoManaCost.INSTANCE)).toList();

        var spellCaster = new SpellCaster().setSpell(new Spell(recipe));

        return Config.GLYPH_TOOLTIPS.get() && !spellCaster.isSpellHidden() && !recipe.isEmpty() ? Optional.of(new SpellTooltip(spellCaster)) : Optional.empty();
    }

    public void onAutoCastedSpell(Player player, LivingEntity target, ItemStack stack) {
        var caster = getSpellCaster(stack);

        if (caster == null)
            return;

        var level = player.getCommandSenderWorld();
        var usedHand = player.getUsedItemHand();
        var context = new SpellContext(player.level(), new Spell(getSpell(caster)), player, new PlayerCaster(player));

        context.setCasterTool(stack);

        if (caster.getSpellResolver(context, level, player, usedHand).onCastOnEntity(stack, target, usedHand))
            caster.playSound(player.getOnPos(), level, player, caster.getCurrentSound(), SoundSource.PLAYERS);
    }

    public List<AbstractSpellPart> getSpellList(Iterable<AbstractSpellPart> recipe) {
        return StreamSupport.stream(recipe.spliterator(), false).filter(part -> !(part instanceof AbstractCastMethod)).toList();
    }

    public List<AbstractSpellPart> getSpell(SpellCaster caster) {
        if (caster == null)
            return Collections.emptyList();

        List<AbstractSpellPart> spellList = new ArrayList<>();

        spellList.add(MethodTouch.INSTANCE);
        spellList.add(EffectNoManaCost.INSTANCE);

        spellList.addAll(getSpellList(caster.getSpell().recipe()));

        return spellList;
    }

    public SpellCaster getSpellCaster(ItemStack stack) {
        return stack.get(DataComponentRegistry.SPELL_CASTER);
    }

    @EventBusSubscriber
    public static class ScribbleRelicEvent {
        @SubscribeEvent
        public static void onCostMana(SpellCostCalcEvent event) {
            if (!(event.context.getCaster() instanceof LivingCaster livingEntity))
                return;

            var entity = livingEntity.livingEntity;
            var spellContext = event.context;

            if (entity.getCommandSenderWorld().isClientSide() || !(spellContext.getCasterTool().getItem() instanceof ScribbleRelicItem relic)
                    || !relic.getSpellList(spellContext.getSpell().recipe()).contains(EffectNoManaCost.INSTANCE))
                return;

            event.currentCost = 0;
        }
    }
}
