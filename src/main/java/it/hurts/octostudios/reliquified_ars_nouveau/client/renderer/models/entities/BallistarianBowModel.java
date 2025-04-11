package it.hurts.octostudios.reliquified_ars_nouveau.client.renderer.models.entities;

import com.hollingsworth.arsnouveau.ArsNouveau;
import it.hurts.octostudios.reliquified_ars_nouveau.entities.BallistarianBowEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BallistarianBowModel extends GeoModel<BallistarianBowEntity> {
    @Override
    public ResourceLocation getModelResource(BallistarianBowEntity ballistarianBowEntity) {
        return ArsNouveau.prefix("geo/spellbow.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BallistarianBowEntity ballistarianBowEntity) {
        return ArsNouveau.prefix("textures/item/spellbow.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BallistarianBowEntity ballistarianBowEntity) {
        return null;
    }
}
