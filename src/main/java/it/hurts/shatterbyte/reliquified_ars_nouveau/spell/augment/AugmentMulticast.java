package it.hurts.shatterbyte.reliquified_ars_nouveau.spell.augment;

import com.hollingsworth.arsnouveau.api.spell.AbstractAugment;
import com.hollingsworth.arsnouveau.api.spell.SpellTier;
import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import net.minecraft.resources.ResourceLocation;

public class AugmentMulticast extends AbstractAugment {
    public static final AugmentMulticast INSTANCE = new AugmentMulticast();

    private AugmentMulticast() {
        super(ResourceLocation.fromNamespaceAndPath(ReliquifiedArsNouveau.MODID, "glyph_multicast"), "Multicast");
    }

    @Override
    public int getDefaultManaCost() {
        return 0;
    }

    @Override
    public SpellTier defaultTier() {
        return SpellTier.ONE;
    }

    @Override
    public String getBookDescription() {
        return "Marks a spell so Archmage Glove can trigger delayed multicasts.";
    }
}
