package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Example PNA+PAT+9435492908:OPI+++SU:KENNEDY+FO:SARAH+TI:MISS+MI:ANGELA'
 */
@Getter
@Builder
@EqualsAndHashCode(callSuper = false)
public class PersonName extends Segment {

    private static final String KEY = "PNA";
    private static final String QUALIFIER = "PAT";
    private static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;
    private static final String FAMILY_NAME_QUALIFIER = "SU";
    private static final String FIRST_NAME_QUALIFIER = "FO";
    private static final String MIDDLE_NAME_QUALIFIER = "MI";
    private static final String TITLE_QUALIFIER = "TI";

    private final String nhsNumber;
    private final PatientIdentificationType patientIdentificationType;
    private final String surname;
    private final String firstForename;
    private final String title;
    private final String secondForename;

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        List<String> values = new ArrayList<>();
        values.add(QUALIFIER);

        values.add(Optional.ofNullable(this.nhsNumber)
            .map(value -> value + COLON_SEPARATOR + this.patientIdentificationType.getCode())
            .orElse(StringUtils.EMPTY));
        values.add(StringUtils.EMPTY);
        values.add(StringUtils.EMPTY);
        values.add(Optional.ofNullable(this.surname)
            .map(value -> FAMILY_NAME_QUALIFIER + COLON_SEPARATOR + value)
            .orElse(StringUtils.EMPTY));
        values.add(Optional.ofNullable(this.firstForename)
            .map(value -> FIRST_NAME_QUALIFIER + COLON_SEPARATOR + value)
            .orElse(StringUtils.EMPTY));
        values.add(Optional.ofNullable(this.title)
            .map(value -> TITLE_QUALIFIER + COLON_SEPARATOR + value)
            .orElse(StringUtils.EMPTY));
        values.add(Optional.ofNullable(this.secondForename)
            .map(value -> MIDDLE_NAME_QUALIFIER + COLON_SEPARATOR + value)
            .orElse(StringUtils.EMPTY));

        values = removeEmptyTrailingFields(values, StringUtils::isNotBlank);

        return String.join(PLUS_SEPARATOR, values);
    }

    @Override
    public void preValidate() throws EdifactValidationException {
        if (isBlank(nhsNumber) && patientIdentificationType == null && isBlank(surname)) {
            throw new EdifactValidationException(getKey() + ": At least one of patient identification and person "
                + "name details are required");
        }
    }

    @Override
    protected void validateStateful() throws EdifactValidationException {
    }

    public static PersonName fromString(final String edifactString) {
        if (!edifactString.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException("Can't create " + PersonName.class.getSimpleName()
                + " from " + edifactString);
        }

        final String nhsNumber = extractNhsNumber(edifactString);
        final PatientIdentificationType patientIdentificationType = getPatientIdentificationType(edifactString);
        final String surname = extractNamePart(FAMILY_NAME_QUALIFIER, edifactString);

        if (isBlank(nhsNumber) && patientIdentificationType == null && isBlank(surname)) {
            throw new EdifactValidationException(KEY + ": At least one of patient identification and person name "
                + "details are required");
        }

        return PersonName.builder()
            .nhsNumber(nhsNumber)
            .patientIdentificationType(patientIdentificationType)
            .surname(surname)
            .firstForename(extractNamePart(FIRST_NAME_QUALIFIER, edifactString))
            .title(extractNamePart(TITLE_QUALIFIER, edifactString))
            .secondForename(extractNamePart(MIDDLE_NAME_QUALIFIER, edifactString))
            .build();
    }

    private static String extractNhsNumber(final String edifactString) {
        final String[] components = Split.byPlus(edifactString);
        if (components.length > 2 && StringUtils.isNotBlank(components[2])) {
            return Split.byColon(components[2])[0];
        }
        return null;
    }

    private static PatientIdentificationType getPatientIdentificationType(final String edifactString) {
        final String[] components = Split.byPlus(edifactString);
        if (StringUtils.isNotBlank(extractNhsNumber(edifactString)) && components.length > 1) {
            return PatientIdentificationType.fromCode(Split.byColon(components[2])[1]);
        }
        return null;
    }

    private static String extractNamePart(final String qualifier, final String text) {
        return Arrays.stream(Split.byPlus(text))
            .filter(value -> value.startsWith(qualifier))
            .map(value -> Split.byColon(value)[1])
            .findFirst()
            .orElse(null);
    }

}
