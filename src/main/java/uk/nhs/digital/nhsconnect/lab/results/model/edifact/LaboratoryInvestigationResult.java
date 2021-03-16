package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Details of the numerical result of a Laboratory Investigation.
 * <pre>
 * Example: {@code RSL+NV+11.9:7++:::ng/mL+HI'}
 * </pre>
 * Details of the coded result of a Laboratory Investigation.
 * <pre>
 * Example: {@code RSL+CV+::375211000000108:921::Bowel cancer screening programme FOB test normal (finding)'}
 * </pre>
 */
@AllArgsConstructor
@Builder
@Getter
public class LaboratoryInvestigationResult extends Segment {

    public static final String KEY = "RSL";

    private static final int MEASUREMENT_UNIT_SECTION = 4;
    private static final int MEASUREMENT_UNIT_INDEX = 3;
    private static final int DEVIATING_RESULT_INDICATOR_INDEX = 5;

    private final LaboratoryInvestigationResultType resultType;

    private final BigDecimal measurementValue;
    private final MeasurementValueComparator measurementValueComparator;
    private final String measurementUnit;
    private final DeviatingResultIndicator deviatingResultIndicator;

    private final String code;
    private final CodingType codingType;
    private final String description;

    @SuppressWarnings("checkstyle:MagicNumber")
    public static LaboratoryInvestigationResult fromString(String edifactString) {
        if (!edifactString.startsWith(KEY)) {
            throw new IllegalArgumentException("Can't create "
                + LaboratoryInvestigationResult.class.getSimpleName() + " from " + edifactString);
        }

        final String[] keySplit = Split.byPlus(edifactString);

        final LaboratoryInvestigationResultType resultType =
            LaboratoryInvestigationResultType.fromCode(keySplit[1]);

        final LaboratoryInvestigationResultBuilder laboratoryInvestigationResultBuilder =
            LaboratoryInvestigationResult.builder()
                .resultType(resultType);

        if (resultType.equals(LaboratoryInvestigationResultType.NUMERICAL_VALUE)) {
            final BigDecimal measurementValue = extractMeasurementValue(keySplit);
            final String measurementValueComparator = extractMeasurementValueComparator(keySplit);
            final String measurementUnit = extractMeasurementUnit(keySplit);
            final String deviatingResultIndicator = extractDeviatingResultIndicator(keySplit);

            return laboratoryInvestigationResultBuilder
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

        final String code = Split.byColon(keySplit[2])[2];
        final String codeType = Split.byColon(keySplit[2])[3];
        final String description = Split.byColon(keySplit[2])[5];

        return laboratoryInvestigationResultBuilder
            .code(code)
            .codingType(CodingType.fromCode(codeType))
            .description(description)
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

    public Optional<MeasurementValueComparator> getMeasurementValueComparator() {
        return Optional.ofNullable(measurementValueComparator);
    }

    public Optional<CodingType> getCodingType() {
        return Optional.ofNullable(codingType);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() throws EdifactValidationException {
        if (resultType.equals(LaboratoryInvestigationResultType.NUMERICAL_VALUE)) {
            if (measurementValue == null) {
                throw new EdifactValidationException(KEY + ": Attribute measurementValue is required");
            }

            if (StringUtils.isBlank(measurementUnit)) {
                throw new EdifactValidationException(KEY + ": Attribute measurementUnit is required");
            }
        }

        if (resultType.equals(LaboratoryInvestigationResultType.CODED_VALUE)) {
            if (code == null) {
                throw new EdifactValidationException(KEY + ": Attribute code is required");
            }

            if (description == null) {
                throw new EdifactValidationException(KEY + ": Attribute description is required");
            }
        }
    }
}
