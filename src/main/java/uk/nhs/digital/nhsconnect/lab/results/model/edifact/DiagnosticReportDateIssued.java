package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;
import uk.nhs.digital.nhsconnect.lab.results.utils.TimestampService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Example DTM+ISR:202001280957:203'
 */
@EqualsAndHashCode(callSuper = false)
@Getter
@Builder
public class DiagnosticReportDateIssued extends Segment {

    private static final String KEY = "DTM";
    private static final String QUALIFIER = "ISR";
    public static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;
    private static final String DATE_FORMAT = "203";

    @NonNull
    private final LocalDateTime dateIssued;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyMMddHHmm").withZone(TimestampService.UK_ZONE);

    public static DiagnosticReportDateIssued fromString(final String edifactString) {
        if (!edifactString.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException("Can't create " + DiagnosticReportDateIssued.class.getSimpleName()
                + " from " + edifactString);
        }

        final String[] keySplit = Split.byPlus(edifactString);
        final String[] subFieldSplit = Split.byColon(keySplit[1]);
        final String dateTime = subFieldSplit[1];
        final LocalDateTime instant = LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
        return new DiagnosticReportDateIssued(instant);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() throws EdifactValidationException { }
}
