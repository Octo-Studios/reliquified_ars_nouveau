package it.hurts.octostudios.reliquified_ars_nouveau.mixin;

import com.hollingsworth.arsnouveau.common.entity.EntityChimeraProjectile;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.back.SpikedCloakItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityChimeraProjectile.class)
public abstract class ChimeraProjectileMixin {
    @ModifyVariable(method = "onHitEntity", at = @At("STORE"), ordinal = 0)
    private float modifyDamage(float originalDamage) {
        EntityChimeraProjectile spike = (EntityChimeraProjectile) (Object) this;

        if (!(spike.getOwner() instanceof Player player))
            return originalDamage;

        var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.SPIKED_CLOAK.value());

        if (!(stack.getItem() instanceof SpikedCloakItem relic) || !relic.isAbilityUnlocked(stack, "spikes"))
            return originalDamage;

        return (float) (MathUtils.round(relic.getStatValue(stack, "spikes", "damage"), 0) * relic.getCount(stack));
    }

    @Inject(method = "onHitEntity", at = @At("HEAD"), cancellable = true)
    protected void hitEntity(EntityHitResult rayTraceResult, CallbackInfo ci) {
        EntityChimeraProjectile spike = (EntityChimeraProjectile) (Object) this;

        if (!(spike.getOwner() instanceof Player player) || !rayTraceResult.getEntity().getUUID().equals(player.getUUID()))
            return;

        ci.cancel();
    }
}
