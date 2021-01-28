package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * Example SPC+TSP+:::BLOOD & URINE'
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = false)
public class SpecimenCharacteristicType extends Segment {
    private static final String KEY = "SPC";
    private static final String QUALIFIER = "TSP";
    private static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;
    private static final int THREE = 3;

    private final String typeOfSpecimen;

    public static SpecimenCharacteristicType fromString(String edifactString) {
        if (!edifactString.startsWith(SpecimenCharacteristicType.KEY_QUALIFIER)) {
            throw new IllegalArgumentException(
                "Can't create " + SpecimenCharacteristicType.class.getSimpleName() + " from " + edifactString);
        }
        String[] split = Split.byColon(
            Split.bySegmentTerminator(edifactString)[0]
        );
        return new SpecimenCharacteristicType(split[THREE]);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        return QUALIFIER + PLUS_SEPARATOR + COLON_SEPARATOR.repeat(THREE) + typeOfSpecimen;
    }

    @Override
    protected void validateStateful() throws EdifactValidationException {
        // nothing
    }

    @Override
    public void preValidate() throws EdifactValidationException {
        if (StringUtils.isBlank(typeOfSpecimen)) {
            throw new EdifactValidationException(getKey() + ": Attribute typeOfSpecimen is blank or missing");
        }
    }
}

