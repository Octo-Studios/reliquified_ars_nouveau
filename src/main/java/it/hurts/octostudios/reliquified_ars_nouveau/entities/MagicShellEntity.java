package it.hurts.octostudios.reliquified_ars_nouveau.entities;

import com.hollingsworth.arsnouveau.client.particle.GlowParticleData;
import com.hollingsworth.arsnouveau.client.particle.ParticleColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
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
            level.addParticle(GlowParticleData.createData(new ParticleColor(255, 25, 180)), true, this.xo + deltaX * coeff, this.yo + deltaY * coeff + this.getBbHeight() / 2, this.zo + deltaZ * coeff,
                    0.0125F * (this.random.nextFloat() - 0.5F), 0.0125F * (this.random.nextFloat() - 0.5F), 0.0125F * (this.random.nextFloat() - 0.5F));

        if (level.isClientSide())
            return;

        var persistentData = getPersistentData();

        if (tickCount >= 300)
            return;

        var targetVec = Vec3.ZERO;
        var curveFactor = 1;
        double targetSpeed;

        if (((ServerLevel) level).getEntity(persistentData.getUUID("TargetUUID")) instanceof Projectile projectileTarget) {
            targetVec = projectileTarget.position();
            targetSpeed = projectileTarget.getDeltaMovement().length() * 0.8;
        } else {
            curveFactor = 0;
            targetSpeed = 0.6F;

            if (persistentData.contains("HitEntity")) {
                if (!(((ServerLevel) level).getEntity(persistentData.getUUID("HitEntity")) instanceof LivingEntity targetEntity) || !targetEntity.isAlive()) {

                    discard();

                    return;
                }

                targetVec = targetEntity.position();
            }

            if (persistentData.contains("HitPosX") && persistentData.contains("HitPosY") && persistentData.contains("HitPosZ")) {
                var hitX = persistentData.getDouble("HitPosX");
                var hitY = persistentData.getDouble("HitPosY");
                var hitZ = persistentData.getDouble("HitPosZ");

                targetVec = new Vec3(hitX, hitY, hitZ);
            }
        }

        Vec3 currentPos = this.position();
        Vec3 toTarget = targetVec.subtract(currentPos);
        double distance = toTarget.length();

        Vec3 directionToTarget = toTarget.normalize();

        int bowIndex = persistentData.getInt("BowIndex");
        int time = this.tickCount + bowIndex * 20;

        Vec3 up = new Vec3(0, 1, 0);
        Vec3 perpendicular1 = directionToTarget.cross(up).normalize();
        Vec3 perpendicular2 = directionToTarget.cross(perpendicular1).normalize();

        double curveAmount = Math.min(1.0, distance / 10.0) * curveFactor;

        Vec3 spiralOffset = perpendicular1.scale(Math.sin(time * 0.1 + bowIndex) * 0.4 * curveAmount)
                .add(perpendicular2.scale(Math.cos(time * 0.12 + bowIndex) * 0.4 * curveAmount));

        double speed = Mth.clamp(distance * 0.4, 0.1, targetSpeed);

        double interpolationFactor = Math.min(distance / 10.0, 1.0);

        Vec3 currentMovement = this.getDeltaMovement();
        Vec3 targetMovement = directionToTarget.add(spiralOffset).normalize().scale(speed);

        Vec3 newMovement = currentMovement.scale(1 - interpolationFactor).add(targetMovement.scale(interpolationFactor));

        this.setDeltaMovement(newMovement);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        var level = getCommandSenderWorld();

        if (!(result.getEntity() instanceof LivingEntity target) || level.isClientSide())
            return;

        target.invulnerableTime = 0;
        target.hurt(level.damageSources().thrown(this, this.getOwner()), getDamage());

        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        var persistentData = getPersistentData();
        var currentPosition = this.position();

        if (persistentData.contains("HitPosX") && persistentData.contains("HitPosY") && persistentData.contains("HitPosZ")) {
            var hitX = persistentData.getDouble("HitPosX");
            var hitY = persistentData.getDouble("HitPosY");
            var hitZ = persistentData.getDouble("HitPosZ");

            if (currentPosition.distanceTo(new Vec3(hitX, hitY, hitZ)) <= 0.5F)
                discard();
        }
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
