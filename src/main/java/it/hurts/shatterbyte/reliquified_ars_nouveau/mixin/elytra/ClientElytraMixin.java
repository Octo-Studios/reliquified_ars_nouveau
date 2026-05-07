package it.hurts.shatterbyte.reliquified_ars_nouveau.mixin.elytra;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.body.WingWildStalkerItem;
import it.hurts.shatterbyte.reliquified_ars_nouveau.network.NetworkHandler;
import it.hurts.shatterbyte.reliquified_ars_nouveau.network.packets.C2SWingStormElectricityPacket;
import it.hurts.shatterbyte.reliquified_ars_nouveau.network.packets.C2SWingWildStalkerDashPacket;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class ClientElytraMixin {
    private static long ran$lastJumpPressTick = -1000L;
    private static boolean ran$wasJumpPressed = false;

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;canElytraFly(Lnet/minecraft/world/entity/LivingEntity;)Z", remap = false))
    public boolean elytraOverride(boolean original) {
        var player = (LocalPlayer) (Object) (this);

        if (EntityUtils.findEquippedCurio(player, ItemRegistry.WING_OF_TH_WILD_STALKER.value()).getItem() instanceof WingWildStalkerItem
                && !player.isFallFlying() && !player.mayFly())
            return true;

        return original;
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void ran$dashOnDoubleJump(CallbackInfo ci) {
        var player = (LocalPlayer) (Object) this;
        var wingStack = EntityUtils.findEquippedCurio(player, ItemRegistry.WING_OF_TH_WILD_STALKER.value());
        var hasWing = wingStack.getItem() instanceof WingWildStalkerItem;

        if (!hasWing) {
            ran$wasJumpPressed = false;
            return;
        }

        if (hasWing && player.isFallFlying() && wingStack.getItem() instanceof WingWildStalkerItem wingItem) {
            var relicData = wingItem.getRelicData(player, wingStack);
            var ability = relicData.getAbilitiesData().getAbilityData("wild_flight");
            var synergy = relicData.getAbilitiesData().getSynergyData("electricity");

            if (synergy.isUnlocked() && !synergy.getMode().equals("disabled"))
                NetworkHandler.sendToServer(new C2SWingStormElectricityPacket());
        }

        if (!player.isFallFlying() || player.mayFly() || !hasWing) {
            ran$wasJumpPressed = false;
            return;
        }

        var isJumpPressed = player.input.jumping;

        if (isJumpPressed && !ran$wasJumpPressed) {
            var gameTime = player.level().getGameTime();

            if (gameTime - ran$lastJumpPressTick <= 7L && wingStack.getItem() instanceof WingWildStalkerItem wingItem && player.isFallFlying() && !player.mayFly()) {
                var relicData = wingItem.getRelicData(player, wingStack);
                var ability = relicData.getAbilitiesData().getAbilityData("wild_flight");
                var totalStrength = 0D;

                for (var stack : EntityUtils.findEquippedCurios(player, ItemRegistry.WING_OF_TH_WILD_STALKER.get())) {
                    if (!(stack.getItem() instanceof WingWildStalkerItem item))
                        continue;

                    var stackAbility = item.getRelicData(player, stack).getAbilitiesData().getAbilityData("wild_flight");

                    if (!stackAbility.getRankModifierData("aerial_dash").isEnabled())
                        continue;

                    var cooldownUntil = stack.getOrDefault(RANDataComponentRegistry.WING_WILD_STALKER_DASH_COOLDOWN_UNTIL.get(), 0L);

                    if (cooldownUntil > gameTime)
                        continue;

                    var dashStrength = Math.max(0D, stackAbility.getStatData("dash_strength").getValue());

                    if (dashStrength <= 0D)
                        continue;

                    totalStrength += dashStrength;
                }

                if (totalStrength > 0D)
                    player.setDeltaMovement(player.getDeltaMovement().add(player.getLookAngle().normalize().scale(totalStrength)));

                PacketDistributor.sendToServer(new C2SWingWildStalkerDashPacket());
            }

            ran$lastJumpPressTick = gameTime;
        }

        ran$wasJumpPressed = isJumpPressed;
    }
}

