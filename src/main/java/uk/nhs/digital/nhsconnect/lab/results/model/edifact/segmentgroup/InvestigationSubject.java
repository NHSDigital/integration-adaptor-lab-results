package uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup;

import lombok.Getter;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.MessageTrailer;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Reference;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.ReferenceType;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.UnstructuredAddress;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static uk.nhs.digital.nhsconnect.lab.results.model.edifact.Segment.PLUS_SEPARATOR;

/**
 * Provides information about the subject of investigation; that is, the patient.
 * <p>
 * Segment group 6: {@code S06-RFF-ADR-COM-SG7-SG10-SG16-SG18}
 * <ul>
 *     <li>{@code RFF} is optional. Must be qualified {@code +SSI}, if present.</li>
 *     <li>{@code ADR} is optional.</li>
 *     <li>{@code COM} is not used.</li>
 *     <li>{@code SG7} is mandatory.</li>
 *     <li>{@code SG10} is optional.</li>
 *     <li>{@code SG16} is mandatory. May have up to 99 instances.</li>
 *     <li>{@code SG18} is mandatory. May have up to 99 instances.</li>
 * </ul>>
 * <p>
 * Parents: {@link uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message Message}
 * &gt; {@link ServiceReportDetails}
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class InvestigationSubject extends SegmentGroup {
    public static final String INDICATOR = "S06";

    @Getter(lazy = true)
    private final Optional<Reference> referenceServiceSubject =
        extractOptionalSegment(Reference.KEY + PLUS_SEPARATOR + ReferenceType.SERVICE_SUBJECT.getQualifier())
            .map(Reference::fromString);

    @Getter(lazy = true)
    private final Optional<UnstructuredAddress> address =
        extractOptionalSegment(UnstructuredAddress.KEY)
            .map(UnstructuredAddress::fromString);

    @Getter(lazy = true)
    private final PatientDetails details = new PatientDetails(getEdifactSegments().stream()
        .dropWhile(segment -> !segment.startsWith(PatientDetails.INDICATOR))
        .takeWhile(segment -> !segment.startsWith(SpecimenDetails.INDICATOR))
        .takeWhile(segment -> !segment.startsWith(MessageTrailer.KEY))
        .collect(toList()));

    @Getter(lazy = true)
    private final Optional<PatientClinicalInfo> clinicalInfo = PatientClinicalInfo.createOptional(
        getEdifactSegments().stream()
            .dropWhile(segment -> !segment.startsWith(PatientClinicalInfo.INDICATOR))
            .takeWhile(segment -> !segment.startsWith(SpecimenDetails.INDICATOR))
            .collect(toList()));

    @Getter(lazy = true)
    private final List<SpecimenDetails> specimens = SpecimenDetails.createMultiple(getEdifactSegments().stream()
        .dropWhile(segment -> !segment.startsWith(SpecimenDetails.INDICATOR))
        .takeWhile(segment -> !segment.startsWith(LabResult.INDICATOR))
        .takeWhile(segment -> !segment.startsWith(MessageTrailer.KEY))
        .collect(toList()));

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
