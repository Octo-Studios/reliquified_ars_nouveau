package it.hurts.shatterbyte.reliquified_ars_nouveau.client.renderer.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.RANDataComponentRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.items.ArchitectsStaffItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.LinkedHashSet;

@EventBusSubscriber(modid = ReliquifiedArsNouveau.MODID, value = Dist.CLIENT)
public class ArchitectsStaffBridgePreviewRenderer {
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES)
            return;

        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        var player = minecraft.player;

        if (level == null || player == null || !player.isUsingItem())
            return;

        var stack = player.getUseItem();

        if (!(stack.getItem() instanceof ArchitectsStaffItem item))
            return;

        var firstPointData = stack.get(RANDataComponentRegistry.ARCHITECTS_STAFF_FIRST_POINT.get());

        if (firstPointData == null || !firstPointData.getLevel().equals(level.dimension()))
            return;

        var secondPoint = player.blockPosition();
        var hit = player.pick(64D, 0F, false);

        if (hit instanceof BlockHitResult blockHitResult)
            secondPoint = blockHitResult.getBlockPos();

        var fromCenter = firstPointData.getPos();
        var toCenter = Vec3.atCenterOf(secondPoint);
        var path = new LinkedHashSet<BlockPos>();
        var currentX = Mth.floor(fromCenter.x);
        var currentY = Mth.floor(fromCenter.y);
        var currentZ = Mth.floor(fromCenter.z);
        var targetX = Mth.floor(toCenter.x);
        var targetY = Mth.floor(toCenter.y);
        var targetZ = Mth.floor(toCenter.z);
        var deltaX = toCenter.x - fromCenter.x;
        var deltaY = toCenter.y - fromCenter.y;
        var deltaZ = toCenter.z - fromCenter.z;
        var stepX = deltaX > 0D ? 1 : deltaX < 0D ? -1 : 0;
        var stepY = deltaY > 0D ? 1 : deltaY < 0D ? -1 : 0;
        var stepZ = deltaZ > 0D ? 1 : deltaZ < 0D ? -1 : 0;
        var tDeltaX = stepX == 0 ? Double.POSITIVE_INFINITY : 1D / Math.abs(deltaX);
        var tDeltaY = stepY == 0 ? Double.POSITIVE_INFINITY : 1D / Math.abs(deltaY);
        var tDeltaZ = stepZ == 0 ? Double.POSITIVE_INFINITY : 1D / Math.abs(deltaZ);
        var tMaxX = stepX > 0 ? ((currentX + 1D) - fromCenter.x) / deltaX : stepX < 0 ? (fromCenter.x - currentX) / -deltaX : Double.POSITIVE_INFINITY;
        var tMaxY = stepY > 0 ? ((currentY + 1D) - fromCenter.y) / deltaY : stepY < 0 ? (fromCenter.y - currentY) / -deltaY : Double.POSITIVE_INFINITY;
        var tMaxZ = stepZ > 0 ? ((currentZ + 1D) - fromCenter.z) / deltaZ : stepZ < 0 ? (fromCenter.z - currentZ) / -deltaZ : Double.POSITIVE_INFINITY;
        var safety = 0;

        path.add(new BlockPos(currentX, currentY, currentZ));

        while ((currentX != targetX || currentY != targetY || currentZ != targetZ) && safety++ < 4096) {
            if (tMaxX <= tMaxY && tMaxX <= tMaxZ) {
                currentX += stepX;
                tMaxX += tDeltaX;
            } else if (tMaxY <= tMaxX && tMaxY <= tMaxZ) {
                currentY += stepY;
                tMaxY += tDeltaY;
            } else {
                currentZ += stepZ;
                tMaxZ += tDeltaZ;
            }

            path.add(new BlockPos(currentX, currentY, currentZ));
        }

        if (path.isEmpty())
            return;

        var maxCharges = Math.max(1, (int) Math.round(Math.max(0D, item.getRelicData(null, stack).getAbilitiesData().getAbilityData("bridgecraft").getStatData("max_charges").getValue())));
        var charges = Mth.clamp(stack.getOrDefault(RANDataComponentRegistry.ARCHITECTS_STAFF_CHARGES.get(), maxCharges), 0, maxCharges);
        var firstPoint = BlockPos.containing(firstPointData.getPos());
        var pathList = new ArrayList<BlockPos>();

        for (var blockPos : path) {
            if (blockPos.equals(firstPoint) || blockPos.equals(secondPoint))
                continue;

            pathList.add(blockPos);
        }

        var unitsToBuild = Math.min(pathList.size(), charges);

        if (unitsToBuild <= 0)
            return;

        var partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        var gameTime = level.getGameTime() + partialTick;
        var pulse = 0.45F + 0.55F * (float) ((Math.sin(gameTime * 0.2F) + 1D) * 0.5D);
        var cameraPosition = event.getCamera().getPosition();
        var poseStack = event.getPoseStack();
        var builder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        poseStack.pushPose();
        poseStack.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);

        var matrix = poseStack.last().pose();

        for (var i = 0; i < unitsToBuild; i++) {
            var blockPos = pathList.get(i);

            if (!level.isInWorldBounds(blockPos))
                continue;

            var state = level.getBlockState(blockPos);
            var canPlace = state.canBeReplaced()
                    && level.getBlockEntity(blockPos) == null
                    && level.isUnobstructed(com.hollingsworth.arsnouveau.setup.registry.BlockRegistry.MAGE_BLOCK.get().defaultBlockState(), blockPos, CollisionContext.empty());
            var minX = (float) blockPos.getX() + 0.08F;
            var minY = (float) blockPos.getY() + 0.08F;
            var minZ = (float) blockPos.getZ() + 0.08F;
            var maxX = (float) blockPos.getX() + 0.92F;
            var maxY = (float) blockPos.getY() + 0.92F;
            var maxZ = (float) blockPos.getZ() + 0.92F;
            var validIntensity = 0.4F + pulse * 0.6F;
            var invalidIntensity = 0.25F + pulse * 0.35F;
            var red = canPlace ? 0.12F : 0.95F;
            var green = canPlace ? 0.75F + 0.2F * validIntensity : 0.25F + 0.2F * invalidIntensity;
            var blue = canPlace ? 1F : 0.18F;
            var alpha = canPlace ? 0.16F + 0.12F * validIntensity : 0.2F + 0.1F * invalidIntensity;

            builder.addVertex(matrix, minX, minY, minZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, minX, maxY, minZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, minX, minY, minZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, maxX, minY, minZ).setColor(red, green, blue, alpha);

            builder.addVertex(matrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);

            builder.addVertex(matrix, minX, minY, minZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, minX, minY, minZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, minX, maxY, minZ).setColor(red, green, blue, alpha);

            builder.addVertex(matrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);

            builder.addVertex(matrix, minX, maxY, minZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, minX, maxY, minZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);

            builder.addVertex(matrix, minX, minY, minZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, minX, minY, minZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
            builder.addVertex(matrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
        }

        var mesh = builder.build();

        if (mesh != null)
            BufferUploader.drawWithShader(mesh);

        poseStack.popPose();

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}
