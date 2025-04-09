package it.hurts.octostudios.reliquified_ars_nouveau;

import it.hurts.octostudios.reliquified_ars_nouveau.entities.MagicShellEntity;
import it.hurts.octostudios.reliquified_ars_nouveau.init.EntityRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.bracelet.BallistarianBracerItem;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.sync.S2CEntityTargetPacket;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.ArrayList;
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
}