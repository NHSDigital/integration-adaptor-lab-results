package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.apache.commons.lang3.StringUtils;

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

    private static final String KEY = "STS";

    private final String detail;
    @NonNull
    private final ReportStatusCode event;

    public static DiagnosticReportStatus fromString(final String edifactString) {
        if (!edifactString.startsWith(KEY)) {
            throw new IllegalArgumentException("Can't create " + DiagnosticReportStatus.class.getSimpleName()
                + " from " + edifactString);
        }
        String detail = Split.byPlus(edifactString)[1];
        String event = Split.byPlus(edifactString)[2];
        return new DiagnosticReportStatus(detail, ReportStatusCode.fromCode(event));
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        if (StringUtils.isBlank(detail)) {
            return event.getCode();
        } else {
            return detail + PLUS_SEPARATOR + event.getCode();
        }
    }

    @Override
    protected void validateStateful() throws EdifactValidationException {

    }

    @Override
    public void preValidate() throws EdifactValidationException {
        if (event.getCode().isBlank()) {
            throw new EdifactValidationException(getKey() + ": Status is required");
        }
    }

    @Override
    public String toEdifact() throws EdifactValidationException {
        super.validate();
        if (StringUtils.isBlank(detail)) {
            return this.getKey() + PLUS_SEPARATOR + PLUS_SEPARATOR + this.getValue() + TERMINATOR;
        } else {
            return this.getKey() + PLUS_SEPARATOR + this.getValue() + TERMINATOR;
        }
    }
}