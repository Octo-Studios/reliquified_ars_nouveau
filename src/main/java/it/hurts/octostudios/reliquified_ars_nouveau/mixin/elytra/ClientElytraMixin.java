package it.hurts.octostudios.reliquified_ars_nouveau.mixin.elytra;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.body.WingWildStalkerItem;
import it.hurts.octostudios.reliquified_ars_nouveau.network.packets.WingStartFlyPacket;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class ClientElytraMixin {
    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;canElytraFly(Lnet/minecraft/world/entity/LivingEntity;)Z", remap = false))
    public boolean elytraOverride(boolean original) {
        var player = (LocalPlayer) (Object) (this);
        var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.WING_OF_TH_WILD_STALKER.value());

        if (stack.getItem() instanceof WingWildStalkerItem relic && !player.isFallFlying()) {
            NetworkHandler.sendToServer(new WingStartFlyPacket());

            return relic.getToggled(stack);
        }

        return original;
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ServerboundPlayerCommandPacket;<init>(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/network/protocol/game/ServerboundPlayerCommandPacket$Action;)V", shift = At.Shift.AFTER))
    private void afterElytraStart(CallbackInfo ci) {
        var player = (LocalPlayer) (Object) (this);
        var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.WING_OF_TH_WILD_STALKER.value());

        if (stack.getItem() instanceof WingWildStalkerItem relic && relic.getToggled(stack))
            player.setDeltaMovement(player.getDeltaMovement().x, 1, player.getKnownMovement().z);
    }
}