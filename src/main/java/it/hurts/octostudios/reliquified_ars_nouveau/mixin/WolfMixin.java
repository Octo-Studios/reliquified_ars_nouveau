package it.hurts.octostudios.reliquified_ars_nouveau.mixin;

import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.head.HornOfWildHunterItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Wolf.class)
public class WolfMixin {
    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void onEntityInside(CallbackInfo ci) {
        var wolf = (Wolf) (Object) this;

        if (!(wolf.getOwner() instanceof Player player))
            return;

        for (var stack : EntityUtils.findEquippedCurios(player, ItemRegistry.HORN_OF_THE_WILD_HUNTER.value())) {
            if (stack.getItem() instanceof HornOfWildHunterItem relic && wolf.getPersistentData().getString("summon").equals("spawned")
                    && !relic.getWolves(stack).contains(wolf.getUUID()))
                wolf.discard();
        }
    }
}
