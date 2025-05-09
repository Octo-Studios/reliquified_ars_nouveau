package it.hurts.octostudios.reliquified_ars_nouveau.items.back;

import it.hurts.octostudios.reliquified_ars_nouveau.entities.WhirlingBroomEntity;
import it.hurts.octostudios.reliquified_ars_nouveau.init.EntityRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import it.hurts.octostudios.reliquified_ars_nouveau.items.NouveauRelicItem;
import it.hurts.octostudios.reliquified_ars_nouveau.items.base.loot.LootEntries;
import it.hurts.octostudios.reliquified_ars_nouveau.network.packets.ActivatedBoostBroomPacket;
import it.hurts.sskirillss.relics.init.DataComponentRegistry;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.CastData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastStage;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastType;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.PredicateType;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.*;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemColor;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemShape;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.BeamsData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleData;
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.sync.S2CEntityMotionPacket;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import top.theillusivec4.curios.api.SlotContext;

public class WhirlingBroomItem extends NouveauRelicItem {
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("broom")
                                .active(CastData.builder()
                                        .type(CastType.INSTANTANEOUS)
                                        .predicate("broom", PredicateType.CAST, (player, stack) -> !player.isInLiquid())
                                        .build())
                                .stat(StatData.builder("height")
                                        .initialValue(15D, 20D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.3D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(StatData.builder("manacost")
                                        .initialValue(70D, 60D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, -0.025)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(StatData.builder("boost")
                                        .initialValue(0.8D, 1.1D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.173)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatData.builder("health")
                                        .initialValue(10D, 12D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.215)
                                        .formatValue(value -> (int) MathUtils.round(value + 2, 0))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingData.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .sources(LevelingSourcesData.builder()
                                .source(LevelingSourceData.abilityBuilder("broom")
                                        .initialValue(1)
                                        .gem(GemShape.SQUARE, GemColor.GREEN)
                                        .build())
                                .build())
                        .build())
                .style(StyleData.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xff85543c)
                                .borderBottom(0xff85543c)
                                .textured(true)
                                .build())
                        .beams(BeamsData.builder()
                                .startColor(0xFF8fe121)
                                .endColor(0x0011532b)
                                .build())
                        .build())
                .loot(LootData.builder()
                        .entry(LootEntries.ARS_NOUVEAU, LootEntries.ARS_NOUVEAU_LIKE)
                        .build())
                .build();
    }

    @Override
    public void castActiveAbility(ItemStack stack, Player player, String ability, CastType type, CastStage stage) {
        if (player.getCommandSenderWorld().isClientSide())
            return;

        if (player.getVehicle() instanceof WhirlingBroomEntity)
            player.stopRiding();
        else {
            var level = (ServerLevel) player.getCommandSenderWorld();
            var broom = new WhirlingBroomEntity(EntityRegistry.WHIRLING_BROOM.get(), level);

            broom.setPos(player.getPosition(1));
            broom.setNoGravity(true);
            broom.setYRot(player.getYRot());
            broom.setXRot(player.getXRot());

            broom.yRotO = broom.yBodyRot = broom.yHeadRot = broom.getYRot();

            level.addFreshEntity(broom);

            EntityUtils.applyAttribute(broom, ItemStack.EMPTY, Attributes.MAX_HEALTH, (float) MathUtils.round(getStatValue(stack, "broom", "health"), 0), AttributeModifier.Operation.ADD_VALUE);

            broom.setHealth(broom.getMaxHealth());

            if (getHealth(stack) > 0) {
                broom.setHealth(getHealth(stack));
                setHealth(stack, 0);
            }

            player.startRiding(broom);
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!slotContext.entity().getCommandSenderWorld().isClientSide() || !((slotContext.entity()) instanceof LocalPlayer player)
                || !(player.getVehicle() instanceof WhirlingBroomEntity broom))
            return;

        NetworkHandler.sendToServer(new ActivatedBoostBroomPacket(Minecraft.getInstance().options.keySprint.isDown()));

        var speed = 1.7D;

        if (getToggled(stack))
            speed += speed * MathUtils.round(getStatValue(stack, "broom", "boost"), 0);

        var turnRate = 0.1;

        var movement = Vec3.ZERO;
        var lookDir = Vec3.directionFromRotation(player.getXRot(), player.getYRot());

        if (player.zza != 0)
            movement = movement.add(lookDir.scale(player.zza * speed * (player.zza < 0 ? 0.5 : 1)));

        if (player.xxa != 0)
            movement = movement.add(new Vec3(lookDir.z, 0, -lookDir.x).normalize().scale(player.xxa * speed * 0.5));

        var start = broom.position();
        var hitResult = broom.getCommandSenderWorld().clip(new ClipContext(start, start.add(0, -MathUtils.round(getStatValue(stack, "broom", "height"), 0), 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, broom));

        if (hitResult.getType() == HitResult.Type.MISS)
            movement = new Vec3(movement.x(), -0.8, movement.z());

        if (!movement.equals(Vec3.ZERO))
            broom.setDeltaMovement(broom.getDeltaMovement().lerp(movement, turnRate));
    }

    public void setToggled(ItemStack stack, boolean val) {
        stack.set(DataComponentRegistry.TOGGLED, val);
    }

    public boolean getToggled(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.TOGGLED, false);
    }

    public float getHealth(ItemStack stack) {
        return stack.getOrDefault(RANDataComponentRegistry.HEALTH, 0F);
    }

    public void setHealth(ItemStack stack, float val) {
        stack.set(RANDataComponentRegistry.HEALTH, Math.max(val, 0));
    }

    @EventBusSubscriber(Dist.CLIENT)
    public static class WhirlingBroomClientEvent {
        private static float currentFovModifier = 0.0F;

        @SubscribeEvent
        public static void onComputeFov(ComputeFovModifierEvent event) {
            var player = event.getPlayer();
            var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.WHIRLING_BROOM.value());

            if (!(stack.getItem() instanceof WhirlingBroomItem relic) || !(player.getVehicle() instanceof WhirlingBroomEntity broom))
                return;

            if (!relic.getToggled(stack) || broom.getKnownMovement().length() < 0.0785)
                currentFovModifier = Mth.lerp(0.1F, currentFovModifier, 0);
            else
                currentFovModifier = Mth.lerp(0.1F, currentFovModifier, (float) (relic.getStatValue(stack, "broom", "boost") / 10));

            event.setNewFovModifier(event.getFovModifier() + currentFovModifier);
        }
    }

    @EventBusSubscriber
    public static class WhirlingBroomEvent {
        @SubscribeEvent
        public static void onPlayerRideEntity(EntityMountEvent event) {
            if (!(event.getEntity() instanceof Player player) || player.getCommandSenderWorld().isClientSide()
                    || !(event.getEntityBeingMounted() instanceof WhirlingBroomEntity broom))
                return;

            if (event.isDismounting()) {
                var stack = EntityUtils.findEquippedCurio(player, ItemRegistry.WHIRLING_BROOM.value());

                if (!(stack.getItem() instanceof WhirlingBroomItem relic))
                    return;

                relic.setHealth(stack, broom.getHealth());
            } else {
                var broomMotion = player.getDeltaMovement().add(player.getDeltaMovement());

                NetworkHandler.sendToClient(new S2CEntityMotionPacket(broom.getId(), broomMotion.x, broomMotion.y / 4, broomMotion.z), (ServerPlayer) player);
            }
        }
    }
}
