package it.hurts.octostudios.reliquified_ars_nouveau.entities;

import com.hollingsworth.arsnouveau.client.particle.GlowParticleData;
import com.hollingsworth.arsnouveau.client.particle.ParticleColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class MagicShellEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(MagicShellEntity.class, EntityDataSerializers.FLOAT);

    public MagicShellEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        super(entityType, level);

        this.setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();

        var level = getCommandSenderWorld();

        double deltaX = this.getX() - this.xo;
        double deltaY = this.getY() - this.yo;
        double deltaZ = this.getZ() - this.zo;

        for (float coeff = 0; coeff <= 1.0F; coeff += 0.2F)
            level.addParticle(GlowParticleData.createData(new ParticleColor(255, 25, 180)), this.xo + deltaX * coeff, this.yo + deltaY * coeff + this.getBbHeight() / 2, this.zo + deltaZ * coeff,
                    0.0125F * (this.random.nextFloat() - 0.5F), 0.0125F * (this.random.nextFloat() - 0.5F), 0.0125F * (this.random.nextFloat() - 0.5F));

        if (level.isClientSide())
            return;

        var entity = ((ServerLevel) level).getEntity(getPersistentData().getUUID("TargetUUID"));
        var persistentData = getPersistentData();

        if (tickCount >= 300 || !persistentData.hasUUID("TargetUUID"))
            return;

        var current = this.getDeltaMovement();
        var currentPosition = this.position();

        if (persistentData.contains("HitPosX") && persistentData.contains("HitPosY") && persistentData.contains("HitPosZ")) {
            var hitX = persistentData.getDouble("HitPosX");
            var hitY = persistentData.getDouble("HitPosY");
            var hitZ = persistentData.getDouble("HitPosZ");

            Vec3 targetPos = new Vec3(hitX, hitY, hitZ);

            if (currentPosition.distanceTo(new Vec3(hitX, hitY, hitZ)) <= 0.5F) {
                teleportTo(hitX, hitY, hitZ);
                discard();
            } else
                this.setDeltaMovement(targetPos.subtract(currentPosition).normalize());
        } else {
            if (!(entity instanceof Projectile target))
                return;

            int bowIndex = persistentData.getInt("BowIndex");
            var time = this.tickCount + bowIndex * 20;

            var directionToTarget = target.position().add(0, target.getBbHeight() / 2.0, 0).subtract(currentPosition).normalize();

            var sideVector = directionToTarget.cross(new Vec3(0, 1, 0)).normalize();
            var chaosVec = sideVector.scale(Math.sin(time * 0.1 + bowIndex) * 0.05).add(directionToTarget.cross(sideVector).normalize().scale(Math.cos(time * 0.12 + bowIndex) * 0.05));

            this.setDeltaMovement(current.normalize().add(directionToTarget.subtract(current.normalize()).scale(0.1)).add(chaosVec).normalize().scale(target.getDeltaMovement().length()).scale(0.6));
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        var level = getCommandSenderWorld();

        if (!(result.getEntity() instanceof LivingEntity target) || level.isClientSide())
            return;

        target.hurt(level.damageSources().thrown(this, this.getOwner()), getDamage());
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        //  discard();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE, 0F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putFloat("damage", getDamage());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        setDamage(tag.getFloat("damage"));
    }

    public void setDamage(float heal) {
        this.getEntityData().set(DAMAGE, heal);
    }

    public float getDamage() {
        return this.getEntityData().get(DAMAGE);
    }
}
