package it.hurts.octostudios.reliquified_ars_nouveau.entities;

import com.hollingsworth.arsnouveau.client.particle.GlowParticleData;
import com.hollingsworth.arsnouveau.client.particle.ParticleColor;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.sync.S2CEntityMotionPacket;
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
        double targetSpeed;
        float curveModifier = 1.0F;

        if (persistentData.contains("TargetUUID")
                && ((ServerLevel) level).getEntity(persistentData.getUUID("TargetUUID")) instanceof Projectile projectileTarget) {
            targetVec = projectileTarget.position();
            targetSpeed = projectileTarget.getDeltaMovement().length() * 0.8;
        } else {
            targetSpeed = 1F;

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

        var toTarget = targetVec.subtract(this.position());
        var directionToTarget = toTarget.normalize();

        int bowIndex = persistentData.getInt("BowIndex");
        int time = this.tickCount + bowIndex * 20;

        var perpendicular1 = directionToTarget.cross(new Vec3(0, 1, 0)).normalize();
        var perpendicular2 = directionToTarget.cross(perpendicular1).normalize();

        double curveAmount = Math.min(1.0, toTarget.length() / 10.0);

        double phase = bowIndex * 0.4;
        double freq1 = 0.08 + (bowIndex % 5) * 0.01;
        double freq2 = 0.10 + ((bowIndex + 3) % 5) * 0.015;
        double amp = 0.4 * curveAmount * (0.8 + (bowIndex % 3) * 0.1);

        var spiralOffset = perpendicular1.scale(Math.sin(time * freq1 + phase) * amp)
                .add(perpendicular2.scale(Math.cos(time * freq2 + phase * 1.3) * amp));

        var finalDirection = directionToTarget.add(spiralOffset.scale(curveModifier)).normalize();

        NetworkHandler.sendToClientsTrackingEntity(new S2CEntityMotionPacket(this.getId(), this.getDeltaMovement()), this);

        this.setDeltaMovement(finalDirection.scale(targetSpeed));
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
