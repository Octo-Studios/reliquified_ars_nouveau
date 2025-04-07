package it.hurts.octostudios.reliquified_ars_nouveau;

import it.hurts.octostudios.reliquified_ars_nouveau.init.EntityRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.bracelet.BallistarianBracerItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.UUID;

@Mod(ReliquifiedArsNouveau.MODID)
public class ReliquifiedArsNouveau {
    public static final String MODID = "reliquified_ars_nouveau";

    public ReliquifiedArsNouveau(IEventBus bus) {
        bus.addListener(this::setupCommon);

        ItemRegistry.register(bus);
        EntityRegistry.register(bus);
        RANDataComponentRegistry.register(bus);
    }

    private void setupCommon(final FMLCommonSetupEvent event) {

    }

    public static void aaa(HitResult hitResult) {
        //   System.out.println(hitResult.getLocation());
    }

    public static void sss(Projectile projectile) {
        if (!(projectile.getOwner() instanceof Player player) || player.getCommandSenderWorld().isClientSide())
            return;

        var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.BALLISTARIAN_BRACER.value());
        var level = (ServerLevel) player.getCommandSenderWorld();

        if (!(stack.getItem() instanceof BallistarianBracerItem relic) || relic.getEntities(stack).isEmpty())
            return;

        for (UUID uuidBow : relic.getEntities(stack)) {
            var bow = level.getEntity(uuidBow);

            if (bow == null || !bow.isAlive())
                continue;

            var arrow = new Arrow(EntityType.ARROW, level);

            arrow.setBaseDamage(Math.round(relic.getStatValue(stack, "striker", "damage")));

            arrow.setPos(bow.getPosition(0));

            level.addFreshEntity(arrow);
        }

//        relic.setUUID(stack, projectile.getUUID().toString());
    }
}