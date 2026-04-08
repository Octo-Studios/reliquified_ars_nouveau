package it.hurts.shatterbyte.reliquified_ars_nouveau.spell.validation;

import com.hollingsworth.arsnouveau.api.spell.AbstractCastMethod;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import com.hollingsworth.arsnouveau.api.spell.SpellValidationError;
import com.hollingsworth.arsnouveau.common.spell.validation.AbstractSpellValidator;
import com.hollingsworth.arsnouveau.common.spell.validation.BaseSpellValidationError;
import it.hurts.shatterbyte.reliquified_ars_nouveau.spell.augment.AugmentMulticast;

import java.util.ArrayList;
import java.util.List;

public class MulticastPlacementSpellValidator extends AbstractSpellValidator {
    @Override
    protected void validateImpl(List<AbstractSpellPart> spellRecipe, List<SpellValidationError> validationErrors) {
        var multicastPositions = new ArrayList<Integer>();

        for (var index = 0; index < spellRecipe.size(); index++) {
            var part = spellRecipe.get(index);

            if (part == AugmentMulticast.INSTANCE)
                multicastPositions.add(index);
        }

        if (multicastPositions.isEmpty())
            return;

        if (multicastPositions.size() > 1) {
            for (var index = 1; index < multicastPositions.size(); index++) {
                var position = multicastPositions.get(index);
                var part = spellRecipe.get(position);

                if (part != null)
                    validationErrors.add(new MulticastLimitError(position, part));
            }
        }

        for (var position : multicastPositions) {
            var part = spellRecipe.get(position);

            if (part == null)
                continue;

            var isAfterForm = position == 1
                    && spellRecipe.size() > 0
                    && spellRecipe.get(0) instanceof AbstractCastMethod;

            if (!isAfterForm)
                validationErrors.add(new MulticastPlacementError(position, part));
        }
    }

    private static class MulticastLimitError extends BaseSpellValidationError {
        public MulticastLimitError(int position, AbstractSpellPart part) {
            super(position, part, "multicast_limit");
        }
    }

    private static class MulticastPlacementError extends BaseSpellValidationError {
        public MulticastPlacementError(int position, AbstractSpellPart part) {
            super(position, part, "multicast_position");
        }
    }
}
