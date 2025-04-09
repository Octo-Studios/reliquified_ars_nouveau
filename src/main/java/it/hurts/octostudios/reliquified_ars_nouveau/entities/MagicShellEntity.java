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

        if (tickCount >= 300 || !persistentData.hasUUID("TargetUUID") || !(((ServerLevel) level).getEntity(persistentData.getUUID("TargetUUID")) instanceof Projectile target))
            return;

        var currentMovement = this.getDeltaMovement();
        var currentPosition = this.position();

        if (persistentData.contains("HitPosX") && persistentData.contains("HitPosY") && persistentData.contains("HitPosZ")
                || persistentData.contains("HitEntity")) {

            Vec3 targetVec;

            if (persistentData.contains("HitEntity")) {
                if (!(((ServerLevel) level).getEntity(persistentData.getUUID("HitEntity")) instanceof LivingEntity targetEntity) || !target.isAlive()) {

                    discard();

                    return;
                }

                targetVec = targetEntity.position();
            } else {
                var hitX = persistentData.getDouble("HitPosX");
                var hitY = persistentData.getDouble("HitPosY");
                var hitZ = persistentData.getDouble("HitPosZ");

                targetVec = new Vec3(hitX, hitY, hitZ);
            }

            var directionToTarget = targetVec.subtract(currentPosition).normalize();

            this.setDeltaMovement(currentMovement.lerp(directionToTarget, Mth.clamp(1 - (position().distanceTo(targetVec) / 6), 0.05, 1.0)).normalize().scale(0.4));
        } else {
            int bowIndex = persistentData.getInt("BowIndex");
            var time = this.tickCount + bowIndex * 20;

            var directionToTarget = target.position().add(0, target.getBbHeight() / 2.0, 0).subtract(currentPosition).normalize();

            var sideVector = directionToTarget.cross(new Vec3(0, 1, 0)).normalize();
            var curveFactor = 0;

            var chaosVec = sideVector.scale(Math.sin(time * 0.1 + bowIndex) * 0.05 * curveFactor)
                    .add(directionToTarget.cross(sideVector).normalize().scale(Math.cos(time * 0.12 + bowIndex) * 0.05 * curveFactor));

            this.setDeltaMovement(currentMovement.normalize()
                    .add(directionToTarget.subtract(currentMovement.normalize()).scale(0.1))
                    .add(chaosVec)
                    .normalize()
                    .scale(target.getDeltaMovement().length())
                    .scale(0.8));
        }
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


            if (currentPosition.distanceTo(new Vec3(hitX, hitY, hitZ)) <= 0.3F)
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
