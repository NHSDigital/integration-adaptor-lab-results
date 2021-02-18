package uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.PersonDateOfBirth;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.PersonName;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.PersonSex;

/**
 * Provides further patient identification information.
 * <p>
 * Segment group 7: {@code S07-PNA-DTM-PDI}
 * <p>
 * Parents: {@link uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message Message}
 * &gt; {@link ServiceReportDetails}
 * &gt; {@link InvestigationSubject}
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class PatientDetails extends SegmentGroup {
    public static final String INDICATOR = "S07";

    // PNA+PAT
    @Getter(lazy = true)
    private final PersonName personName = PersonName.fromString(extractSegment(PersonName.KEY_QUALIFIER));

    // DTM+329?
    @Getter(lazy = true)
    private final Optional<PersonDateOfBirth> personDateOfBirth =
        extractOptionalSegment(PersonDateOfBirth.KEY_QUALIFIER)
            .map(PersonDateOfBirth::fromString);

    // PDI?
    @Getter(lazy = true)
    private final Optional<PersonSex> personSex =
        extractOptionalSegment(PersonSex.KEY)
            .map(PersonSex::fromString);

    public PatientDetails(final List<String> edifactSegments) {
        super(edifactSegments);
    }
}
