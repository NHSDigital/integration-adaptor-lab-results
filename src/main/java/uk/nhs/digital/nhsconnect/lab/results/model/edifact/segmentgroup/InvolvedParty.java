package uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup;

import lombok.Getter;
import lombok.NonNull;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.MessageRecipientNameAndAddress;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.PerformingOrganisationNameAndAddress;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Reference;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ReferenceType;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.RequesterNameAndAddress;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ServiceProvider;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static uk.nhs.digital.nhsconnect.lab.results.model.edifact.Segment.PLUS_SEPARATOR;

/**
 * Represents information about involved healthcare parties.
 * <p>
 * Segment group 1: {@code S01-NAD-ADR-COM-RFF-SEQ-SPR}
 * <ul>
 *     <li>{@code NAD} is optional. Must be qualified {@code +SLA}, {@code +PO}, or {@code +MR}, if present.</li>
 *     <li>{@code ADR} is not used.</li>
 *     <li>{@code COM} is not used.</li>
 *     <li>{@code RFF} is optional. Must be qualified {@code +AHI}, if present.</li>
 *     <li>{@code SEQ} is not used.</li>
 *     <li>{@code SPR} is mandatory.</li>
 * </ul>
 * <p>
 * Parents: {@link uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message Message}
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class InvolvedParty extends SegmentGroup {
    public static final String INDICATOR = "S01";

    @Getter(lazy = true)
    private final Optional<PerformingOrganisationNameAndAddress> performingOrganisationNameAndAddress =
        extractOptionalSegment(PerformingOrganisationNameAndAddress.KEY_QUALIFIER)
            .map(PerformingOrganisationNameAndAddress::fromString);

    @Getter(lazy = true)
    private final Optional<RequesterNameAndAddress> requesterNameAndAddress =
        extractOptionalSegment(RequesterNameAndAddress.KEY_QUALIFIER)
            .map(RequesterNameAndAddress::fromString);

    @Getter(lazy = true)
    private final Optional<MessageRecipientNameAndAddress> messageRecipientNameAndAddress =
        extractOptionalSegment(MessageRecipientNameAndAddress.KEY_QUALIFIER)
            .map(MessageRecipientNameAndAddress::fromString);

    @Getter(lazy = true)
    private final Optional<Reference> partnerAgreedIdentification =
        extractOptionalSegment(Reference.KEY + PLUS_SEPARATOR + ReferenceType.PARTNER_AGREED_ID.getQualifier())
            .map(Reference::fromString);

    @Getter(lazy = true)
    private final ServiceProvider serviceProvider = ServiceProvider.fromString(extractSegment(ServiceProvider.KEY));

    public static List<InvolvedParty> createMultiple(@NonNull final List<String> edifactSegments) {
        return splitMultipleSegmentGroups(edifactSegments, INDICATOR).stream()
            .map(InvolvedParty::new)
            .collect(toList());
    }

    public InvolvedParty(final List<String> edifactSegments) {
        super(edifactSegments);
    }
}
