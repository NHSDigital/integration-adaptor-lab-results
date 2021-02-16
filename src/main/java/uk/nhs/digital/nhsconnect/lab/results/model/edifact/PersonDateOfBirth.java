package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Example DTM+329:19450730:102'
 */
@Getter
@EqualsAndHashCode(callSuper = false)
@Builder
public class PersonDateOfBirth extends Segment {

    protected static final String KEY = "DTM";
    private static final String QUALIFIER = "329";
    private static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;
    private static final DateTimeFormatter DATE_FORMATTER_CCYY = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter DATE_FORMATTER_CCYYMM = DateTimeFormatter.ofPattern("yyyyMM");
    private static final DateTimeFormatter DATE_FORMATTER_CCYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    @NonNull
    private final String dateOfBirth;
    @NonNull
    private final DateFormat dateFormat;

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        return QUALIFIER
            + COLON_SEPARATOR
            + getFormattedEdifactDate(dateOfBirth, dateFormat)
            + COLON_SEPARATOR
            + dateFormat.getCode();
    }

    @Override
    protected void validateStateful() throws EdifactValidationException {
    }

    @Override
    public void preValidate() throws EdifactValidationException {
        if (dateOfBirth.isBlank()) {
            throw new EdifactValidationException(getKey() + ": Date of birth is required");
        }
    }

    public static PersonDateOfBirth fromString(final String edifactString) {
        if (!edifactString.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException("Can't create " + PersonDateOfBirth.class.getSimpleName()
                + " from " + edifactString);
        }
        final String input = Split.byPlus(edifactString)[1];
        final String dateOfBirth = Split.byColon(input)[1];
        final String format = Split.bySegmentTerminator(Split.byColon(input)[2])[0];

        final PersonDateOfBirthBuilder dateOfBirthBuilder = PersonDateOfBirth.builder();

        if (isNotBlank(dateOfBirth) && isNotBlank(format)) {
            final String formattedFhirDate = getFormattedFhirDate(dateOfBirth, format);

            dateOfBirthBuilder
                .dateOfBirth(formattedFhirDate)
                .dateFormat(DateFormat.fromCode(format));
        }

        return dateOfBirthBuilder.build();
    }

    private static String getFormattedFhirDate(final String dateOfBirth, final String dateFormat) {
        if (dateFormat.equals(DateFormat.CCYY.getCode())) {
            return Year.parse(dateOfBirth, DATE_FORMATTER_CCYY).toString();
        } else if (dateFormat.equals(DateFormat.CCYYMM.getCode())) {
            return YearMonth.parse(dateOfBirth, DATE_FORMATTER_CCYYMM).toString();
        } else if (dateFormat.equals(DateFormat.CCYYMMDD.getCode())) {
            return LocalDate.parse(dateOfBirth, DATE_FORMATTER_CCYYMMDD).toString();
        }
        throw new UnsupportedOperationException(KEY + ": Date format code " + dateFormat + " is not supported");
    }

    private static String getFormattedEdifactDate(final String dateOfBirth, final DateFormat dateFormat) {
        if (dateFormat.equals(DateFormat.CCYY)) {
            return DATE_FORMATTER_CCYY.format(Year.parse(dateOfBirth));
        } else if (dateFormat.equals(DateFormat.CCYYMM)) {
            return DATE_FORMATTER_CCYYMM.format(YearMonth.parse(dateOfBirth));
        } else if (dateFormat.equals(DateFormat.CCYYMMDD)) {
            return DATE_FORMATTER_CCYYMMDD.format(LocalDate.parse(dateOfBirth));
        }
        throw new UnsupportedOperationException(KEY + ": Date format " + dateFormat.name() + " is not supported");
    }
}
