package it.hurts.octostudios.reliquified_ars_nouveau.items.bracelet;

import com.hollingsworth.arsnouveau.api.spell.Spell;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import com.hollingsworth.arsnouveau.api.spell.SpellResolver;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.client.particle.ParticleUtil;
import com.hollingsworth.arsnouveau.common.entity.Cinder;
import com.hollingsworth.arsnouveau.common.items.curios.ShapersFocus;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.NouveauRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.*;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemColor;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemShape;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.List;

public class FlamingBracerItem extends NouveauRelicItem {
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("resistance")
                                .maxLevel(0)
                                .build())
                        .ability(AbilityData.builder("pyroclastic")
                                .stat(StatData.builder("chance")
                                        .initialValue(0.1D, 0.3D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.2)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 1))
                                        .build())
                                .stat(StatData.builder("count")
                                        .initialValue(2D, 4D)
                                        .upgradeModifier(UpgradeOperation.ADD, 0.5D)
                                        .formatValue(value -> (int) MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingData.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .sources(LevelingSourcesData.builder()
                                .source(LevelingSourceData.abilityBuilder("pyroclastic")
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

    public static List<ItemStack> findEquippedCurios(Entity entity) {
        if (!(entity instanceof Player player))
            return List.of();

        return CuriosApi.getCuriosInventory(player)
                .map(inventory -> inventory.findCurios(ItemRegistry.FLAMING_BRACER.value()).stream()
                        .map(SlotResult::stack)
                        .toList())
                .orElse(List.of());
    }

    @EventBusSubscriber
    public static class FlamingBracerItemEvent {
        @SubscribeEvent
        public static void onPlayerAttacking(AttackEntityEvent event) {
            var player = event.getEntity();
            var target = event.getTarget();

            if (!target.isAlive())
                return;

            var level = player.getCommandSenderWorld();

            for (var stack : findEquippedCurios(player)) {
                if (player.getCommandSenderWorld().isClientSide() || !(stack.getItem() instanceof FlamingBracerItem relic))
                    continue;

                var random = level.getRandom();

                if (!relic.isAbilityUnlocked(stack, "pyroclastic") || !target.isOnFire() || player.getAttackStrengthScale(0.5F) < 0.9F)
                    continue;

                var context = new SpellContext(player.level(), new Spell(), player, new LivingCaster(player));
                var hit = target.getPosition(1);
                var resolver = new SpellResolver(context);

                var fireCount = Math.min(relic.getStatValue(stack, "pyroclastic", "count"), MathUtils.multicast(random, relic.getStatValue(stack, "pyroclastic", "chance")));

                for (int i = 0; i < fireCount; i++) {
                    var vec3 = new Vec3(hit.x() - Math.sin(random.nextInt(360)), hit.y(), hit.z() - Math.cos(random.nextInt(360)));
                    var fallingBlock = new Cinder(level, vec3.x(), vec3.y(), vec3.z(), BlockRegistry.MAGIC_FIRE.defaultBlockState(), resolver);

                    fallingBlock.setDeltaMovement(vec3.x() - hit.x(), ParticleUtil.inRange(0.1, 0.5), vec3.z() - hit.z());
                    fallingBlock.setDeltaMovement(fallingBlock.getDeltaMovement().multiply(new Vec3(ParticleUtil.inRange(0.1, 0.5), 1, ParticleUtil.inRange(0.1, 0.5))));
                    fallingBlock.dropItem = false;
                    fallingBlock.hurtEntities = false;
                    fallingBlock.shooter = player;
                    fallingBlock.setOwner(player);
                    fallingBlock.getPersistentData().putBoolean("canTrail", true);

                    level.addFreshEntity(fallingBlock);

                    ShapersFocus.tryPropagateEntitySpell(fallingBlock, level, player, context, resolver);
                }
            }
        }
    }
}
