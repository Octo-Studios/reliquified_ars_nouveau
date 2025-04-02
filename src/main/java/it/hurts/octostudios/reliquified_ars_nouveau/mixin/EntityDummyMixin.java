package it.hurts.octostudios.reliquified_ars_nouveau.mixin;

import com.hollingsworth.arsnouveau.api.entity.ISummon;
import com.hollingsworth.arsnouveau.common.entity.EntityDummy;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.back.IllusionistsMantleItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityDummy.class)
public abstract class EntityDummyMixin extends PathfinderMob implements ISummon {
    protected EntityDummyMixin(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void onEntityInside(CallbackInfo ci) {
        var dummy = (EntityDummy) (Object) this;

        if (dummy.getCommandSenderWorld().isClientSide() || dummy.getOwnerUUID() == null || !dummy.getPersistentData().getBoolean("SpawnedFromRelic"))
            return;

        var level = (ServerLevel) dummy.getCommandSenderWorld();
        var entity = (level.getPlayerByUUID(dummy.getOwnerUUID()));
        var stack = EntityUtils.findEquippedCurio(entity, ItemRegistry.ILLUSIONISTS_MANTLE.value());

        if (!stack.isEmpty())
            return;

        dummy.discard();
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);

        var dummy = (EntityDummy) (Object) this;

        if (dummy.getCommandSenderWorld().isClientSide() || dummy.getOwnerUUID() == null || !dummy.getPersistentData().getBoolean("SpawnedFromRelic"))
            return;

        var level = (ServerLevel) dummy.getCommandSenderWorld();
        var entity = (level.getPlayerByUUID(dummy.getOwnerUUID()));
        var stack = EntityUtils.findEquippedCurio(entity, ItemRegistry.ILLUSIONISTS_MANTLE.value());

        if (!(stack.getItem() instanceof IllusionistsMantleItem relic))
            return;

        relic.removeEntities(stack, dummy.getUUID());
    }
}
