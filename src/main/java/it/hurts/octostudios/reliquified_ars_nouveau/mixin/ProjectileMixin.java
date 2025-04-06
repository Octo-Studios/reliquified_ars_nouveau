package it.hurts.octostudios.reliquified_ars_nouveau.mixin;

import com.hollingsworth.arsnouveau.common.entity.EntityOrbitProjectile;
import it.hurts.octostudios.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.charm.EmblemOfDevotionItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
public class ProjectileMixin {
    @Inject(method = "onHit", at = @At(value = "HEAD"))
    private void onHit(HitResult result, CallbackInfo ci) {
        ReliquifiedArsNouveau.aaa(result);
    }

    @Inject(method = "shoot", at = @At(value = "HEAD"))
    private void onEntityInside(CallbackInfo ci) {
        var projectile = (Projectile) (Object) this;
        ReliquifiedArsNouveau.sss(projectile);
    }

}
