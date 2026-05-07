package it.hurts.shatterbyte.reliquified_ars_nouveau.entities;

import it.hurts.octostudios.octolib.module.particle.trail.EntityTrailProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class BallistarianPhantomProjectileEntity extends Entity {
    private static final EntityDataAccessor<Integer> TARGET_ID = SynchedEntityData.defineId(BallistarianPhantomProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ORBIT_INDEX = SynchedEntityData.defineId(BallistarianPhantomProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ORBIT_TOTAL = SynchedEntityData.defineId(BallistarianPhantomProjectileEntity.class, EntityDataSerializers.INT);

    private Vec3 lastTargetPosition = null;

    public BallistarianPhantomProjectileEntity(EntityType<? extends BallistarianPhantomProjectileEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TARGET_ID, -1);
        builder.define(ORBIT_INDEX, 0);
        builder.define(ORBIT_TOTAL, 1);
    }

    public void bindToTarget(Projectile target, int index, int total) {
        this.entityData.set(TARGET_ID, target.getId());
        this.entityData.set(ORBIT_INDEX, Math.max(0, index));
        this.entityData.set(ORBIT_TOTAL, Math.max(1, total));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide())
            return;

        var targetRaw = this.level().getEntity(this.entityData.get(TARGET_ID));

        if (!(targetRaw instanceof Projectile target) || !target.isAlive()) {
            this.discard();
            return;
        }

        var targetPosition = target.position();

        if (this.lastTargetPosition != null && targetPosition.distanceToSqr(this.lastTargetPosition) <= 1.0E-8D) {
            this.discard();
            return;
        }

        this.lastTargetPosition = targetPosition;

        var velocity = target.getDeltaMovement();
        var speedSqr = velocity.x * velocity.x + velocity.z * velocity.z + velocity.y * velocity.y * 0.1D;

        if (speedSqr <= 5.0E-4D) {
            this.discard();
            return;
        }

        var directionVector = new Vec3(velocity.x, velocity.y * 0.35D, velocity.z);
        var direction = directionVector.lengthSqr() > 1.0E-6D ? directionVector.normalize() : new Vec3(0D, 1D, 0D);

        if (direction.lengthSqr() <= 1.0E-6D)
            direction = new Vec3(0D, 1D, 0D);

        var perpendicular1 = direction.cross(new Vec3(0D, 1D, 0D));

        if (perpendicular1.lengthSqr() < 1.0E-6D)
            perpendicular1 = new Vec3(1D, 0D, 0D);
        else
            perpendicular1 = perpendicular1.normalize();

        var perpendicular2 = direction.cross(perpendicular1).normalize();
        var count = Math.max(1, this.entityData.get(ORBIT_TOTAL));
        var distance = 0.55D + Math.max(0, count - 1) * 0.08D;
        var rotation = target.tickCount * 0.12D + target.getId() * 0.07D;
        var angle = Math.PI * 2D * this.entityData.get(ORBIT_INDEX) / count + rotation;
        var offset = perpendicular1.scale(Math.cos(angle) * distance)
                .add(perpendicular2.scale(Math.sin(angle) * distance));

        var center = target.position().add(velocity.scale(2D)).add(0D, target.getBbHeight() * 0.5D, 0D);

        this.setPos(center.add(offset));
        this.setDeltaMovement(velocity);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public static class TrailProvider extends EntityTrailProvider<BallistarianPhantomProjectileEntity> {
        public TrailProvider(BallistarianPhantomProjectileEntity entity) {
            super(entity);
        }

        @Override
        public Vec3 getTrailPosition(float partialTicks) {
            return this.entity.getPosition(partialTicks).add(0D, this.entity.getBbHeight() * 0.5D, 0D);
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
            return 5;
        }

        @Override
        public int getTrailFadeInColor() {
            return 0xF0F5D22D;
        }

        @Override
        public int getTrailFadeOutColor() {
            return 0x00F5D22D;
        }

        @Override
        public double getTrailScale() {
            return 0.18D;
        }
    }
}
