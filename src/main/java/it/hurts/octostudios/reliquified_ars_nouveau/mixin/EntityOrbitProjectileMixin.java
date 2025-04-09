package it.hurts.octostudios.reliquified_ars_nouveau.mixin;

import com.hollingsworth.arsnouveau.common.entity.EntityOrbitProjectile;
import com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.charm.EmblemOfDevotionItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityOrbitProjectile.class)
public abstract class EntityOrbitProjectileMixin extends EntityProjectileSpell {
    public EntityOrbitProjectileMixin(EntityType<? extends EntityProjectileSpell> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void onEntityInside(CallbackInfo ci) {
        var orbit = (EntityOrbitProjectile) (Object) this;

        if (orbit.spellResolver == null)
            return;

        var player = orbit.getOwner();
        var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.EMBLEM_OF_DEVOTION.value());

        if (!(orbit.spellResolver.spellContext.getCasterTool().getItem() instanceof EmblemOfDevotionItem) || !stack.isEmpty())
            return;

        orbit.getCommandSenderWorld().broadcastEntityEvent(orbit, (byte) 3);
        orbit.remove(Entity.RemovalReason.DISCARDED);
    }

    @Inject(method = "onHit", at = @At(value = "HEAD"))
    protected void onHitEntity(HitResult result, CallbackInfo ci) {
        var entity = (EntityOrbitProjectile) (Object) this;

        if (entity.spellResolver == null || !(result instanceof EntityHitResult))
            return;

        var player = entity.getOwner();
        var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.EMBLEM_OF_DEVOTION.value());

        if (!(entity.spellResolver.spellContext.getCasterTool().getItem() instanceof EmblemOfDevotionItem relic))
            return;

        relic.spreadRelicExperience((LivingEntity) player, stack, 1);
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);

        var entity = (EntityOrbitProjectile) (Object) this;

        if (entity.spellResolver == null)
            return;

        var player = entity.getOwner();
        var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.EMBLEM_OF_DEVOTION.value());

        if (!(entity.spellResolver.spellContext.getCasterTool().getItem() instanceof EmblemOfDevotionItem relic))
            return;

        relic.addCharges(stack, -1);
    }
}
