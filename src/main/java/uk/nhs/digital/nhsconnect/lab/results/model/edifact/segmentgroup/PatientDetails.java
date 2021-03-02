package uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup;

import lombok.Getter;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.PersonDateOfBirth;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.PersonName;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.PersonSex;

import java.util.List;
import java.util.Optional;

/**
 * Provides further patient identification information.
 * <p>
 * Segment group 7: {@code S07-PNA-DTM-PDI}
 * <ul>
 *     <li>{@code PNA} is mandatory. Must be qualified with {@code +PAT}.</li>
 *     <li>{@code DTM} is optional. Must be qualified with {@code +329}, if present.</li>
 *     <li>{@code PDI} is optional.</li>
 * </ul>
 * <p>
 * Parents: {@link uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message Message}
 * &gt; {@link ServiceReportDetails}
 * &gt; {@link InvestigationSubject}
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class PatientDetails extends SegmentGroup {
    public static final String INDICATOR = "S07";

    @Getter(lazy = true)
    private final PersonName name = PersonName.fromString(extractSegment(PersonName.KEY_QUALIFIER));

    @Getter(lazy = true)
    private final Optional<PersonDateOfBirth> dateOfBirth =
            extractOptionalSegment(PersonDateOfBirth.KEY_QUALIFIER)
                    .map(PersonDateOfBirth::fromString);

    @Getter(lazy = true)
    private final Optional<PersonSex> sex =
            extractOptionalSegment(PersonSex.KEY)
                    .map(PersonSex::fromString);

    public PatientDetails(final List<String> edifactSegments) {
        super(edifactSegments);
    }
}
