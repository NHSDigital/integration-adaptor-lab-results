package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class DiagnosticReportStatus extends Segment {

    private static final String KEY = "STS";

    private final @NonNull String event;

    public static DiagnosticReportStatus fromString(final String edifactString) {
        if (!edifactString.startsWith(KEY)) {
            throw new IllegalArgumentException("Can't create " + DiagnosticReportStatus.class.getSimpleName() + " from " + edifactString);
        }
        String value = Split.byPlus(edifactString)[2];
        return new DiagnosticReportStatus(value);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        return event;
    }

    @Override
    protected void validateStateful() throws EdifactValidationException {

    }

    @Override
    public void preValidate() throws EdifactValidationException {
        if (event.isBlank()) {
            throw new EdifactValidationException(getKey() + ": Status is required");
        }
    }
}