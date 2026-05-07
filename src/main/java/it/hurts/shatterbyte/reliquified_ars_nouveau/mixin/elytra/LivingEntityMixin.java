package it.hurts.shatterbyte.reliquified_ars_nouveau.mixin.elytra;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.body.WingWildStalkerItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @ModifyExpressionValue(method = "updateFallFlying", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;canElytraFly(Lnet/minecraft/world/entity/LivingEntity;)Z", remap = false))
    public boolean elytraOverride(boolean original) {
        if (!((LivingEntity) (Object) this instanceof Player player))
            return original;

        if (EntityUtils.findEquippedCurio(player, ItemRegistry.WING_OF_TH_WILD_STALKER.value()).getItem() instanceof WingWildStalkerItem && !player.mayFly())
            return true;

        return original;
    }

    @ModifyExpressionValue(method = "updateFallFlying", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;elytraFlightTick(Lnet/minecraft/world/entity/LivingEntity;I)Z", remap = false))
    public boolean elytraValidOverride(boolean original) {
        if (!((LivingEntity) (Object) this instanceof Player player))
            return original;

        if (EntityUtils.findEquippedCurio(player, ItemRegistry.WING_OF_TH_WILD_STALKER.value()).getItem() instanceof WingWildStalkerItem && !player.mayFly())
            return true;

        return original;
    }

    @Inject(method = "travel", at = @At("TAIL"))
    private void ran$applyWingFlightSpeed(Vec3 movementInput, CallbackInfo ci) {
        if (!((LivingEntity) (Object) this instanceof Player player) || player.mayFly())
            return;

        var hasUsableWing = false;
        var canContinueFlight = false;
        var forceDescent = false;

        for (var stack : EntityUtils.findEquippedCurios(player, ItemRegistry.WING_OF_TH_WILD_STALKER.get())) {
            if (!(stack.getItem() instanceof WingWildStalkerItem item))
                continue;

            var ability = item.getRelicData(player, stack).getAbilitiesData().getAbilityData("wild_flight");
            hasUsableWing = true;
            var maxFlightDurationTicks = Math.max(0, (int) Math.round(ability.getStatData("flight_duration").getValue() * 20D));

            if (!player.isFallFlying()) {
                stack.set(RANDataComponentRegistry.WING_WILD_STALKER_FLIGHT_TICKS.get(), 0);
                continue;
            }

            var flightTicks = stack.getOrDefault(RANDataComponentRegistry.WING_WILD_STALKER_FLIGHT_TICKS.get(), 0) + 1;
            stack.set(RANDataComponentRegistry.WING_WILD_STALKER_FLIGHT_TICKS.get(), flightTicks);

            if (maxFlightDurationTicks > 0 && flightTicks <= maxFlightDurationTicks)
                canContinueFlight = true;
            else
                forceDescent = true;
        }

        if (!hasUsableWing || !player.isFallFlying() || canContinueFlight || !forceDescent)
            return;

        var motion = player.getDeltaMovement();

        // After duration ends, block only natural "pull-up" attempts.
        // External boosts (fireworks/modded impulses) usually produce much larger vertical speed and should pass through.
        if (player.getXRot() < -2F && motion.y > 0D && motion.y <= 0.35D)
            player.setDeltaMovement(motion.x, 0D, motion.z);
    }
}
