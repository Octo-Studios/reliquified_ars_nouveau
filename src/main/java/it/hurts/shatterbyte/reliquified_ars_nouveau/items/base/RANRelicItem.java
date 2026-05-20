package it.hurts.shatterbyte.reliquified_ars_nouveau.items.base;

import it.hurts.shatterbyte.reliquified_ars_nouveau.ReliquifiedArsNouveau;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.Nullable;

public abstract class RANRelicItem extends RelicItem {
    public RANRelicItem(Item.Properties properties) {
        super(properties);
    }

    public RANRelicItem() {
        super(new Item.Properties()
                .rarity(Rarity.EPIC)
                .stacksTo(1));
    }

    @Override
    public String getConfigRoute() {
        return ReliquifiedArsNouveau.MODID;
    }

    @Override
    public @Nullable String getURI(LivingEntity entity, ItemStack stack) {
        return "https://shatterbyte.com/docs/mods/reliquified-ars-nouveau/relics/" + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + "/";
    }
}