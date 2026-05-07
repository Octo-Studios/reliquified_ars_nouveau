package it.hurts.shatterbyte.reliquified_ars_nouveau.mixin.ars;

import com.hollingsworth.arsnouveau.common.entity.EntityChimeraProjectile;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.back.SpikedCloakItem;
import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityChimeraProjectile.class)
public abstract class EntityChimeraProjectileMixin {
    @Redirect(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lcom/hollingsworth/arsnouveau/common/entity/EntityChimeraProjectile;remove(Lnet/minecraft/world/entity/Entity$RemovalReason;)V", ordinal = 0))
    private void ran$preventDiscardOnEntityHit(EntityChimeraProjectile projectile, Entity.RemovalReason reason) {
        if (projectile.getPersistentData().getBoolean("ran_spiked_cloak_spike"))
            return;

        projectile.remove(reason);
    }

    @Redirect(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean ran$applySpikeStoredDamage(Entity target, DamageSource source, float originalDamage) {
        var projectile = (EntityChimeraProjectile) (Object) this;

        if (!projectile.getPersistentData().getBoolean("ran_spiked_cloak_spike"))
            return target.hurt(source, originalDamage);

        var storedDamage = (float) projectile.getPersistentData().getDouble("ran_spiked_cloak_damage");
        var applied = target.hurt(source, storedDamage > 0F ? storedDamage : originalDamage);

        if (applied && target instanceof LivingEntity livingTarget) {
            if (projectile.getOwner() instanceof ServerPlayer player) {
                for (var stack : EntityUtils.findEquippedCurios(player, ItemRegistry.SPIKED_CLOAK.get())) {
                    if (!(stack.getItem() instanceof SpikedCloakItem item))
                        continue;

                    var relicData = item.getRelicData(player, stack);
                    var ability = relicData.getAbilitiesData().getAbilityData("thorn_burst");

                    relicData.getLevelingData().addExperience("thorn_burst", "spike_hit", 1D);
                    ability.getStatisticData().getMetricData("spike_hits").addValue(1D);
                    ability.getStatisticData().getMetricData("spike_damage_dealt").addValue(storedDamage > 0F ? storedDamage : originalDamage);
                }
            }

            var duration = Math.max(0, projectile.getPersistentData().getInt("ran_spiked_cloak_bleeding_duration"));

            if (duration > 0) {
                var amplifier = Math.max(0, projectile.getPersistentData().getInt("ran_spiked_cloak_bleeding_level"));
                livingTarget.addEffect(new MobEffectInstance(RelicsMobEffects.BLEEDING, duration, amplifier, false, true, true),
                        projectile.getOwner() instanceof LivingEntity owner ? owner : null);
            }

            if (!projectile.level().isClientSide() && projectile.getOwner() instanceof LivingEntity owner) {
                var chainDepth = Math.max(0, projectile.getPersistentData().getInt("ran_spiked_cloak_chain_depth"));
                var chainChance = Mth.clamp(projectile.getPersistentData().getDouble("ran_spiked_cloak_chain_chance"), 0D, 1D);
                var chainMaxSpikes = Math.max(0, projectile.getPersistentData().getInt("ran_spiked_cloak_chain_max_spikes"));
                var chainStoredDamage = Math.max(0D, projectile.getPersistentData().getDouble("ran_spiked_cloak_damage"));

                if (chainDepth < 2 && chainChance > 0D && chainMaxSpikes > 0 && chainStoredDamage > 0D && livingTarget.isAlive()) {
                    var chainSpikes = MathUtils.multicast(owner.getRandom(), chainChance, chainMaxSpikes);

                    if (chainSpikes > 0) {
                        var origin = livingTarget.getEyePosition();
                        var chainForward = livingTarget.position().subtract(owner.position());
                        var chainHorizontalForward = new Vec3(chainForward.x, 0D, chainForward.z);

                        if (chainHorizontalForward.lengthSqr() < 1.0E-6D)
                            chainHorizontalForward = new Vec3(owner.getLookAngle().x, 0D, owner.getLookAngle().z);

                        if (chainHorizontalForward.lengthSqr() < 1.0E-6D)
                            chainHorizontalForward = new Vec3(0D, 0D, 1D);
                        else
                            chainHorizontalForward = chainHorizontalForward.normalize();

                        var chainRight = new Vec3(-chainHorizontalForward.z, 0D, chainHorizontalForward.x);
                        var chainOffset = owner.getRandom().nextDouble() * (Math.PI * 2D);

                        for (var i = 0; i < chainSpikes; i++) {
                            var angle = chainOffset + (Math.PI * 2D * i) / chainSpikes;
                            var direction = chainHorizontalForward.scale(Math.cos(angle))
                                    .add(chainRight.scale(Math.sin(angle)))
                                    .normalize();

                            var chainSpike = new EntityChimeraProjectile(projectile.level());
                            var spawnPos = origin.add(direction.scale(0.45D));

                            chainSpike.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
                            chainSpike.setOwner(owner);
                            chainSpike.getPersistentData().putBoolean("ran_spiked_cloak_spike", true);
                            chainSpike.getPersistentData().putDouble("ran_spiked_cloak_damage", chainStoredDamage);
                            chainSpike.getPersistentData().putInt("ran_spiked_cloak_chain_depth", chainDepth + 1);
                            chainSpike.getPersistentData().putDouble("ran_spiked_cloak_chain_chance", chainChance);
                            chainSpike.getPersistentData().putInt("ran_spiked_cloak_chain_max_spikes", chainMaxSpikes);

                            var bleedingDuration = Math.max(0, projectile.getPersistentData().getInt("ran_spiked_cloak_bleeding_duration"));
                            var bleedingLevel = Math.max(0, projectile.getPersistentData().getInt("ran_spiked_cloak_bleeding_level"));

                            if (bleedingDuration > 0) {
                                chainSpike.getPersistentData().putInt("ran_spiked_cloak_bleeding_duration", bleedingDuration);
                                chainSpike.getPersistentData().putInt("ran_spiked_cloak_bleeding_level", bleedingLevel);
                            }

                            chainSpike.shoot(direction.x, direction.y, direction.z, 2.0F, 0F);
                            projectile.level().addFreshEntity(chainSpike);
                        }

                        if (owner instanceof ServerPlayer playerOwner) {
                            for (var stack : EntityUtils.findEquippedCurios(playerOwner, ItemRegistry.SPIKED_CLOAK.get())) {
                                if (!(stack.getItem() instanceof SpikedCloakItem item))
                                    continue;

                                var relicData = item.getRelicData(playerOwner, stack);
                                var ability = relicData.getAbilitiesData().getAbilityData("thorn_burst");

                                if (!ability.getRankModifierData("spike_bloom").isEnabled())
                                    continue;

                                relicData.getLevelingData().addExperience("thorn_burst", "chain_trigger", 1D);
                                ability.getStatisticData().getMetricData("chain_triggers").addValue(1D);
                            }
                        }
                    }
                }
            }
        }

        return applied;
    }
}

