package uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup;

import static java.util.stream.Collectors.toList;

import java.util.List;
import lombok.Getter;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.DiagnosticReportCode;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.DiagnosticReportDateIssued;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.DiagnosticReportStatus;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.MessageTrailer;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ReferenceDiagnosticReport;

/**
 * Provides general information about the laboratory service report from the laboratory service provider.
 * <p>
 * Segment group 2: {@code S02-GIS-RFF-STS-DTM-FTX-SG4-SG6}
 * <p>
 * Parents: {@link uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message Message}
 */
public class ServiceReportDetails extends SegmentGroup {
    public static final String INDICATOR = "S02";

    // GIS
    @Getter(lazy = true)
    private final DiagnosticReportCode diagnosticReportCode =
        DiagnosticReportCode.fromString(extractSegment(DiagnosticReportCode.KEY));

    // RFF+SRI
    @Getter(lazy = true)
    private final ReferenceDiagnosticReport referenceDiagnosticReport =
        ReferenceDiagnosticReport.fromString(extractSegment(ReferenceDiagnosticReport.KEY_QUALIFIER));

    // STS
    @Getter(lazy = true)
    private final DiagnosticReportStatus diagnosticReportStatus =
        DiagnosticReportStatus.fromString(extractSegment(DiagnosticReportStatus.KEY));

    // DTM+ISR
    @Getter(lazy = true)
    private final DiagnosticReportDateIssued diagnosticReportDateIssued =
        DiagnosticReportDateIssued.fromString(extractSegment(DiagnosticReportDateIssued.KEY_QUALIFIER));

    // S06
    @Getter(lazy = true)
    private final InvestigationSubject investigationSubject = new InvestigationSubject(getEdifactSegments().stream()
        .dropWhile(segment -> !segment.startsWith(InvestigationSubject.INDICATOR))
        .takeWhile(segment -> !segment.startsWith(MessageTrailer.KEY))
        .collect(toList()));

    public ServiceReportDetails(final List<String> edifactSegments) {
        super(edifactSegments);
    }
}
