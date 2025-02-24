package it.hurts.octostudios.reliquified_ars_nouveau.spell;

import com.hollingsworth.arsnouveau.api.spell.AbstractAugment;
import com.hollingsworth.arsnouveau.api.spell.AbstractEffect;
import com.hollingsworth.arsnouveau.common.lib.GlyphLib;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectBubble;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class EffectNoManaCost  extends AbstractEffect {
    public static final EffectNoManaCost INSTANCE = new EffectNoManaCost();

    public EffectNoManaCost() {
        super(GlyphLib.prependGlyph("empty"), "Empty");
    }

    @Override
    protected int getDefaultManaCost() {
        return 0;
    }

    @Override
    protected @NotNull Set<AbstractAugment> getCompatibleAugments() {
        return Set.of();
    }
}
