package it.hurts.shatterbyte.reliquified_ars_nouveau.client.renderer.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = ReliquifiedArsNouveau.MODID, value = Dist.CLIENT)
public class CloakAbsorptionSphereRenderer {
    private static final Map<Integer, Long> ACTIVE_SPHERES = new HashMap<>();
    private static final Map<Integer, Boolean> PARTIAL_ABSORPTION = new HashMap<>();

    public static void add(int entityId, boolean partialAbsorption) {
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;

        if (level == null)
            return;

        ACTIVE_SPHERES.put(entityId, level.getGameTime() + 20L);
        PARTIAL_ABSORPTION.put(entityId, partialAbsorption);
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE_SPHERES.isEmpty())
            return;

        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;

        if (level == null)
            return;

        var gameTime = level.getGameTime();

        ACTIVE_SPHERES.entrySet().removeIf(entry ->
                entry.getValue() <= gameTime
                        || !(level.getEntity(entry.getKey()) instanceof LivingEntity living)
                        || !living.isAlive()
        );
        PARTIAL_ABSORPTION.keySet().removeIf(entityId -> !ACTIVE_SPHERES.containsKey(entityId));

        if (ACTIVE_SPHERES.isEmpty())
            return;

        var partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        var cameraPosition = event.getCamera().getPosition();
        var poseStack = event.getPoseStack();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        for (var entry : ACTIVE_SPHERES.entrySet()) {
            if (!(level.getEntity(entry.getKey()) instanceof LivingEntity living) || !living.isAlive())
                continue;

            var remainingTicks = (float) (entry.getValue() - (gameTime + partialTick));

            if (remainingTicks <= 0F)
                continue;

            var fade = Mth.clamp(remainingTicks / 20F, 0F, 1F);
            var isPartialAbsorption = PARTIAL_ABSORPTION.getOrDefault(entry.getKey(), false);
            var red = isPartialAbsorption ? 1F : 0.35F;
            var green = isPartialAbsorption ? 0.25F : 0.72F;
            var blue = isPartialAbsorption ? 0.25F : 1F;
            var alpha = 0.28F * fade;
            var interpolatedPosition = living.getPosition(partialTick);
            var sphereCenter = interpolatedPosition.add(0D, living.getBbHeight() * 0.5D, 0D);
            var horizontalRadius = Math.max(0.7D, living.getBbWidth() * 0.75D + 0.35D);
            var verticalRadius = Math.max(horizontalRadius, living.getBbHeight() * 0.5D + 0.2D);

            poseStack.pushPose();
            poseStack.translate(
                    sphereCenter.x - cameraPosition.x,
                    sphereCenter.y - cameraPosition.y,
                    sphereCenter.z - cameraPosition.z
            );

            var builder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
            var matrix = poseStack.last().pose();
            var rings = 16;
            var segments = 24;

            for (var ring = 0; ring < rings; ring++) {
                var latitude0 = -Math.PI / 2D + Math.PI * (double) ring / (double) rings;
                var latitude1 = -Math.PI / 2D + Math.PI * (double) (ring + 1) / (double) rings;
                var sinLatitude0 = Math.sin(latitude0);
                var sinLatitude1 = Math.sin(latitude1);
                var cosLatitude0 = Math.cos(latitude0);
                var cosLatitude1 = Math.cos(latitude1);

                for (var segment = 0; segment < segments; segment++) {
                    var longitude0 = Math.PI * 2D * (double) segment / (double) segments;
                    var longitude1 = Math.PI * 2D * (double) (segment + 1) / (double) segments;
                    var cosLongitude0 = Math.cos(longitude0);
                    var sinLongitude0 = Math.sin(longitude0);
                    var cosLongitude1 = Math.cos(longitude1);
                    var sinLongitude1 = Math.sin(longitude1);

                    var x00 = (float) (cosLongitude0 * cosLatitude0 * horizontalRadius);
                    var y00 = (float) (sinLatitude0 * verticalRadius);
                    var z00 = (float) (sinLongitude0 * cosLatitude0 * horizontalRadius);
                    var x01 = (float) (cosLongitude1 * cosLatitude0 * horizontalRadius);
                    var y01 = y00;
                    var z01 = (float) (sinLongitude1 * cosLatitude0 * horizontalRadius);
                    var x10 = (float) (cosLongitude0 * cosLatitude1 * horizontalRadius);
                    var y10 = (float) (sinLatitude1 * verticalRadius);
                    var z10 = (float) (sinLongitude0 * cosLatitude1 * horizontalRadius);
                    var x11 = (float) (cosLongitude1 * cosLatitude1 * horizontalRadius);
                    var y11 = y10;
                    var z11 = (float) (sinLongitude1 * cosLatitude1 * horizontalRadius);

                    builder.addVertex(matrix, x00, y00, z00).setColor(red, green, blue, alpha);
                    builder.addVertex(matrix, x10, y10, z10).setColor(red, green, blue, alpha);
                    builder.addVertex(matrix, x11, y11, z11).setColor(red, green, blue, alpha);

                    builder.addVertex(matrix, x00, y00, z00).setColor(red, green, blue, alpha);
                    builder.addVertex(matrix, x11, y11, z11).setColor(red, green, blue, alpha);
                    builder.addVertex(matrix, x01, y01, z01).setColor(red, green, blue, alpha);
                }
            }

            var mesh = builder.build();

            if (mesh != null)
                BufferUploader.drawWithShader(mesh);

            poseStack.popPose();
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}
