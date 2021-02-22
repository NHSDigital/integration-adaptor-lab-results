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
    public static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;
    private static final int TYPE_OF_SPECIMEN_DETAILS_INDEX = 3;

    private final String typeOfSpecimen;

    public static SpecimenCharacteristicType fromString(String edifactString) {
        if (!edifactString.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException(
                "Can't create " + SpecimenCharacteristicType.class.getSimpleName() + " from " + edifactString);
        }
        final String[] split = Split.byColon(edifactString);
        return new SpecimenCharacteristicType(split[TYPE_OF_SPECIMEN_DETAILS_INDEX]);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() throws EdifactValidationException {
        if (StringUtils.isBlank(typeOfSpecimen)) {
            throw new EdifactValidationException(getKey() + ": Attribute typeOfSpecimen is blank or missing");
        }
    }
}

