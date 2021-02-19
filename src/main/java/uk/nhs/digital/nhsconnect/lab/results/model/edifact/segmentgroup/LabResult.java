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
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Reference;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.TestStatus;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * Provides information about laboratory investigation results.
 * <p>
 * Segment group 18: {@code GIS-INV-SEQ-RSL-STS-DTM-FTX-RFF-SG19-SG20}
 * <ul>
 *     <li>{@code GIS} is mandatory.</li>
 *     <li>{@code INV} is mandatory. Must be qualified {@code +MQ}.</li>
 *     <li>{@code SEQ} is optional.</li>
 *     <li>{@code RSL} is optional. Must be qualified {@code +NV}, if present.</li>
 *     <li>{@code STS} is optional.</li>
 *     <li>{@code DTM} is not used.</li>
 *     <li>{@code FTX} is optional. May have up to 99 instances. Each must be qualified {@code +SPC}, {@code +RIT},
 *     or {@code +CRR}.</li>
 *     <li>{@code RFF} is mandatory.</li>
 *     <li>{@code SG19} is not used.</li>
 *     <li>{@code SG20} is optional. May have up to 9 instances.</li>
 * </ul>
 * <p>
 * Parents: {@link uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message Message}
 * &gt; {@link ServiceReportDetails}
 * &gt; {@link InvestigationSubject}
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class LabResult extends SegmentGroup {
    public static final String INDICATOR = DiagnosticReportCode.KEY;

    @Getter(lazy = true)
    private final DiagnosticReportCode diagnosticReportCode =
        DiagnosticReportCode.fromString(extractSegment(DiagnosticReportCode.KEY));

    @Getter(lazy = true)
    private final LaboratoryInvestigation laboratoryInvestigation =
        LaboratoryInvestigation.fromString(extractSegment(LaboratoryInvestigation.KEY_QUALIFIER));

    @Getter(lazy = true)
    private final Optional<SequenceDetails> sequenceDetails =
        extractOptionalSegment(SequenceDetails.KEY)
            .map(SequenceDetails::fromString);

    @Getter(lazy = true)
    private final Optional<LaboratoryInvestigationResult> laboratoryInvestigationResult =
        extractOptionalSegment(LaboratoryInvestigationResult.KEY_QUALIFIER)
            .map(LaboratoryInvestigationResult::fromString);

    @Getter(lazy = true)
    private final Optional<TestStatus> testStatus =
        extractOptionalSegment(TestStatus.KEY)
            .map(TestStatus::fromString);

    @Getter(lazy = true)
    private final List<FreeTextSegment> freeTexts =
        extractSegments(FreeTextSegment.KEY).stream()
            .map(FreeTextSegment::fromString)
            .filter(segment -> List.of(FreeTextType.SERVICE_PROVIDER_COMMENT, FreeTextType.INVESTIGATION_RESULT,
                FreeTextType.COMPLEX_REFERENCE_RANGE).contains(segment.getType()))
            .collect(toList());

    @Getter(lazy = true)
    private final Reference sequenceReference = Reference.fromString(
        extractSegment(Reference.KEY));

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
