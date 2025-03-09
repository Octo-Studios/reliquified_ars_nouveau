package it.hurts.octostudios.reliquified_ars_nouveau.mixin;

import com.hollingsworth.arsnouveau.api.spell.SpellResolver;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.capability.ManaCap;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.ScribbleRelicItem;
import it.hurts.octostudios.reliquified_ars_nouveau.items.hands.ArchmagesGloveItem;
import it.hurts.octostudios.reliquified_ars_nouveau.items.ring.RingOfThriftItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(SpellResolver.class)
public class SpellResolverMixin {
    @Inject(method = "expendMana", at = @At(value = "HEAD"), cancellable = true)
    private void onEntityInside(CallbackInfo ci) {
        var resolver = (SpellResolver) (Object) this;

        if (!(resolver.spellContext.getCaster() instanceof LivingCaster livingEntity))
            return;

        var entity = livingEntity.livingEntity;
        var level = entity.getCommandSenderWorld();
        var random = level.getRandom();

        var stack = EntityUtils.findEquippedCurio(entity, ItemRegistry.RING_OF_THRIFT.value());
        var casterTool = resolver.spellContext.getCasterTool().getItem();

        if (level.isClientSide() || !(stack.getItem() instanceof RingOfThriftItem relic) || !relic.isAbilityUnlocked(stack, "thrift")
                || random.nextDouble() >= relic.getStatValue(stack, "thrift", "chance") || resolver.getResolveCost() > new ManaCap(entity).getCurrentMana()
                || casterTool instanceof ScribbleRelicItem || casterTool instanceof ArchmagesGloveItem)
            return;

        relic.spreadRelicExperience(entity, stack, 1);

        level.playSound(null, entity, SoundEvents.ALLAY_THROW, SoundSource.PLAYERS, 1F, 0.9F + random.nextFloat() * 0.2F);

        ((ServerLevel) level).sendParticles(ParticleUtils.constructSimpleSpark(new Color(100 + random.nextInt(156), random.nextInt(100 + random.nextInt(156)), random.nextInt(100 + random.nextInt(156))), 0.3F, 60, 0.95F),
                entity.getX(), entity.getY() + 0.4, entity.getZ(), 30, 0.1, 0.1, 0.1, 0.1);

        ci.cancel();
    }
}
