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
import org.apache.commons.lang3.tuple.Pair;
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

        if (getCommandSenderWorld().isClientSide())
            return;

        var owner = getOwner();

        if (owner == null) {
            discard();

            return;
        }

        var stack = EntityUtils.findEquippedCurio(owner, ItemRegistry.BALLISTARIAN_BRACER.value());

        if (!(stack.getItem() instanceof BallistarianBracerItem relic) || !relic.getEntities(stack).contains(this.getUUID())) {
            discard();

            return;
        }

        var index = relic.getEntities(stack).indexOf(this.getUUID());
        var maxCount = (int) Math.round(relic.getStatValue(stack, "striker", "count"));
        var normalizedLookAngle = owner.getLookAngle().normalize();

        var pair = calculateOffsetAndHeight(index, maxCount, normalizedLookAngle);
        var offset = pair.getLeft();
        var targetPosition = owner.position().add(offset.x, owner.getEyeY() - owner.getY() + pair.getRight(), offset.z);

        this.setDeltaMovement(targetPosition.subtract(this.position()).scale(Mth.clamp(owner.getKnownMovement().length() * 1.5, 0.1, 0.75)));

        rotatedBowAngle(normalizedLookAngle, maxCount, index);
    }

    public void rotatedBowAngle(Vec3 vec, int maxCount, int index) {
        var owner = getOwner();

        if (owner == null)
            return;

        var radians = Math.toRadians(index % 2 == 0 ? 45D : -45D);

        var cos = Math.cos(radians);
        var sin = Math.sin(radians);

        var vecAngle = new Vec3(vec.x * cos - vec.z * sin, vec.y, vec.x * sin + vec.z * cos);
        var additionalBow = index == 0 && maxCount % 2 != 0;

        this.setYRot(additionalBow ? owner.getYRot() : (float) Mth.atan2(vecAngle.z, vecAngle.x) * (180F / (float) Math.PI) - 90F);
        this.setXRot(additionalBow ? 45F : (float) -(Mth.atan2(vecAngle.y, Math.sqrt(vecAngle.x * vecAngle.x + vecAngle.z * vecAngle.z)) * (180F / (float) Math.PI)));

        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
    }

    public Pair<Vec3, Double> calculateOffsetAndHeight(int index, int total, Vec3 lookVec) {
        var radius = 1;

        if (lookVec.y > 0.5) {
            double angle = Math.toRadians(360.0 * index / total);

            return Pair.of(new Vec3(Math.cos(angle), 0, Math.sin(angle)).scale(radius), 0.3);
        } else if (lookVec.y < -0.5) {
            double angle = Math.toRadians(360.0 * index / total);

            return Pair.of(new Vec3(Math.cos(angle), 0, Math.sin(angle)).scale(radius), -1.8);
        } else {
            var backVec = lookVec.scale(-1);
            var rightVec = new Vec3(-lookVec.z, 0, lookVec.x);
            var isEven = total % 2 == 0;
            var offset = Vec3.ZERO;
            var heightOffset = 0D;

            if (index == 0 && !isEven) {
                offset = backVec.scale(radius);
                heightOffset = 0.6;
            } else {
                int side = (index % 2 == 0) ? 1 : -1;
                int indexFromCenter = isEven ? index / 2 + 1 : (index + 1) / 2;

                offset = backVec.scale(radius * 0.9).add(rightVec.scale(side * indexFromCenter * 0.8));
                heightOffset = 0.6 - (0.15 * indexFromCenter);
            }

            return Pair.of(offset, heightOffset);
        }
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
