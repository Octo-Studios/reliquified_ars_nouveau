//package it.hurts.octostudios.reliquified_ars_nouveau.mixin;
//
//import it.hurts.octostudios.reliquified_ars_nouveau.entities.MagicShellEntity;
//import it.hurts.octostudios.reliquified_ars_nouveau.init.EntityRegistry;
//import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
//import it.hurts.octostudios.reliquified_ars_nouveau.items.bracelet.BallistarianBracerItem;
//import it.hurts.sskirillss.relics.network.NetworkHandler;
//import it.hurts.sskirillss.relics.network.packets.sync.S2CEntityTargetPacket;
//import it.hurts.sskirillss.relics.utils.EntityUtils;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.nbt.ListTag;
//import net.minecraft.nbt.Tag;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.entity.projectile.Projectile;
//import net.minecraft.world.phys.EntityHitResult;
//import net.minecraft.world.phys.HitResult;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//import java.util.ArrayList;
//import java.util.UUID;
//
//@Mixin(Projectile.class)
//public class ProjectileMixin {
//    @Inject(method = "onHit", at = @At(value = "HEAD"))
//    private void onHit(HitResult hitResult, CallbackInfo ci) {
//        var projectile = (Projectile) (Object) this;
//
//        var level = projectile.getCommandSenderWorld();
//
//        if (!(projectile.getOwner() instanceof Player player) || level.isClientSide()
//                || !projectile.getPersistentData().contains("ShellUUIDs"))
//            return;
//
//        var shellUuids = new ArrayList<UUID>();
//        var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.BALLISTARIAN_BRACER.value());
//
//        for (Tag tag : projectile.getPersistentData().getList("ShellUUIDs", Tag.TAG_COMPOUND))
//            if (tag instanceof CompoundTag compoundTag)
//                shellUuids.add(new UUID(compoundTag.getLong("MostSigBits"), compoundTag.getLong("LeastSigBits")));
//
//        for (UUID uuid : shellUuids) {
//            var shell = ((ServerLevel) level).getEntity(uuid);
//            var position = hitResult.getLocation();
//
//            if (shell != null && shell.isAlive()) {
//                var shellPersistentData = shell.getPersistentData();
//                shellPersistentData.remove("TargetUUID");
//
//                if (hitResult instanceof EntityHitResult entityHitResult) {
//                    if (stack.getItem() instanceof BallistarianBracerItem relic)
//                        relic.spreadRelicExperience(player, stack, 1);
//
//                    shellPersistentData.putUUID("HitEntity", entityHitResult.getEntity().getUUID());
//                } else {
//                    shellPersistentData.putDouble("HitPosX", position.x);
//                    shellPersistentData.putDouble("HitPosY", position.y);
//                    shellPersistentData.putDouble("HitPosZ", position.z);
//                }
//            }
//        }
//    }
//
//    @Inject(method = "shoot", at = @At(value = "HEAD"))
//    private void onEntityInside(CallbackInfo ci) {
//        var projectile = (Projectile) (Object) this;
//
//        if (!(projectile.getOwner() instanceof Player player) || player.getCommandSenderWorld().isClientSide())
//            return;
//
//        var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.BALLISTARIAN_BRACER.value());
//        var level = (ServerLevel) player.getCommandSenderWorld();
//
//        if (!(stack.getItem() instanceof BallistarianBracerItem relic) || relic.getEntities(stack).isEmpty())
//            return;
//
//        var uuidListTag = new ListTag();
//        var random = projectile.getRandom();
//
//        for (UUID uuidBow : relic.getEntities(stack)) {
//            var bow = level.getEntity(uuidBow);
//
//            if (bow == null || !bow.isAlive() || random.nextFloat() < relic.getStatValue(stack, "striker", "chance"))
//                continue;
//
//            var shell = new MagicShellEntity(EntityRegistry.MAGIC_SHELL.value(), level);
//
//            shell.getPersistentData().putUUID("TargetUUID", projectile.getUUID());
//            shell.getPersistentData().putInt("BowIndex", relic.getEntities(stack).indexOf(uuidBow));
//            shell.setOwner(player);
//            shell.setPos(bow.getEyePosition(0).add(0, -0.5, 0));
//            shell.setDeltaMovement(bow.getLookAngle().normalize().scale(0.3));
//            shell.setDamage(Math.round(relic.getStatValue(stack, "striker", "damage")));
//
//            level.addFreshEntity(shell);
//            var uuidTag = new CompoundTag();
//
//            uuidTag.putLong("MostSigBits", shell.getUUID().getMostSignificantBits());
//            uuidTag.putLong("LeastSigBits", shell.getUUID().getLeastSignificantBits());
//            uuidListTag.add(uuidTag);
//
//            NetworkHandler.sendToClientsTrackingEntity(new S2CEntityTargetPacket(shell.getId(), projectile.getId()), shell);
//        }
//
//        projectile.getPersistentData().put("ShellUUIDs", uuidListTag);
//    }
//
//}
