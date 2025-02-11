package it.hurts.octostudios.reliquified_ars_nouveau.mixin;

import com.hollingsworth.arsnouveau.common.entity.BubbleEntity;
import it.hurts.octostudios.octolib.modules.particles.OctoRenderManager;
import it.hurts.octostudios.octolib.modules.particles.trail.TrailProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BubbleEntity.class)
public abstract class BubbleEntityMixin extends Entity implements TrailProvider {
    public BubbleEntityMixin(EntityType<? extends Entity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();

        if (this.getPersistentData().getBoolean("canTrail"))
            OctoRenderManager.registerProvider(this);
    }

    @Override
    public Vec3 getTrailPosition(float partialTicks) {
        var entityPos = this.getPosition(1);
        return new Vec3(entityPos.x, entityPos.y + 0.05F, entityPos.z);
    }

    @Override
    public int getTrailUpdateFrequency() {
        return 1;
    }

    @Override
    public boolean isTrailAlive() {
        return isAlive();
    }

    @Override
    public boolean isTrailGrowing() {
        return tickCount > 2;
    }

    @Override
    public int getTrailMaxLength() {
        return 15;
    }

    @Override
    public int getTrailFadeInColor() {
        return 0xFF4287f5;
    }

    @Override
    public int getTrailFadeOutColor() {
        return 0x8093b6ed;
    }

    @Override
    public double getTrailScale() {
        return 0.2F;
    }
}
