package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * Example QTY+SVO:1750+:::mL'
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = false)
public class SpecimenQuantity extends Segment {
    private static final String KEY = "QTY";
    private static final String QUALIFIER = "SVO";
    public static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;
    private static final int QUANTITY_INDEX = 3;
    private static final int MEASUREMENT_UNIT_INDEX = 4;

    private final int quantity;
    private final String quantityUnitOfMeasure;

    public static SpecimenQuantity fromString(String edifactString) {
        if (!edifactString.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException(
                    "Can't create " + SpecimenQuantity.class.getSimpleName() + " from " + edifactString);
        }
        final String[] splitByColon = Split.byColon(edifactString);
        final int quantity = Integer.parseInt(Split.byPlus(splitByColon[1])[0]);
        return new SpecimenQuantity(quantity, splitByColon[MEASUREMENT_UNIT_INDEX]);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() throws EdifactValidationException {
        if (quantityUnitOfMeasure.isBlank()) {
            throw new EdifactValidationException(KEY + ": Unit of measure is required");
        }
    }
}
