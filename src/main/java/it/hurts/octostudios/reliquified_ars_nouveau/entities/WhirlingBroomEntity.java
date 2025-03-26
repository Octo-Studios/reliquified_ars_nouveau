package it.hurts.octostudios.reliquified_ars_nouveau.entities;

import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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

        if (getFirstPassenger() == null) {
            if (tickCount >= 10)
                discard();
        } else {
            setYRot(getFirstPassenger().getYRot());
            fallDistance = 0;

            yRotO = yBodyRot = yHeadRot = getYRot();

            if (getDeltaMovement().length() <= 0.15) {
                wobbleAngle += 0.1F;

                setPos(getX() + Math.sin(wobbleAngle) * 0.05, getY() + Math.cos(wobbleAngle * 2) * 0.02, getZ());
            }

            if (getDeltaMovement().length() < 0.6)
                return;


            for (int i = 0; i < 10; i++) {
                double offsetX = (random.nextDouble() - 0.5) * 0.7;
                double offsetZ = (random.nextDouble() - 0.5) * 0.7;

                getCommandSenderWorld().addParticle(ParticleUtils.constructSimpleSpark(new Color(0, 100 + random.nextInt(150), 0), 0.3F, 20, 0.7F),
                        getX() + offsetX, getY() + 0.4, getZ() + offsetZ, 0, 0, 0);
            }
        }
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        return (LivingEntity) getFirstPassenger();
    }

    @Override
    public @NotNull Vec3 getPassengerRidingPosition(@NotNull Entity entity) {
        return super.getPassengerRidingPosition(entity).add(0, -0.6, 0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);

        builder.define(DATA_ID, 0);
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
