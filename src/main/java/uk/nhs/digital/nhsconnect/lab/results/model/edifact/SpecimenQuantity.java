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
    private static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;

    private final int quantity;
    private final String quantityUnitOfMeasure;

    public static SpecimenQuantity fromString(String edifactString) {
        if (!edifactString.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException(
                "Can't create " + SpecimenQuantity.class.getSimpleName() + " from " + edifactString);
        }
        String[] splitByColon = Split.byColon(
            Split.bySegmentTerminator(edifactString)[0]
        );
        int quantity = Integer.parseInt(Split.byPlus(splitByColon[1])[0]);
        return new SpecimenQuantity(quantity, splitByColon[4]);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        return QUALIFIER + COLON_SEPARATOR + quantity + PLUS_SEPARATOR + COLON_SEPARATOR.repeat(3) + quantityUnitOfMeasure;
    }

    @Override
    protected void validateStateful() throws EdifactValidationException {
        // nothing
    }

    @Override
    public void preValidate() throws EdifactValidationException {
        if (quantityUnitOfMeasure.isBlank()) {
            throw new EdifactValidationException(getKey() + ": Unit of measure is required");
        }
    }
}
