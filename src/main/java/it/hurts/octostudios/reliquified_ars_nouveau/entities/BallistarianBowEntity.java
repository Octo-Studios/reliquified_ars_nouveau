package it.hurts.octostudios.reliquified_ars_nouveau.entities;

import it.hurts.octostudios.reliquified_ars_nouveau.init.EntityRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.bracelet.BallistarianBracerItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
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
        if (owner == null) return;

        int localIndex = index % 7;

        int remaining = maxCount - (index / 7) * 7;
        int rowCount = Math.min(remaining, 7);
        boolean isRowEven = rowCount % 2 == 0;

        if (isRowEven && localIndex == 3) {
            this.setYRot(owner.getYRot());
            this.setXRot(0);

            this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();

            return;
        }

        var radians = Math.toRadians(localIndex % 2 == 0 ? 30D : -30D);
        var cos = Math.cos(radians);
        var sin = Math.sin(radians);

        var vecAngle = new Vec3(
                vec.x * cos - vec.z * sin,
                vec.y,
                vec.x * sin + vec.z * cos
        );

        boolean additionalBow = localIndex == 0 && rowCount % 2 != 0;

        float yaw = additionalBow
                ? owner.getYRot()
                : (float) Math.toDegrees(Math.atan2(vecAngle.z, vecAngle.x)) - 90F;

        float pitch = (float) -Math.toDegrees(Math.atan2(
                vecAngle.y,
                Math.sqrt(vecAngle.x * vecAngle.x + vecAngle.z * vecAngle.z)
        ));

        this.setYRot(yaw);
        this.setXRot(pitch);

        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
    }

    public Pair<Vec3, Double> calculateOffsetAndHeight(int index, int total, Vec3 lookVec) {
        var radius = 3;

        if (lookVec.y > 0.65) {
            double angle = Math.toRadians(360.0 * index / total);
            return Pair.of(new Vec3(Math.cos(angle), 0, Math.sin(angle)).scale(radius + 1), 0.3);
        } else if (lookVec.y < -0.8) {
            double angle = Math.toRadians(360.0 * index / total);
            return Pair.of(new Vec3(Math.cos(angle), 0, Math.sin(angle)).scale(radius + 1), -1.8);
        } else {
            var backVec = lookVec.scale(-1);
            var offset = Vec3.ZERO;
            var heightOffset = 0D;

            radius /= 2;

            int row = index / 7;
            int localIndex = index % 7;

            int remaining = total - row * 7;
            int rowCount = Math.min(remaining, 7);
            boolean isRowEven = rowCount % 2 == 0;

            double rowYOffset = 1.3 * row;

            if (localIndex == 0 && !isRowEven) {
                // центральный лук
                offset = backVec.scale(radius);
                heightOffset = 0.6;
            } else {
                int side = (localIndex % 2 == 0) ? 1 : -1;
                int indexFromCenter = isRowEven ? localIndex / 2 + 1 : (localIndex + 1) / 2;

                offset = backVec.scale(radius * 0.5)
                        .add(new Vec3(-lookVec.z, 0, lookVec.x).scale(side * indexFromCenter * 0.8));
                heightOffset = 0.6 - (0.15 * indexFromCenter);
            }

            heightOffset += rowYOffset;

            return Pair.of(offset, heightOffset);
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        var player = getOwner();

        if (player == null || player.getCommandSenderWorld().isClientSide())
            return;

        var stack = EntityUtils.findEquippedCurio(getOwner(), ItemRegistry.BALLISTARIAN_BRACER.value());

        if (!(stack.getItem() instanceof BallistarianBracerItem relic))
            return;

        relic.addCooldown(stack, (int) Math.round(relic.getStatValue(stack, "striker", "cooldown") * 20));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        var entity = source.getEntity();

        if (entity != null && entity.getUUID().equals(this.ownerUUID))
            return false;

        return super.hurt(source, amount);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.hasUUID("OwnerUUID"))
            this.ownerUUID = tag.getUUID("OwnerUUID");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (ownerUUID != null)
            tag.putUUID("OwnerUUID", ownerUUID);
    }

    @Nullable
    public LivingEntity getOwner() {
        if (getOwnerUUID() == null) return null;

        return getCommandSenderWorld().getPlayerByUUID(this.ownerUUID);
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
