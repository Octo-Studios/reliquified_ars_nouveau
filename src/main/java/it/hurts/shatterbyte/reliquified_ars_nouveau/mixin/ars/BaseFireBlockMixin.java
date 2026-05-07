package it.hurts.shatterbyte.reliquified_ars_nouveau.mixin.ars;

import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.bracelet.FlamingBracerItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseFireBlock.class)
public class BaseFireBlockMixin {
    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    private void cancelMagicFireIgnitionForFlamingBracer(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (!(entity instanceof ServerPlayer player) || !state.is(BlockRegistry.MAGIC_FIRE.get()))
            return;

        var stacks = EntityUtils.findEquippedCurios(player, ItemRegistry.FLAMING_BRACER.get()).stream()
                .filter(relicStack -> relicStack.getItem() instanceof FlamingBracerItem)
                .toList();

        for (var stack : stacks) {
            if (!(stack.getItem() instanceof FlamingBracerItem item))
                continue;

            var ability = item.getRelicData(player, stack).getAbilitiesData().getAbilityData("pyroclastic");

            if (ability.getRankModifierData("fire_immunity").isEnabled()) {
                ci.cancel();
                return;
            }
        }
    }
}

