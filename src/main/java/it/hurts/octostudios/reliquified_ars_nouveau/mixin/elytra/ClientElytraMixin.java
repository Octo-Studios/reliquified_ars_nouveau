package it.hurts.octostudios.reliquified_ars_nouveau.mixin.elytra;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.body.WingWildStalkerItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public class ClientElytraMixin {
    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;canElytraFly(Lnet/minecraft/world/entity/LivingEntity;)Z", remap = false))
    public boolean elytraOverride(boolean original) {
        var player = (LocalPlayer) (Object) (this);

        if (EntityUtils.findEquippedCurio(player, ItemRegistry.WING_OF_TH_WILD_STALKER.value()).getItem() instanceof WingWildStalkerItem
                && !player.isFallFlying() && !player.mayFly())
            return true;

        return original;
    }
}