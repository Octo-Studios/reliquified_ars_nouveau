package it.hurts.shatterbyte.reliquified_ars_nouveau.client.handlers;

import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.shatterbyte.reliquified_ars_nouveau.client.models.items.*;
import it.hurts.shatterbyte.reliquified_ars_nouveau.client.renderer.items.ArchmageGloveRenderer;
import it.hurts.shatterbyte.reliquified_ars_nouveau.client.renderer.items.CloakOfConcealmentRenderer;
import it.hurts.shatterbyte.reliquified_ars_nouveau.client.renderer.items.FlamingBracerRenderer;
import it.hurts.shatterbyte.reliquified_ars_nouveau.client.renderer.items.QuantumBubbleRenderer;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.style.base.RelicStyle;
import it.hurts.sskirillss.relics.init.RelicsRelicRenderers;
import it.hurts.sskirillss.relics.init.RelicsRelicStyles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = ReliquifiedArsNouveau.MODID, value = Dist.CLIENT)
public class ClientHandler {
    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (var entry : ItemRegistry.ITEMS.getEntries()) {
                var item = entry.get();

                if (!(item instanceof IRelicItem))
                    continue;

                RelicsRelicStyles.register(item, RelicStyle::new);
            }

            RelicsRelicStyles.init();
        });

        RelicsRelicRenderers.register(ItemRegistry.CLOAK_OF_CONCEALMENT.get(), CloakOfConcealmentRenderer::new);
        RelicsRelicRenderers.register(ItemRegistry.FLAMING_BRACER.get(), FlamingBracerRenderer::new);
        RelicsRelicRenderers.register(ItemRegistry.QUANTUM_BUBBLE.get(), QuantumBubbleRenderer::new);
        RelicsRelicRenderers.register(ItemRegistry.ARCHMAGE_GLOVE.get(), ArchmageGloveRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayers(final EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(CloakOfConcealmentModel.LAYER, CloakOfConcealmentModel::constructLayerDefinition);
        event.registerLayerDefinition(FlamingBracerSlimModel.LAYER, FlamingBracerSlimModel::constructLayerDefinition);
        event.registerLayerDefinition(FlamingBracerWideModel.LAYER, FlamingBracerWideModel::constructLayerDefinition);
        event.registerLayerDefinition(ArchmageGloveSlimModel.LAYER, ArchmageGloveSlimModel::constructLayerDefinition);
        event.registerLayerDefinition(ArchmageGloveWideModel.LAYER, ArchmageGloveWideModel::constructLayerDefinition);
        event.registerLayerDefinition(QuantumBubbleModel.LAYER, QuantumBubbleModel::constructLayerDefinition);
    }

    @SubscribeEvent
    public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event) {

    }
}