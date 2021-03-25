package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.DateFormat;

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

    @NonNull
    private final String dateIssued;
    @NonNull
    private final DateFormat dateFormat;

    public static DiagnosticReportDateIssued fromString(final String edifactString) {
        if (!edifactString.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException("Can't create " + DiagnosticReportDateIssued.class.getSimpleName()
                + " from " + edifactString);
        }

        final String[] keySplit = Split.byPlus(edifactString);
        final String[] subFieldSplit = Split.byColon(keySplit[1]);
        final String dateIssued = subFieldSplit[1];
        final String dateFormat = subFieldSplit[2];

        return DiagnosticReportDateIssued.builder()
            .dateIssued(dateIssued)
            .dateFormat(DateFormat.fromCode(dateFormat))
            .build();
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() throws EdifactValidationException { }
}
