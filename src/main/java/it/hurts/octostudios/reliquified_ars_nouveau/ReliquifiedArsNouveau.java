package it.hurts.octostudios.reliquified_ars_nouveau;

import it.hurts.octostudios.reliquified_ars_nouveau.entities.BallistarianBowEntity;
import it.hurts.octostudios.reliquified_ars_nouveau.entities.MagicShellEntity;
import it.hurts.octostudios.reliquified_ars_nouveau.init.EntityRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.bracelet.BallistarianBracerItem;
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

    public static void aaa(Projectile projectile, HitResult hitResult) {
        var level = projectile.getCommandSenderWorld();

        if (!(projectile.getOwner() instanceof Player player) || level.isClientSide()
                || !projectile.getPersistentData().contains("ShellUUIDs"))
            return;

        var shellUuids = new ArrayList<UUID>();

        for (Tag tag : projectile.getPersistentData().getList("ShellUUIDs", Tag.TAG_COMPOUND))
            if (tag instanceof CompoundTag compoundTag)
                shellUuids.add(new UUID(compoundTag.getLong("MostSigBits"), compoundTag.getLong("LeastSigBits")));

        for (UUID uuid : shellUuids) {
            var shell = ((ServerLevel) level).getEntity(uuid);
            var position = hitResult.getLocation();

            if (shell != null && shell.isAlive()) {
                var shellPersistentData = shell.getPersistentData();

                if (hitResult instanceof EntityHitResult entityHitResult)
                    shellPersistentData.putUUID("HitEntity", entityHitResult.getEntity().getUUID());
                else {

                    shellPersistentData.putDouble("HitPosX", position.x);
                    shellPersistentData.putDouble("HitPosY", position.y);
                    shellPersistentData.putDouble("HitPosZ", position.z);
                }
            }
        }
    }

    public static void sss(Projectile projectile) {
        if (!(projectile.getOwner() instanceof Player player) || player.getCommandSenderWorld().isClientSide())
            return;

        var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.BALLISTARIAN_BRACER.value());
        var level = (ServerLevel) player.getCommandSenderWorld();

        if (!(stack.getItem() instanceof BallistarianBracerItem relic) || relic.getEntities(stack).isEmpty())
            return;

        var uuidListTag = new ListTag();

        for (UUID uuidBow : relic.getEntities(stack)) {
            var bow = level.getEntity(uuidBow);

            if (bow == null || !bow.isAlive())
                continue;

            var shell = new MagicShellEntity(EntityRegistry.MAGIC_SHELL.value(), level);

            shell.getPersistentData().putUUID("TargetUUID", projectile.getUUID());
            shell.getPersistentData().putInt("BowIndex", relic.getEntities(stack).indexOf(uuidBow));
            shell.setOwner(player);
            shell.setPos(bow.getEyePosition(0).add(0, -0.5, 0));
            shell.setDeltaMovement(bow.getLookAngle().normalize().scale(0.8));
            shell.setDamage(Math.round(relic.getStatValue(stack, "striker", "damage")));

            level.addFreshEntity(shell);
            var uuidTag = new CompoundTag();

            uuidTag.putLong("MostSigBits", shell.getUUID().getMostSignificantBits());
            uuidTag.putLong("LeastSigBits", shell.getUUID().getLeastSignificantBits());
            uuidListTag.add(uuidTag);
        }

        projectile.getPersistentData().put("ShellUUIDs", uuidListTag);
    }
}