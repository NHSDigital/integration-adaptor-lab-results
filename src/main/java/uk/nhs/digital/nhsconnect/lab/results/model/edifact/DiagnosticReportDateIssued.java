package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;
import uk.nhs.digital.nhsconnect.lab.results.utils.TimestampService;

/**
 * Example DTM+ISR:202001280957:203'
 */
@EqualsAndHashCode(callSuper = false)
@Getter
@Builder
public class DiagnosticReportDateIssued extends Segment {

    private static final String KEY = "DTM";
    private static final String QUALIFIER = "ISR";
    private static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;
    private static final String DATE_FORMAT = "203";

    @NonNull
    private final LocalDateTime dateIssued;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyMMddHHmm").withZone(TimestampService.UK_ZONE);

    public static DiagnosticReportDateIssued fromString(final String edifactString) {
        if (!edifactString.startsWith(DiagnosticReportDateIssued.KEY_QUALIFIER)) {
            throw new IllegalArgumentException("Can't create " + DiagnosticReportDateIssued.class.getSimpleName()
                + " from " + edifactString);
        }

        final String dateTime = Split.byColon(Split.byPlus(edifactString)[1])[1];
        final LocalDateTime instant = LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
        return new DiagnosticReportDateIssued(instant);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        return QUALIFIER
            + COLON_SEPARATOR
            + DATE_TIME_FORMATTER.format(dateIssued)
            + COLON_SEPARATOR
            + DATE_FORMAT;
    }

    @Override
    protected void validateStateful() throws EdifactValidationException {

    }

    @Override
    public void preValidate() throws EdifactValidationException { }
}
