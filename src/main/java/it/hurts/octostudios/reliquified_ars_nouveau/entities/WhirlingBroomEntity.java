package it.hurts.octostudios.reliquified_ars_nouveau.entities;

import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.back.WhirlingBroomItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class WhirlingBroomEntity extends Mob {
    protected static EntityDataAccessor<Integer> DATA_ID = SynchedEntityData.defineId(WhirlingBroomEntity.class, EntityDataSerializers.INT);
    private float wobbleAngle = 0;

    public WhirlingBroomEntity(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();

        var owner = (Player) getFirstPassenger();

        if (owner == null) {
            if (tickCount >= 5)
                discard();
        } else {
            var stack = EntityUtils.findEquippedCurio(owner, ItemRegistry.WHIRLING_BROOM.value());

            if (!(stack.getItem() instanceof WhirlingBroomItem relic)) {
                discard();

                return;
            }

            var level = getCommandSenderWorld();

            if (!level.isClientSide()) {
                if (owner.tickCount % 100 == 0)
                    heal(1);

                if (tickCount % 20 == 0 && getKnownMovement().length() >= 0.2)
                    relic.spreadRelicExperience(owner, stack, 1);
            }

            setYRot(owner.getYRot());
            setXRot(owner.getXRot());
            fallDistance = 0;

            yRotO = yBodyRot = yHeadRot = getYRot();

            if (getDeltaMovement().length() <= 0.15) {
                wobbleAngle += 0.1F;

                var look = getLookAngle().normalize();
                var side = new Vec3(-look.z, 0, look.x).normalize();

                var newX = getX() + side.x * Math.sin(wobbleAngle) * 0.05;
                var newY = getY() + Math.cos(wobbleAngle * 2) * 0.02;
                var newZ = getZ() + side.z * Math.sin(wobbleAngle) * 0.05;

                if (level.noCollision(this, getBoundingBox().move(newX - getX(), newY - getY(), newZ - getZ())))
                    setPos(newX, newY, newZ);
            }

            if (getDeltaMovement().length() < 0.6)
                return;

            var modifier = relic.getToggled(stack) ? 1.3 : 0.7;

            for (int i = 0; i < (relic.getToggled(stack) ? 30 : 10); i++) {
                double offsetX = (random.nextDouble() - 0.5) * modifier;
                double offsetZ = (random.nextDouble() - 0.5) * modifier;

                level.addParticle(ParticleUtils.constructSimpleSpark(new Color(random.nextInt(50), 80 + random.nextInt(175), random.nextInt(50)), 0.3F, 20, 0.7F),
                        (getX() + offsetX) - getDeltaMovement().x, getY() + (random.nextDouble() - 0.3F), (getZ() + offsetZ) - getDeltaMovement().z, 0, 0, 0);
            }

        }
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        return (LivingEntity) getFirstPassenger();
    }

    @Override
    public @NotNull Vec3 getPassengerRidingPosition(@NotNull Entity entity) {
        return super.getPassengerRidingPosition(entity).add(0, -0.3, 0);
    }

    @Override
    public void die(DamageSource damageSource) {
        var owner = (Player) getFirstPassenger();
        var stack = EntityUtils.findEquippedCurio(owner, ItemRegistry.WHIRLING_BROOM.value());

        if (!(stack.getItem() instanceof WhirlingBroomItem relic))
            return;

        relic.setAbilityCooldown(stack, "broom", 1200);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);

        builder.define(DATA_ID, 0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        if (compound.contains("BroomMaxHealth"))
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(compound.getFloat("BroomMaxHealth"));

        if (compound.contains("BroomCurrentHealth"))
            this.setHealth(compound.getFloat("BroomCurrentHealth"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        compound.putFloat("BroomMaxHealth", this.getMaxHealth());

        var owner = (Player) getFirstPassenger();
        var stack = EntityUtils.findEquippedCurio(owner, ItemRegistry.WHIRLING_BROOM.value());

        if (stack.getItem() instanceof WhirlingBroomItem relic && relic.getHealth(stack) > 0)
            compound.putFloat("BroomCurrentHealth", relic.getHealth(stack));
        else
            compound.putFloat("BroomCurrentHealth", this.getHealth());
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    protected boolean canRide(Entity entity) {
        return entity instanceof Player;
    }
}
