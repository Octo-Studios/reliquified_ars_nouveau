package it.hurts.shatterbyte.reliquified_ars_nouveau.entities;

import com.hollingsworth.arsnouveau.common.capability.ManaCap;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.WhirlingBroomItem;
import it.hurts.octostudios.octolib.module.particle.trail.EntityTrailProvider;
import it.hurts.sskirillss.relics.entities.RollerSparkEntity;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.FlawlessUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WhirlingBroomEntity extends Mob {
    private static final EntityDataAccessor<Boolean> BOOSTING = SynchedEntityData.defineId(WhirlingBroomEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> SPEED = SynchedEntityData.defineId(WhirlingBroomEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MAX_HEIGHT = SynchedEntityData.defineId(WhirlingBroomEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DAMAGE_REDUCTION = SynchedEntityData.defineId(WhirlingBroomEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SPRINT_SPEED_BONUS = SynchedEntityData.defineId(WhirlingBroomEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SPRINT_MANA_PER_SECOND = SynchedEntityData.defineId(WhirlingBroomEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> SPRINT_BOOST_UNLOCKED = SynchedEntityData.defineId(WhirlingBroomEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> STORM_DAMAGE = SynchedEntityData.defineId(WhirlingBroomEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> STORM_LIFETIME = SynchedEntityData.defineId(WhirlingBroomEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> STORM_FLAWLESS = SynchedEntityData.defineId(WhirlingBroomEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FLAWLESS = SynchedEntityData.defineId(WhirlingBroomEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BOOST_REQUESTED = SynchedEntityData.defineId(WhirlingBroomEntity.class, EntityDataSerializers.BOOLEAN);

    public WhirlingBroomEntity(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = false;
    }

    @Override
    public void tick() {
        super.tick();

        if (!(this.getFirstPassenger() instanceof Player player)) {
            if (this.tickCount >= 10)
                this.discard();
            return;
        }

        var pos = this.blockPosition();
        var groundY = Integer.MIN_VALUE;

        for (var y = pos.getY(); y >= this.level().getMinBuildHeight(); y--) {
            var check = new BlockPos(pos.getX(), y, pos.getZ());

            if (!this.level().getBlockState(check).getCollisionShape(this.level(), check).isEmpty() || !this.level().getFluidState(check).isEmpty()) {
                groundY = y;
                break;
            }
        }

        if (groundY != Integer.MIN_VALUE) {
            var maxY = groundY + 1D + this.getMaxHeight();

            if (this.getY() > maxY) {
                var excess = this.getY() - maxY;
                var currentMotion = this.getDeltaMovement();
                var pullDown = Math.min(0.25D, 0.04D + excess * 0.12D);
                var newYMotion = Math.min(currentMotion.y, -pullDown);

                this.setDeltaMovement(currentMotion.x, newYMotion, currentMotion.z);
            }
        }

        if (!this.level().isClientSide()) {
            var wantsBoost = this.hasSprintBoostUnlocked() && this.isBoostRequested() && (Math.abs(player.zza) > 0.01F || Math.abs(player.xxa) > 0.01F);

            if (!wantsBoost) {
                this.entityData.set(BOOSTING, false);
            } else if (player instanceof ServerPlayer) {
                var mana = new ManaCap(player);
                var manaPerTick = this.getSprintManaPerSecond() / 20F;

                if (manaPerTick > 0F && mana.getCurrentMana() >= manaPerTick) {
                    mana.removeMana(manaPerTick);
                    this.entityData.set(BOOSTING, true);
                } else {
                    this.entityData.set(BOOSTING, false);
                }
            }
        }

        this.fallDistance = 0F;
        player.fallDistance = 0F;
        this.setYRot(player.getYRot());
        this.setXRot(player.getXRot());
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isVehicle() && this.getControllingPassenger() instanceof Player player) {
            var speed = this.getBaseSpeed() <= 0D ? 0.2D : this.getBaseSpeed();

            if (this.entityData.get(BOOSTING))
                speed *= (1D + this.getSprintSpeedBonus());

            this.setYRot(player.getYRot());
            this.setXRot(player.getXRot());
            this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();

            var forward = player.getLookAngle().normalize();
            var right = new Vec3(-forward.z, 0D, forward.x).normalize();
            var targetMotion = forward.scale(player.zza * speed).add(right.scale(-player.xxa * speed * 0.65D));

            if (player.isShiftKeyDown())
                targetMotion = targetMotion.add(0D, -speed * 0.4D, 0D);
            else if (player.zza > 0F)
                targetMotion = targetMotion.add(0D, forward.y * speed, 0D);

            // Stronger inertia: smoothly converge to target velocity instead of snapping each tick.
            var current = this.getDeltaMovement();
            var motion = new Vec3(
                    current.x + (targetMotion.x - current.x) * 0.12D,
                    current.y + (targetMotion.y - current.y) * 0.1D,
                    current.z + (targetMotion.z - current.z) * 0.12D
            );

            this.setDeltaMovement(motion);
            this.move(MoverType.SELF, this.getDeltaMovement());

            return;
        }

        super.travel(travelVector);
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof LivingEntity living ? living : null;
    }

    @Override
    public @NotNull Vec3 getPassengerRidingPosition(@NotNull Entity entity) {
        return super.getPassengerRidingPosition(entity).add(0D, -0.2D, 0D);
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    protected boolean canRide(Entity entity) {
        return entity instanceof Player;
    }

    @Override
    public void die(net.minecraft.world.damagesource.DamageSource source) {
        if (!this.level().isClientSide() && this.getFirstPassenger() instanceof ServerPlayer player) {
            var stackId = this.getPersistentData().getString(WhirlingBroomItem.BROOM_STACK_ID_TAG);

            for (var stack : EntityUtils.findItemsInInventory(player, ItemRegistry.WHIRLING_BROOM.get())) {
                if (!(stack.getItem() instanceof WhirlingBroomItem item))
                    continue;
                if (!stackId.isBlank() && !stackId.equals(stack.getOrDefault(RANDataComponentRegistry.WHIRLING_BROOM_STACK_ID.get(), "")))
                    continue;

                var cooldownTicks = item.getReviveCooldownTicks(stack);

                if (cooldownTicks > 0) {
                    WhirlingBroomItem.setCooldownUntil(stack, player.level().getGameTime() + cooldownTicks);
                    stack.set(RANDataComponentRegistry.WHIRLING_BROOM_CURRENT_HEALTH.get(), 0F);
                    stack.set(RANDataComponentRegistry.WHIRLING_BROOM_HEALTH_REGEN_TICKS.get(), 0);
                }
            }

            for (var stack : EntityUtils.findEquippedCurios(player, ItemRegistry.WHIRLING_BROOM.get())) {
                if (!(stack.getItem() instanceof WhirlingBroomItem item))
                    continue;
                if (!stackId.isBlank() && !stackId.equals(stack.getOrDefault(RANDataComponentRegistry.WHIRLING_BROOM_STACK_ID.get(), "")))
                    continue;

                var cooldownTicks = item.getReviveCooldownTicks(stack);

                if (cooldownTicks > 0) {
                    WhirlingBroomItem.setCooldownUntil(stack, player.level().getGameTime() + cooldownTicks);
                    stack.set(RANDataComponentRegistry.WHIRLING_BROOM_CURRENT_HEALTH.get(), 0F);
                    stack.set(RANDataComponentRegistry.WHIRLING_BROOM_HEALTH_REGEN_TICKS.get(), 0);
                }
            }
        }

        super.die(source);
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BOOSTING, false);
        builder.define(SPEED, 0.35F);
        builder.define(MAX_HEIGHT, 16F);
        builder.define(DAMAGE_REDUCTION, 0F);
        builder.define(SPRINT_SPEED_BONUS, 0F);
        builder.define(SPRINT_MANA_PER_SECOND, 0F);
        builder.define(SPRINT_BOOST_UNLOCKED, false);
        builder.define(STORM_DAMAGE, 0F);
        builder.define(STORM_LIFETIME, 0);
        builder.define(STORM_FLAWLESS, false);
        builder.define(FLAWLESS, false);
        builder.define(BOOST_REQUESTED, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putFloat("speed", this.getBaseSpeed());
        tag.putFloat("max_height", this.getMaxHeight());
        tag.putFloat("damage_reduction", this.getDamageReduction());
        tag.putFloat("sprint_speed_bonus", this.getSprintSpeedBonus());
        tag.putFloat("sprint_mana_per_second", this.getSprintManaPerSecond());
        tag.putBoolean("sprint_boost", this.hasSprintBoostUnlocked());
        tag.putFloat("storm_damage", this.getStormDamage());
        tag.putInt("storm_lifetime", this.getStormLifetime());
        tag.putBoolean("storm_flawless", this.isStormFlawless());
        tag.putBoolean("flawless", this.isFlawless());
        tag.putBoolean("boost_requested", this.isBoostRequested());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        this.setBaseSpeed(tag.getFloat("speed"));
        this.setMaxHeight(tag.getFloat("max_height"));
        this.setDamageReduction(tag.getFloat("damage_reduction"));
        this.setSprintSpeedBonus(tag.getFloat("sprint_speed_bonus"));
        this.setSprintManaPerSecond(tag.getFloat("sprint_mana_per_second"));
        this.setSprintBoostUnlocked(tag.getBoolean("sprint_boost"));
        this.setStormDamage(tag.getFloat("storm_damage"));
        this.setStormLifetime(tag.getInt("storm_lifetime"));
        this.setStormFlawless(tag.getBoolean("storm_flawless"));
        this.setFlawless(tag.getBoolean("flawless"));
        this.setBoostRequested(tag.getBoolean("boost_requested"));
    }

    public float getBaseSpeed() {
        return this.entityData.get(SPEED);
    }

    public void setBaseSpeed(float value) {
        this.entityData.set(SPEED, Math.max(0F, value));
    }

    public float getMaxHeight() {
        return this.entityData.get(MAX_HEIGHT);
    }

    public void setMaxHeight(float value) {
        this.entityData.set(MAX_HEIGHT, Math.max(0F, value));
    }

    public float getDamageReduction() {
        return this.entityData.get(DAMAGE_REDUCTION);
    }

    public void setDamageReduction(float value) {
        this.entityData.set(DAMAGE_REDUCTION, Math.max(0F, value));
    }

    public float getSprintSpeedBonus() {
        return this.entityData.get(SPRINT_SPEED_BONUS);
    }

    public void setSprintSpeedBonus(float value) {
        this.entityData.set(SPRINT_SPEED_BONUS, Math.max(0F, value));
    }

    public float getSprintManaPerSecond() {
        return this.entityData.get(SPRINT_MANA_PER_SECOND);
    }

    public void setSprintManaPerSecond(float value) {
        this.entityData.set(SPRINT_MANA_PER_SECOND, Math.max(0F, value));
    }

    public boolean hasSprintBoostUnlocked() {
        return this.entityData.get(SPRINT_BOOST_UNLOCKED);
    }

    public void setSprintBoostUnlocked(boolean value) {
        this.entityData.set(SPRINT_BOOST_UNLOCKED, value);
    }

    public float getStormDamage() {
        return this.entityData.get(STORM_DAMAGE);
    }

    public void setStormDamage(float value) {
        this.entityData.set(STORM_DAMAGE, Math.max(0F, value));
    }

    public int getStormLifetime() {
        return this.entityData.get(STORM_LIFETIME);
    }

    public void setStormLifetime(int value) {
        this.entityData.set(STORM_LIFETIME, Math.max(0, value));
    }

    public boolean isStormFlawless() {
        return this.entityData.get(STORM_FLAWLESS);
    }

    public void setStormFlawless(boolean value) {
        this.entityData.set(STORM_FLAWLESS, value);
    }

    public boolean isFlawless() {
        return this.entityData.get(FLAWLESS);
    }

    public void setFlawless(boolean value) {
        this.entityData.set(FLAWLESS, value);
    }

    public boolean isBoostRequested() {
        return this.entityData.get(BOOST_REQUESTED);
    }

    public void setBoostRequested(boolean value) {
        this.entityData.set(BOOST_REQUESTED, value);
    }

    @OnlyIn(Dist.CLIENT)
    public static class TrailProvider extends EntityTrailProvider<WhirlingBroomEntity> {
        public TrailProvider(WhirlingBroomEntity entity) {
            super(entity);
        }

        @Override
        public Vec3 getTrailPosition(float partialTicks) {
            var position = this.entity.getPosition(partialTicks);
            var forward = this.entity.getViewVector(partialTicks);

            if (forward.lengthSqr() <= 1.0E-6D)
                forward = this.entity.getDeltaMovement();

            if (forward.lengthSqr() <= 1.0E-6D)
                forward = new Vec3(0D, 0D, 1D);

            var horizontalForward = new Vec3(forward.x, 0D, forward.z);

            if (horizontalForward.lengthSqr() <= 1.0E-6D)
                horizontalForward = new Vec3(0D, 0D, 1D);
            else
                horizontalForward = horizontalForward.normalize();

            return position.add(0D, 0.6D, 0D).subtract(horizontalForward.scale(1.25D));
        }

        @Override
        public int getTrailUpdateFrequency() {
            return 1;
        }

        @Override
        public boolean isTrailAlive() {
            return this.entity.isAlive();
        }

        @Override
        public boolean isTrailGrowing() {
            return this.entity.tickCount > 1;
        }

        @Override
        public int getTrailMaxLength() {
            return 10;
        }

        @Override
        public int getTrailFadeInColor() {
            return FlawlessUtils.getColor(entity.isFlawless(), 0x8000FF00);
        }

        @Override
        public int getTrailFadeOutColor() {
            return FlawlessUtils.getColor(entity.isFlawless(), 0x80FFFF00);
        }

        @Override
        public double getTrailScale() {
            return 0.2F;
        }
    }
}
