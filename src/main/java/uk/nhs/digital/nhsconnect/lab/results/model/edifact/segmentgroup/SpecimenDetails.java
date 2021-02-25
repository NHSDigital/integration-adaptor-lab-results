package uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup;

import lombok.Getter;
import lombok.NonNull;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.FreeTextSegment;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.FreeTextType;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Reference;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ReferenceType;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.SequenceDetails;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.SpecimenCharacteristicType;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.SpecimenCollectionDateTime;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.SpecimenCollectionReceiptDateTime;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.SpecimenQuantity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.nhs.digital.nhsconnect.lab.results.model.edifact.Segment.PLUS_SEPARATOR;

/**
 * Provides information about a specimen.
 * <p>
 * Segment group 16: {@code S16-SEQ-SPC-PRC-RFF-QTY-DTM-FTX}
 * <ul>
 *     <li>{@code SEQ} is optional.</li>
 *     <li>{@code SPC} is mandatory. Must be qualified with {@code +TSP}.
 *     May optionally also be present qualified with {@code +FS}.</li>
 *     <li>{@code PRC} is not used.</li>
 *     <li>{@code RFF} is optional. May be qualified with {@code +RTI} or {@code +STI}.
 *     May have up to 2 instances.</li>
 *     <li>{@code QTY} is optional.</li>
 *     <li>{@code DTM} is optional. May be qualified with {@code +SCO} or {@code +SRI}.
 *     May have up to 2 instances.</li>
 *     <li>{@code FTX} is optional. May have up to 9 instances. Each must be qualified with {@code +SPC}.</li>
 * </ul>
 * <p>
 * Parents: {@link uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message Message}
 * &gt; {@link ServiceReportDetails}
 * &gt; {@link InvestigationSubject}
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SpecimenDetails extends SegmentGroup {
    public static final String INDICATOR = "S16";

    @Getter(lazy = true)
    private final Optional<SequenceDetails> sequenceDetails =
        extractOptionalSegment(SequenceDetails.KEY)
            .map(SequenceDetails::fromString);

    @Getter(lazy = true)
    private final SpecimenCharacteristicType characteristicType =
        SpecimenCharacteristicType.fromString(extractSegment(SpecimenCharacteristicType.KEY_QUALIFIER));

    @Getter(lazy = true)
    private final Optional<Reference> serviceRequesterReference =
        extractOptionalSegment(Reference.KEY + PLUS_SEPARATOR + ReferenceType.SPECIMEN_BY_REQUESTER.getQualifier())
            .map(Reference::fromString);

    @Getter(lazy = true)
    private final Optional<Reference> serviceProviderReference =
        extractOptionalSegment(Reference.KEY + PLUS_SEPARATOR + ReferenceType.SPECIMEN_BY_PROVIDER.getQualifier())
            .map(Reference::fromString);

    @Getter(lazy = true)
    private final Optional<SpecimenQuantity> quantity =
        extractOptionalSegment(SpecimenQuantity.KEY_QUALIFIER)
            .map(SpecimenQuantity::fromString);

    @Getter(lazy = true)
    private final Optional<SpecimenCollectionDateTime> collectionDateTime =
        extractOptionalSegment(SpecimenCollectionDateTime.KEY_QUALIFIER)
            .map(SpecimenCollectionDateTime::fromString);

    @Getter(lazy = true)
    private final Optional<SpecimenCollectionReceiptDateTime> collectionReceiptDateTime =
        extractOptionalSegment(SpecimenCollectionReceiptDateTime.KEY_QUALIFIER)
            .map(SpecimenCollectionReceiptDateTime::fromString);

    @Getter(lazy = true)
    private final List<FreeTextSegment> freeTexts =
        extractSegments(FreeTextSegment.KEY + PLUS_SEPARATOR + FreeTextType.SERVICE_PROVIDER_COMMENT.getQualifier())
            .stream()
            .map(FreeTextSegment::fromString)
            .collect(Collectors.toList());

    public static List<SpecimenDetails> createMultiple(@NonNull final List<String> edifactSegments) {
        return splitMultipleSegmentGroups(edifactSegments, INDICATOR).stream()
            .map(SpecimenDetails::new)
            .collect(Collectors.toList());
    }

    public SpecimenDetails(final List<String> edifactSegments) {
        super(edifactSegments);
    }
}
