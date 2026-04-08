package it.hurts.shatterbyte.reliquified_ars_nouveau.mixin.ars;

import com.hollingsworth.arsnouveau.client.gui.book.GlyphUnlockMenu;
import com.hollingsworth.arsnouveau.common.crafting.recipes.GlyphRecipe;
import it.hurts.shatterbyte.reliquified_ars_nouveau.init.ItemRegistry;
import it.hurts.shatterbyte.reliquified_ars_nouveau.spell.augment.AugmentMulticast;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.List;

@Mixin(GlyphUnlockMenu.class)
public abstract class GlyphUnlockMenuMixin {
    @Shadow
    public List<RecipeHolder<GlyphRecipe>> allParts;

    @Shadow
    public List<RecipeHolder<GlyphRecipe>> displayedGlyphs;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void ran$hideMulticastWithoutGlove(BlockPos pos, CallbackInfo ci) {
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

        if (hasArchmageGloveEquipped)
            return;

        var multicastGlyphId = AugmentMulticast.INSTANCE.getRegistryName();
        allParts = allParts.stream()
                .filter(recipe -> !recipe.value().getSpellPart().getRegistryName().equals(multicastGlyphId))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        displayedGlyphs = new ArrayList<>(allParts);
    }
}
