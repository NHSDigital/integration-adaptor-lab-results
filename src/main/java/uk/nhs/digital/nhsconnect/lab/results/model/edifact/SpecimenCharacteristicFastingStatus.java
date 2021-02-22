package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * Example SPC+FS+F'
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = false)
public class SpecimenCharacteristicFastingStatus extends Segment {
    private static final String KEY = "SPC";
    private static final String QUALIFIER = "FS";
    private static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;

    private final String fastingStatus;

    public static SpecimenCharacteristicFastingStatus fromString(String edifactString) {
        if (!edifactString.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException(
                "Can't create " + SpecimenCharacteristicFastingStatus.class.getSimpleName() + " from " + edifactString);
        }
        final String[] split = Split.byPlus(edifactString);
        return new SpecimenCharacteristicFastingStatus(split[2]);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() throws EdifactValidationException {
        if (StringUtils.isBlank(fastingStatus)) {
            throw new EdifactValidationException(getKey() + ": Attribute fastingStatus is blank or missing");
        }
    }
}

