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
    public static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;

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

        String[] keySplit = Split.byPlus(edifactString);

        BigDecimal measurementValue = extractMeasurementValue(keySplit);
        String measurementValueComparator = extractMeasurementValueComparator(keySplit);
        String measurementUnit = extractMeasurementUnit(keySplit);
        String deviatingResultIndicator = extractDeviatingResultIndicator(keySplit);

        return new LaboratoryInvestigationResult(
                measurementValue,
                MeasurementValueComparator.fromCode(measurementValueComparator),
                measurementUnit,
                DeviatingResultIndicator.fromCode(deviatingResultIndicator)
        );
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
    public String getValue() {
        return QUALIFIER
                + PLUS_SEPARATOR
                + measurementValue
                + COLON_SEPARATOR
                + measurementValueComparator.getCode()
                + PLUS_SEPARATOR + PLUS_SEPARATOR
                + COLON_SEPARATOR + COLON_SEPARATOR + COLON_SEPARATOR
                + measurementUnit
                + PLUS_SEPARATOR
                + deviatingResultIndicator.getCode();
    }

    @Override
    protected void validateStateful() throws EdifactValidationException {

    }

    @Override
    public void preValidate() throws EdifactValidationException {
        if (measurementValue == null) {
            throw new EdifactValidationException(getKey() + ": Attribute measurementValue is required");
        }

        if (StringUtils.isBlank(measurementUnit)) {
            throw new EdifactValidationException(getKey() + ": Attribute measurementUnit is required");
        }
    }
}
