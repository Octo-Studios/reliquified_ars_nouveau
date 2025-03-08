package it.hurts.octostudios.reliquified_ars_nouveau.mixin;

import com.hollingsworth.arsnouveau.api.spell.SpellResolver;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.ScribbleRelicItem;
import it.hurts.octostudios.reliquified_ars_nouveau.items.hands.ArchmagesGloveItem;
import it.hurts.octostudios.reliquified_ars_nouveau.items.ring.RingOfThriftItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpellResolver.class)
public class SpellResolverMixin {
    @Inject(method = "expendMana", at = @At(value = "HEAD"))
    private void onEntityInside(CallbackInfo ci) {
        var resolver = (SpellResolver) (Object) this;

        if (!(resolver.spellContext.getCaster() instanceof LivingCaster livingEntity))
            return;

        var entity = livingEntity.livingEntity;
        var stack = EntityUtils.findEquippedCurio(entity, ItemRegistry.RING_OF_THRIFT.value());
        var casterTool = resolver.spellContext.getCasterTool().getItem();

        if (entity.getCommandSenderWorld().isClientSide() || !(stack.getItem() instanceof RingOfThriftItem relic) || !relic.getToggled(stack)
                || casterTool instanceof ScribbleRelicItem || casterTool instanceof ArchmagesGloveItem)
            return;

        relic.spreadRelicExperience(entity, stack, 1);
        relic.setToggled(stack, false);
    }
}
