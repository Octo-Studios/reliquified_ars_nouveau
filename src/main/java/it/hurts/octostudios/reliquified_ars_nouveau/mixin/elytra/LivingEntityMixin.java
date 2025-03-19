package it.hurts.octostudios.reliquified_ars_nouveau.mixin.elytra;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.body.WingWildStalkerItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

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
}
