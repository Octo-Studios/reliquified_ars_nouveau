package it.hurts.octostudios.reliquified_ars_nouveau.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WhirlingBroomEntity extends Mob {
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID =
            SynchedEntityData.defineId(WhirlingBroomEntity.class, EntityDataSerializers.BYTE);

    public WhirlingBroomEntity(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();

        if(getCommandSenderWorld().isClientSide())
            return;

        if (getPassengers().isEmpty()) {
            discard();
            return;
        }

        if (!(getFirstPassenger() instanceof Player player))
            return;

        controlBroom(player);
    }

    private void controlBroom(Player player) {

    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLAGS_ID, (byte) 0);
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected boolean canRide(Entity entity) {
        return entity instanceof Player;
    }
}
