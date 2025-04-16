package it.hurts.octostudios.reliquified_ars_nouveau.items.bracelet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BallistarianBracerComponent(String uuid, int cooldown) {
    public static final Codec<BallistarianBracerComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("uuid").forGetter(BallistarianBracerComponent::uuid),
            Codec.INT.fieldOf("cooldown").forGetter(BallistarianBracerComponent::cooldown)
    ).apply(instance, BallistarianBracerComponent::new));

}
