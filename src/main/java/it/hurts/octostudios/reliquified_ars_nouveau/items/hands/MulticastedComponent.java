package it.hurts.octostudios.reliquified_ars_nouveau.items.hands;

import com.hollingsworth.arsnouveau.api.spell.SpellCaster;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MulticastedComponent(int multicastCount, int tickSpell, SpellCaster spellCaster, String id) {
    public static final Codec<MulticastedComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("multicastCount").forGetter(MulticastedComponent::multicastCount),
            Codec.INT.fieldOf("tickSpell").forGetter(MulticastedComponent::tickSpell),
            SpellCaster.CODEC.codec().fieldOf("spellCaster").forGetter(MulticastedComponent::spellCaster),
            Codec.STRING.fieldOf("id").forGetter(MulticastedComponent::id)
    ).apply(instance, MulticastedComponent::new));

}
