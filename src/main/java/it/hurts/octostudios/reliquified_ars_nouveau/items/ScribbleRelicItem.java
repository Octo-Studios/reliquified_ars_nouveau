package it.hurts.octostudios.reliquified_ars_nouveau.items;

import com.hollingsworth.arsnouveau.api.event.SpellCostCalcEvent;
import com.hollingsworth.arsnouveau.api.item.ICasterTool;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster;
import com.hollingsworth.arsnouveau.client.gui.SpellTooltip;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import com.hollingsworth.arsnouveau.setup.config.Config;
import com.hollingsworth.arsnouveau.setup.registry.DataComponentRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.spell.EffectNoManaCost;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Optional;

public abstract class ScribbleRelicItem extends NouveauRelicItem implements ICasterTool {
    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level world, @NotNull Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
//.setSpell(new Spell( EffectNoManaCost.INSTANCE))
        if (!stack.has(DataComponentRegistry.SPELL_CASTER))
            stack.set(DataComponentRegistry.SPELL_CASTER, new SpellCaster());
    }

    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack pStack) {
        AbstractCaster<?> caster = this.getSpellCaster(pStack);

        return caster != null && Config.GLYPH_TOOLTIPS.get() && !caster.isSpellHidden() && !caster.getSpell().isEmpty() ? Optional.of(new SpellTooltip(caster)) : Optional.empty();
    }

    public void onAutoCastedSpell(Player player, ItemStack stack) {
        var caster = getSpellCaster(stack);

        if (caster == null)
            return;

        ArrayList<AbstractSpellPart> recipe = new ArrayList<>();

        recipe.add(MethodTouch.INSTANCE);

        for (var spell : caster.getSpell().recipe())
            if (!(spell instanceof AbstractCastMethod))
                recipe.add(spell);

        caster.castSpell(player.getCommandSenderWorld(), player, player.getUsedItemHand(), Component.empty(), new Spell(recipe));
    }

    public SpellCaster getSpellCaster(ItemStack stack) {
        return stack.get(DataComponentRegistry.SPELL_CASTER);
    }

//    public void addSpellsToItem(ItemStack stack, AbstractSpellPart... spellParts) {
//
//    }

    @EventBusSubscriber
    public static class ScribbleRelicEvent {
        @SubscribeEvent
        public static void onCostMana(SpellCostCalcEvent event) {
            if (!(event.context.getCaster() instanceof PlayerCaster playerCaster))
                return;

            var player = playerCaster.player;
            var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.EMBLEM_OF_DEFENSE.value());

            if (player.getCommandSenderWorld().isClientSide() || !(stack.getItem() instanceof ScribbleRelicItem relic)
                    || !event.context.getCasterTool().is(Items.AIR))
                return;

            var caster = relic.getSpellCaster(stack);

            if (caster == null)
                return;

            ArrayList<AbstractSpellPart> recipe = new ArrayList<>();
            for (var spell : caster.getSpell().recipe())
                if (!(spell instanceof AbstractCastMethod))
                    recipe.add(spell);

            recipe.add(EffectNoManaCost.INSTANCE);

            for (var spell : recipe) {
                if (spell.equals(EffectNoManaCost.INSTANCE)) {
                    event.currentCost = 0;
                    break;
                }
            }
        }

        public static boolean containsSpellPart(Iterable<AbstractSpellPart> parts, AbstractSpellPart target) {
            for (AbstractSpellPart part : parts)
                if (part.equals(target))
                    return true;

            return false;
        }
    }
}
