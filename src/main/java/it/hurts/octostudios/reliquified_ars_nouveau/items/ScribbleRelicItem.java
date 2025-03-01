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
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.awt.*;
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

        List<AbstractSpellPart> recipe = getSpell(caster).stream().filter(spell -> !(spell instanceof AbstractCastMethod)).toList();

        var spellCaster = new SpellCaster().setSpell(new Spell(recipe));

        return Config.GLYPH_TOOLTIPS.get() && !spellCaster.isSpellHidden() && !recipe.isEmpty() ? Optional.of(new SpellTooltip(spellCaster)) : Optional.empty();
    }

    public void onAutoCastedSpell(Player player, LivingEntity target, ItemStack stack, Color color) {
        var caster = getSpellCaster(stack);

        if (caster == null)
            return;

        var level = player.getCommandSenderWorld();
        var usedHand = player.getUsedItemHand();
        var context = new SpellContext(player.level(), new Spell(getSpell(caster)), player, new PlayerCaster(player));

        context.setCasterTool(stack);

        caster.getSpellResolver(context, level, player, usedHand).onCastOnEntity(stack, target, usedHand);
        caster.playSound(player.getOnPos(), level, player, caster.getCurrentSound(), SoundSource.PLAYERS);

        if (CuriosApi.getCuriosInventory(player).flatMap(inventory -> inventory.findCurios(stack1 -> stack1.is(stack.getItem()))
                .stream().map(SlotResult::slotContext).filter(slotContext -> !slotContext.visible()).findFirst()).isPresent())
            return;

        var random = player.getRandom();

        double x1 = player.getX(), y1 = player.getY() + player.getBbHeight() / 2, z1 = player.getZ();
        double x2 = target.getX(), y2 = target.getY() + target.getBbHeight() / 2, z2 = target.getZ();

        var start = new Vec3(x1, y1, z1);
        var end = new Vec3(x2, y2, z2);

        int steps = Math.max(3, Math.round(player.distanceTo(target)) * 2);
        var points = new ArrayList<Vec3>();

        points.add(start);

        addPoints(points, start, end, random, 1.3, steps);

        points.add(end);

        renderLightningLine(player, points, 25, color);

        for (int n = 0; n <= 3; n++) {
            var shortPoints = new ArrayList<Vec3>();

            shortPoints.add(start);

            var minSteps = Math.max(1, steps / 2);

            addPoints(shortPoints, start, end, random, (double) (random.nextInt(12, 15)) / 13, random.nextInt(minSteps, Math.max(minSteps + 1, steps)));

            renderLightningLine(player, shortPoints, 20, color);
        }
    }

    public List<AbstractSpellPart> getSpellList(Iterable<AbstractSpellPart> recipe) {
        return StreamSupport.stream(recipe.spliterator(), false).filter(part -> !(part instanceof AbstractCastMethod)).toList();
    }

    public List<AbstractSpellPart> getSpell(SpellCaster caster) {
        if (caster == null)
            return Collections.emptyList();

        List<AbstractSpellPart> spellList = new ArrayList<>();

        spellList.add(MethodTouch.INSTANCE);

        spellList.addAll(getSpellList(caster.getSpell().recipe()));

        return spellList;
    }

    public SpellCaster getSpellCaster(ItemStack stack) {
        return stack.get(DataComponentRegistry.SPELL_CASTER);
    }

    private static void addPoints(ArrayList<Vec3> arrayPoint, Vec3 start, Vec3 end, RandomSource random, double offsetRange, double steps) {
        for (int i = 1; i < steps; i++) {
            float t = i / 10F;

            double x = start.x() + (end.x() - start.x()) * t + (random.nextDouble() - 0.5) * offsetRange;
            double y = start.y() + (end.y() - start.y()) * t + (random.nextDouble() - 0.5) * 2;
            double z = start.z() + (end.z() - start.z()) * t + (random.nextDouble() - 0.5) * offsetRange;

            arrayPoint.add(new Vec3(x, y, z));
        }
    }

    private static void renderLightningLine(Player player, List<Vec3> points, int lineSteps, Color color) {
        for (int i = 0; i < points.size() - 1; i++) {
            Vec3 start = points.get(i);
            Vec3 end = points.get(i + 1);

            for (int j = 0; j <= lineSteps; j++) {
                float t = j / (float) lineSteps;
                double x = start.x + (end.x - start.x) * t;
                double y = start.y + (end.y - start.y) * t;
                double z = start.z + (end.z - start.z) * t;

                ((ServerLevel) player.getCommandSenderWorld()).sendParticles(ParticleUtils.constructSimpleSpark(color, 0.2F, 60, 0.95F),
                        x, y, z, 1, 0, 0, 0, 0.001);
            }
        }
    }

    @EventBusSubscriber
    public static class ScribbleRelicEvent {
        @SubscribeEvent
        public static void onCostMana(SpellCostCalcEvent event) {
            if (!(event.context.getCaster() instanceof LivingCaster livingEntity))
                return;

            var entity = livingEntity.livingEntity;
            var spellContext = event.context;

            if (entity.getCommandSenderWorld().isClientSide() || !(spellContext.getCasterTool().getItem() instanceof ScribbleRelicItem relic))
                return;

            event.currentCost = 0;
        }
    }
}
