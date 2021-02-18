package uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;

import lombok.Getter;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.MessageTrailer;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ReferenceServiceSubject;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.UnstructuredAddress;

/**
 * Provides information about the subject of investigation; that is, the patient.
 * <p>
 * Segment group 6: {@code S06-RFF-ADR-COM-SG7-SG10-SG16-SG18}
 * <p>
 * Parents: {@link uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message Message}
 * &gt; {@link ServiceReportDetails}
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class InvestigationSubject extends SegmentGroup {
    public static final String INDICATOR = "S06";

    // RFF+SSI?
    @Getter(lazy = true)
    private final Optional<ReferenceServiceSubject> referenceServiceSubject =
        extractOptionalSegment(ReferenceServiceSubject.KEY_QUALIFIER)
            .map(ReferenceServiceSubject::fromString);

    // ADR?
    @Getter(lazy = true)
    private final Optional<UnstructuredAddress> unstructuredAddress =
        extractOptionalSegment(UnstructuredAddress.KEY)
            .map(UnstructuredAddress::fromString);

    // COM not used

    // S07
    @Getter(lazy = true)
    private final PatientDetails patientDetails = new PatientDetails(getEdifactSegments().stream()
        .dropWhile(segment -> !segment.startsWith(PatientDetails.INDICATOR))
        .takeWhile(segment -> !segment.startsWith(Specimen.INDICATOR))
        .takeWhile(segment -> !segment.startsWith(MessageTrailer.KEY))
        .collect(toList()));

    // S10?
    @Getter(lazy = true)
    private final Optional<PatientClinicalInfo> patientClinicalInfo = PatientClinicalInfo.createOptional(
        getEdifactSegments().stream()
            .dropWhile(segment -> !segment.startsWith(PatientClinicalInfo.INDICATOR))
            .takeWhile(segment -> !segment.startsWith(Specimen.INDICATOR))
            .collect(toList()));

    // S016{1,99}
    @Getter(lazy = true)
    private final List<Specimen> specimens = Specimen.createMultiple(getEdifactSegments().stream()
        .dropWhile(segment -> !segment.startsWith(Specimen.INDICATOR))
        .takeWhile(segment -> !segment.startsWith(LabResult.INDICATOR))
        .takeWhile(segment -> !segment.startsWith(MessageTrailer.KEY))
        .collect(toList()));

    // SG18{1,99}
    @Getter(lazy = true)
    private final List<LabResult> labResults =
        LabResult.createMultiple(getEdifactSegments().stream()
            .dropWhile(segment -> !segment.startsWith(LabResult.INDICATOR))
            .takeWhile(segment -> !segment.startsWith(MessageTrailer.KEY))
            .collect(toList()));

    public InvestigationSubject(final List<String> edifactSegments) {
        super(edifactSegments);
    }
}
