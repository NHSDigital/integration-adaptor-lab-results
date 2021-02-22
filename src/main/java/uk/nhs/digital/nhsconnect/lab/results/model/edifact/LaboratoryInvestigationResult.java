package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

import java.math.BigDecimal;

/**
 * Example: RSL+NV+11.9:7++:::ng/mL+HI'
 */
@Getter
@Builder
@AllArgsConstructor
public class LaboratoryInvestigationResult extends Segment {

    private static final String KEY = "RSL";
    private static final String QUALIFIER = "NV";
    private static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;

    private static final int MEASUREMENT_UNIT_SECTION = 4;
    private static final int MEASUREMENT_UNIT_INDEX = 3;
    private static final int DEVIATING_RESULT_INDICATOR_INDEX = 5;

    private final BigDecimal measurementValue;
    private final MeasurementValueComparator measurementValueComparator;
    private final String measurementUnit;
    private final DeviatingResultIndicator deviatingResultIndicator;

    public static LaboratoryInvestigationResult fromString(String edifactString) {
        if (!edifactString.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException("Can't create "
                    + LaboratoryInvestigationResult.class.getSimpleName() + " from " + edifactString);
        }

        final String[] keySplit = Split.byPlus(edifactString);

        final BigDecimal measurementValue = extractMeasurementValue(keySplit);
        final String measurementValueComparator = extractMeasurementValueComparator(keySplit);
        final String measurementUnit = extractMeasurementUnit(keySplit);
        final String deviatingResultIndicator = extractDeviatingResultIndicator(keySplit);

        return LaboratoryInvestigationResult.builder()
            .measurementValue(measurementValue)
            .measurementValueComparator(StringUtils.isNotBlank(measurementValueComparator)
                    ? MeasurementValueComparator.fromCode(measurementValueComparator)
                    : null)
            .measurementUnit(measurementUnit)
            .deviatingResultIndicator(StringUtils.isNotBlank(deviatingResultIndicator)
                    ? DeviatingResultIndicator.fromCode(deviatingResultIndicator)
                    : null)
            .build();
    }

    private static BigDecimal extractMeasurementValue(String[] keySplit) {
        if (StringUtils.isNotBlank(keySplit[2])) {
            return new BigDecimal(Split.byColon(keySplit[2])[0]);
        }

        return null;
    }

    private static String extractMeasurementValueComparator(String[] keySplit) {
        if (StringUtils.isNotBlank(keySplit[2]) && Split.byColon(keySplit[2]).length >= 2) {
            return Split.byColon(keySplit[2])[1];
        }

        return null;
    }

    private static String extractMeasurementUnit(String[] keySplit) {
        if (StringUtils.isNotBlank(keySplit[MEASUREMENT_UNIT_SECTION])) {
            return Split.byColon(keySplit[MEASUREMENT_UNIT_SECTION])[MEASUREMENT_UNIT_INDEX];
        }

        return null;
    }

    private static String extractDeviatingResultIndicator(String[] keySplit) {
        if (DEVIATING_RESULT_INDICATOR_INDEX < keySplit.length) {
            return keySplit[DEVIATING_RESULT_INDICATOR_INDEX];
        }

        return null;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() throws EdifactValidationException {
        if (measurementValue == null) {
            throw new EdifactValidationException(getKey() + ": Attribute measurementValue is required");
        }

        if (StringUtils.isBlank(measurementUnit)) {
            throw new EdifactValidationException(getKey() + ": Attribute measurementUnit is required");
        }
    }
}
