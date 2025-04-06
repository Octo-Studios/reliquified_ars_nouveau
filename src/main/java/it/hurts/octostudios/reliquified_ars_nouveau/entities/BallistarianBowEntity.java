package it.hurts.octostudios.reliquified_ars_nouveau.entities;

import it.hurts.octostudios.reliquified_ars_nouveau.init.EntityRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.bracelet.BallistarianBracerItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

@Getter
@Setter
public class BallistarianBowEntity extends Mob implements GeoEntity, OwnableEntity {
    private static final EntityDataAccessor<Integer> TARGET_ID = SynchedEntityData.defineId(BallistarianBowEntity.class, EntityDataSerializers.INT);
    private LivingEntity target;
    private UUID ownerUUID;

    public BallistarianBowEntity(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);

        this.noPhysics = true;
    }


    @Override
    public void tick() {
        super.tick();
        if (getCommandSenderWorld().isClientSide()) return;

        if (getOwner() == null) {
            discard();
            return;
        }

        var stack = EntityUtils.findEquippedCurio(getOwner(), ItemRegistry.BALLISTARIAN_BRACER.value());
        if (!(stack.getItem() instanceof BallistarianBracerItem relic)) {
            discard();
            return;
        }

        var entities = relic.getEntities(stack);
        int total = entities.size();
        int formationIndex = entities.indexOf(this.getUUID());
        if (formationIndex < 0) {
            discard();
            return;
        }

//        double arcRadians = Math.toRadians(80); // увеличено пространство между луками
//        double baseAngle = total % 2 == 0
//                ? (formationIndex - (total / 2.0 - 0.5)) * (arcRadians / (total - 1))
//                : (formationIndex - (total / 2.0)) * (arcRadians / (total - 1));
//
//        var lookVec = getOwner().getLookAngle().normalize();
//        var rightVec = new Vec3(-lookVec.z, 0, lookVec.x);
//        var offset = lookVec.scale(-Math.cos(baseAngle) * 1.5)
//                .add(rightVec.scale(Math.sin(baseAngle) * 1.5));
//
//        double heightOffset = 0.6;
//        if (!(total % 2 != 0 && formationIndex == total / 2)) {
//            heightOffset -= 0.15 * Math.abs(formationIndex - total / 2);
//        }
//
//        var targetPos = getOwner().position().add(
//                offset.x,
//                getOwner().getEyeY() - getOwner().getY() + heightOffset,
//                offset.z
//        );
//
//        this.setDeltaMovement(
//                targetPos.subtract(this.position()).scale(
//                        Mth.clamp(getOwner().getDeltaMovement().length() * 1.5, 0.1, 0.75)
//                )
//        );
//
//        this.setYRot(lerpRotation(this.getYRot(), getOwner().getYRot(), 0.2f));
//        this.setXRot(lerpRotation(this.getXRot(), getOwner().getXRot(), 0.2f));
//        this.yBodyRot = this.getYRot();
    }


    private float lerpRotation(float current, float target, float speed) {
        float delta = Mth.wrapDegrees(target - current);
        return current + delta * speed;
    }

    private void fireArrow(Level level) {
        if (level.isClientSide() || target == null) return;

        var stack = EntityUtils.findEquippedCurio(getOwner(), ItemRegistry.BALLISTARIAN_BRACER.value());

        if (!(stack.getItem() instanceof BallistarianBracerItem relic)) return;

        var direction = new Vec3(target.getX() - this.getX(), target.getY() + target.getBbHeight() - this.getY(), target.getZ() - this.getZ()).normalize();
        var arrow = new Arrow(EntityType.ARROW, level);

        arrow.setPos(this.getX(), this.getY() + 0.3F, this.getZ());
        arrow.shoot(direction.x, direction.y, direction.z, 2.5F, 1.0F);
        arrow.setBaseDamage(Math.round(relic.getStatValue(stack, "striker", "damage")));
        arrow.setBaseDamage(1);

        level.addFreshEntity(arrow);

        target.invulnerableTime = 0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TARGET_ID, 0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(TARGET_ID, tag.getInt("TargetID"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        tag.putInt("TargetID", this.entityData.get(TARGET_ID));
    }

    @Nullable
    public LivingEntity getOwner() {
        if (getOwnerUUID() == null) return null;

        return getCommandSenderWorld().getPlayerByUUID(this.ownerUUID);
    }

    public void setTarget(LivingEntity target) {
        this.target = target;
        this.entityData.set(TARGET_ID, target.getId());
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return GeckoLibUtil.createInstanceCache(this);
    }

    @Override
    public @Nullable UUID getOwnerUUID() {
        return ownerUUID;
    }

    @EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
    public static class RegistryAttributesHandlerEvent {
        @SubscribeEvent
        public static void onRegisterAttributes(EntityAttributeCreationEvent event) {
            event.put(EntityRegistry.BALLISTARIAN_BOW.get(), Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10F).build());
        }
    }
}
