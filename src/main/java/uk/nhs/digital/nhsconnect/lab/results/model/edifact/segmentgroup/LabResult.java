package uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup;

import lombok.Getter;
import lombok.NonNull;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.DiagnosticReportCode;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.FreeTextSegment;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.FreeTextType;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.LaboratoryInvestigation;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.LaboratoryInvestigationResult;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.MessageTrailer;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.SequenceDetails;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.SequenceReference;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.TestStatus;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * Provides information about laboratory investigation results.
 * <p>
 * Segment group 18: {@code GIS-INV-SEQ-RSL-STS-DTM-FTX-RFF-SG19-SG20}
 * <p>
 * Parents: {@link uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message Message}
 * &gt; {@link ServiceReportDetails}
 * &gt; {@link InvestigationSubject}
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class LabResult extends SegmentGroup {
    public static final String INDICATOR = DiagnosticReportCode.KEY;

    // GIS
    @Getter(lazy = true)
    private final DiagnosticReportCode diagnosticReportCode =
        DiagnosticReportCode.fromString(extractSegment(DiagnosticReportCode.KEY));

    // INV+MQ
    @Getter(lazy = true)
    private final LaboratoryInvestigation laboratoryInvestigation =
        LaboratoryInvestigation.fromString(extractSegment(LaboratoryInvestigation.KEY_QUALIFIER));

    // SEQ?
    @Getter(lazy = true)
    private final Optional<SequenceDetails> sequenceDetails =
        extractOptionalSegment(SequenceDetails.KEY)
            .map(SequenceDetails::fromString);

    // RSL+NV?
    @Getter(lazy = true)
    private final Optional<LaboratoryInvestigationResult> laboratoryInvestigationResult =
        extractOptionalSegment(LaboratoryInvestigationResult.KEY_QUALIFIER)
            .map(LaboratoryInvestigationResult::fromString);

    // STS?
    @Getter(lazy = true)
    private final Optional<TestStatus> testStatus =
        extractOptionalSegment(TestStatus.KEY)
            .map(TestStatus::fromString);

    // DTM not used

    // FTX(SPC|RIT|CRR){,99}
    @Getter(lazy = true)
    private final List<FreeTextSegment> freeTexts =
        extractSegments(FreeTextSegment.KEY).stream()
            .map(FreeTextSegment::fromString)
            .filter(segment -> List.of(FreeTextType.SERVICE_PROVIDER_COMMENT, FreeTextType.INVESTIGATION_RESULT,
                FreeTextType.COMPLEX_REFERENCE_RANGE).contains(segment.getType()))
            .collect(toList());

    // RFF
    @Getter(lazy = true)
    private final SequenceReference sequenceReference = SequenceReference.fromString(
        extractSegment(SequenceReference.KEY));

    // SG19 not used

    // S20{,9}
    @Getter(lazy = true)
    private final List<ResultReferenceRange> resultReferenceRanges =
        ResultReferenceRange.createMultiple(getEdifactSegments().stream()
            .dropWhile(segment -> !segment.startsWith(ResultReferenceRange.INDICATOR))
            .takeWhile(segment -> !segment.startsWith(MessageTrailer.KEY))
            .collect(toList()));

    public static List<LabResult> createMultiple(@NonNull final List<String> edifactSegments) {
        return splitMultipleSegmentGroups(edifactSegments, INDICATOR).stream()
            .map(LabResult::new)
            .collect(toList());
    }

    public LabResult(final List<String> edifactSegments) {
        super(edifactSegments);
    }
}
