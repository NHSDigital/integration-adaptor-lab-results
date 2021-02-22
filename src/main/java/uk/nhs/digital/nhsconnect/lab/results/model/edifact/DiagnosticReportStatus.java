package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * Example STS++UN'
 */
@EqualsAndHashCode(callSuper = false)
@Builder
@Getter
public class DiagnosticReportStatus extends Segment {

    public static final String KEY = "STS";

    private final String detail;
    @NonNull
    private final ReportStatusCode event;

    public static DiagnosticReportStatus fromString(final String edifactString) {
        if (!edifactString.startsWith(KEY)) {
            throw new IllegalArgumentException("Can't create " + DiagnosticReportStatus.class.getSimpleName()
                + " from " + edifactString);
        }
        final String detail = Split.byPlus(edifactString)[1];
        final String event = Split.byPlus(edifactString)[2];
        return new DiagnosticReportStatus(detail, ReportStatusCode.fromCode(event));
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() throws EdifactValidationException {
        if (event.getCode().isBlank()) {
            throw new EdifactValidationException(getKey() + ": Status is required");
        }
    }
}