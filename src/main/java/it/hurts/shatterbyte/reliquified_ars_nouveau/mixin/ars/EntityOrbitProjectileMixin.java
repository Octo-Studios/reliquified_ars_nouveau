package it.hurts.shatterbyte.reliquified_ars_nouveau.mixin.ars;

import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import com.hollingsworth.arsnouveau.api.spell.SpellResolver;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster;
import com.hollingsworth.arsnouveau.common.entity.EntityOrbitProjectile;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.charm.EmblemOfDevotionItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;

@Mixin(EntityOrbitProjectile.class)
public class EntityOrbitProjectileMixin {
    @Inject(method = "onHit", at = @At("HEAD"), cancellable = true)
    private void ran$handleEmblemOfDevotion(HitResult result, CallbackInfo ci) {
        if (!(result instanceof EntityHitResult entityHitResult))
            return;

        var projectile = (EntityOrbitProjectile) (Object) this;

        if (projectile.level().isClientSide())
            return;

        var projectileData = projectile.getPersistentData();

        if (!projectileData.getBoolean(EmblemOfDevotionItem.DEVOTION_ORBIT_TAG))
            return;

        if (!(projectile.getOwner() instanceof ServerPlayer player))
            return;

        if (!(entityHitResult.getEntity() instanceof LivingEntity target) || !target.isAlive() || target == player)
            return;

        var stackId = projectileData.getString(EmblemOfDevotionItem.DEVOTION_STACK_ID_TAG);

        if (stackId.isBlank())
            return;

        var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.EMBLEM_OF_DEVOTION.get()).stream()
                .filter(relicStack -> relicStack.getItem() instanceof EmblemOfDevotionItem)
                .filter(relicStack -> stackId.equals(relicStack.getOrDefault(it.hurts.shatterbyte.reliquified_ars_nouveau.init.RANDataComponentRegistry.EMBLEM_OF_DEVOTION_STACK_ID.get(), "")))
                .toList();

        if (stacks.isEmpty())
            return;

        var consumed = true;

        for (var stack : stacks) {
            if (!(stack.getItem() instanceof EmblemOfDevotionItem item))
                continue;

            var relicData = item.getRelicData(player, stack);
            var ability = relicData.getAbilitiesData().getAbilityData("devotion");

            var caster = item.getSpellCaster(stack);

            if (caster == null)
                continue;

            var spell = caster.getSpell();

            if (spell == null || !spell.isValid() || spell.size() <= 0 || !(spell.get(0) instanceof MethodTouch))
                continue;

            var resolver = new SpellResolver(new SpellContext(player.serverLevel(), spell, player, new PlayerCaster(player), stack)).withSilent(true);

            if (!resolver.onCastOnEntity(stack, target, InteractionHand.MAIN_HAND))
                continue;

            relicData.getLevelingData().addExperience("devotion", "sphere_hit", 1D);
            ability.getStatisticData().getMetricData("sphere_hits").addValue(1D);

            if (ability.getRankModifierData("sphere_preservation").isEnabled()) {
                var preserveChance = Mth.clamp(ability.getStatData("preservation_chance").getValue(), 0D, 1D);

                if (preserveChance > 0D && player.getRandom().nextDouble() < preserveChance) {
                    consumed = false;
                    relicData.getLevelingData().addExperience("devotion", "sphere_preserved", 1D);
                    ability.getStatisticData().getMetricData("spheres_preserved").addValue(1D);
                }
            }

            if (ability.getRankModifierData("absorption_burst").isEnabled()) {
                var absorptionGain = Math.max(0D, ability.getStatData("absorption_hearts").getValue());
                var absorptionCap = Math.max(0D, ability.getStatData("absorption_cap").getValue());

                if (absorptionCap <= 0D)
                    absorptionCap = absorptionGain;

                if (absorptionGain > 0D) {
                    var data = player.getPersistentData();
                    var currentAmount = Math.max(0D, data.getDouble(EmblemOfDevotionItem.DEVOTION_ABSORPTION_AMOUNT_TAG));
                    var newAmount = Math.min(absorptionCap, currentAmount + absorptionGain);
                    var grantedAmount = Math.max(0D, newAmount - currentAmount);

                    data.putDouble(EmblemOfDevotionItem.DEVOTION_ABSORPTION_AMOUNT_TAG, newAmount);
                    EntityUtils.resetAttribute(player, Attributes.MAX_ABSORPTION, (float) newAmount, AttributeModifier.Operation.ADD_VALUE, EmblemOfDevotionItem.DEVOTION_ABSORPTION_ATTRIBUTE_ID);
                    player.setAbsorptionAmount((float) Math.min(newAmount, player.getAbsorptionAmount() + (float) absorptionGain));

                    ability.getStatisticData().getMetricData("absorption_granted").addValue(grantedAmount);
                }
            }

            if (ability.getRankModifierData("spell_propagation").isEnabled()) {
                var propagationChance = Mth.clamp(ability.getStatData("propagation_chance").getValue(), 0D, 1D);

                if (propagationChance > 0D) {
                    var propagationRadius = Math.max(0D, ability.getStatData("propagation_radius").getValue());
                    var maxPropagationTargets = Math.max(1, (int) Math.round(Math.max(0D, ability.getStatData("propagation_targets").getValue())));
                    var propagationTargets = MathUtils.multicast(player.getRandom(), propagationChance, maxPropagationTargets);

                    if (propagationRadius > 0D && propagationTargets > 0) {
                        var neighbors = player.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(propagationRadius),
                                        neighbor -> neighbor != player && neighbor != target && neighbor.isAlive() && !EntityUtils.isAlliedTo(player, neighbor))
                                .stream()
                                .sorted(Comparator.comparingDouble(target::distanceToSqr))
                                .limit(propagationTargets)
                                .toList();

                        if (!neighbors.isEmpty()) {
                            var hits = 0;

                            for (var neighbor : neighbors) {
                                var chainedResolver = new SpellResolver(new SpellContext(player.serverLevel(), spell, player, new PlayerCaster(player), stack)).withSilent(true);

                                if (chainedResolver.onCastOnEntity(stack, neighbor, InteractionHand.MAIN_HAND))
                                    hits++;
                            }

                            if (hits > 0) {
                                ability.getStatisticData().getMetricData("extra_targets_hit").addValue(hits);
                                relicData.getLevelingData().addExperience("devotion", "chain_hit", hits);
                            }
                        }
                    }
                }
            }

            break;
        }

        if (consumed) {
            projectile.pierceLeft--;

            if (projectile.pierceLeft < 0) {
                projectile.level().broadcastEntityEvent(projectile, (byte) 3);
                projectile.discard();
            }
        }

        ci.cancel();
    }
}

