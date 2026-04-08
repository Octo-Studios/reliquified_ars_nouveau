package it.hurts.shatterbyte.reliquified_ars_nouveau.mixin.ars;

import com.hollingsworth.arsnouveau.api.spell.ISpellValidator;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import com.hollingsworth.arsnouveau.client.gui.book.GuiSpellBook;
import com.hollingsworth.arsnouveau.common.spell.validation.CombinedSpellValidator;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.spell.augment.AugmentMulticast;
import it.hurts.shatterbyte.reliquified_ars_nouveau.spell.validation.MulticastPlacementSpellValidator;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.List;

@Mixin(GuiSpellBook.class)
public abstract class GuiSpellBookMixin {
    @Shadow
    public ISpellValidator spellValidator;

    @Shadow
    public ISpellValidator pasteValidator;

    @Shadow
    public List<AbstractSpellPart> unlockedSpells;

    @Shadow
    public List<AbstractSpellPart> displayedGlyphs;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void ran$injectMulticastRules(InteractionHand hand, CallbackInfo ci) {
        var validator = new MulticastPlacementSpellValidator();
        var player = Minecraft.getInstance().player;
        var hasArchmageGloveEquipped = player != null && CuriosApi.getCuriosInventory(player)
                .map(handler -> {
                    var equippedCurios = handler.getEquippedCurios();

                    for (var slot = 0; slot < equippedCurios.getSlots(); slot++) {
                        if (equippedCurios.getStackInSlot(slot).getItem() == ItemRegistry.ARCHMAGE_GLOVE.value())
                            return true;
                    }

                    return false;
                })
                .orElse(false);

        spellValidator = new CombinedSpellValidator(spellValidator, validator);
        pasteValidator = new CombinedSpellValidator(pasteValidator, validator);

        if (hasArchmageGloveEquipped) {
            var hasMulticast = unlockedSpells.stream().anyMatch(part -> part.getRegistryName() != null && part.getRegistryName().equals(AugmentMulticast.INSTANCE.getRegistryName()));

            if (!hasMulticast) {
                unlockedSpells = new ArrayList<>(unlockedSpells);
                unlockedSpells.add(AugmentMulticast.INSTANCE);
            }

            displayedGlyphs = new ArrayList<>(unlockedSpells);
            return;
        }

        var multicastGlyphId = AugmentMulticast.INSTANCE.getRegistryName();

        unlockedSpells = unlockedSpells.stream()
                .filter(part -> part.getRegistryName() == null || !part.getRegistryName().equals(multicastGlyphId))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        displayedGlyphs = new ArrayList<>(unlockedSpells);
    }
}
