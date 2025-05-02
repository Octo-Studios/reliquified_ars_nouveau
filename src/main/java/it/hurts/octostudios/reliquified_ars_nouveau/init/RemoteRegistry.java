package it.hurts.octostudios.reliquified_ars_nouveau.init;

import it.hurts.octostudios.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.octostudios.reliquified_ars_nouveau.client.renderer.entities.BallistarianBowRenderer;
import it.hurts.octostudios.reliquified_ars_nouveau.client.renderer.entities.WhirlingBroomRenderer;
import it.hurts.octostudios.reliquified_ars_nouveau.client.renderer.models.layers.CuttingLayer;
import it.hurts.sskirillss.relics.client.renderer.entities.NullRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = ReliquifiedArsNouveau.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RemoteRegistry {
    @SubscribeEvent
    public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.WHIRLING_BROOM.get(), WhirlingBroomRenderer::new);
        event.registerEntityRenderer(EntityRegistry.BALLISTARIAN_BOW.get(), BallistarianBowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.MAGIC_SHELL.get(), NullRenderer::new);
    }

    @SubscribeEvent
    public static void onPlayerRendererRegister(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skinType : event.getSkins())
            if (event.getSkin(skinType) instanceof PlayerRenderer playerRenderer)
                playerRenderer.addLayer(new CuttingLayer<>(playerRenderer));
    }
}