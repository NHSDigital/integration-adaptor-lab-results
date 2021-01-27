package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;
import uk.nhs.digital.nhsconnect.lab.results.utils.TimestampService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Example DTM+329:19450730:102'
 */
@EqualsAndHashCode(callSuper = false)
@Builder
@Data
public class PersonDateOfBirth extends Segment {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyyMMdd").withZone(TimestampService.UK_ZONE);
    private static final String KEY = "DTM";
    private static final String QUALIFIER = "329";
    private static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;
    private static final String DATE_FORMAT = "102";
    private final LocalDate dateOfBirth;

    public static PersonDateOfBirth fromString(final String edifactString) {
        if (!edifactString.startsWith(PersonDateOfBirth.KEY_QUALIFIER)) {
            throw new IllegalArgumentException("Can't create " + PersonDateOfBirth.class.getSimpleName() + " from " + edifactString);
        }
        final String dateTime = Split.byColon(Split.byPlus(edifactString)[1])[1];
        final LocalDate instant = LocalDate.parse(dateTime, DATE_TIME_FORMATTER);
        return new PersonDateOfBirth(instant);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        return QUALIFIER
            .concat(COLON_SEPARATOR)
            .concat(DATE_TIME_FORMATTER.format(dateOfBirth))
            .concat(COLON_SEPARATOR)
            .concat(DATE_FORMAT);
    }

    @Override
    protected void validateStateful() throws EdifactValidationException {
    }

    @Override
    public void preValidate() throws EdifactValidationException {
        if (dateOfBirth == null) {
            throw new EdifactValidationException(getKey() + ": Date of birth is required");
        }
    }
}
