package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * Example: NAD+SLA+++ST JAMES?'S UNIVERSITY HOSPITAL'
 */
@Getter
@Setter
@Builder
@RequiredArgsConstructor
public class PerformingOrganisationNameAndAddress extends Segment {

    private static final String KEY = "NAD";
    private static final String QUALIFIER = "SLA";
    public static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;
    private static final int PERFORMING_ORGANISATION_NAME_INDEX_IN_EDIFACT_STRING = 4;

    @NonNull
    private final String performingOrganisationName;

    public static PerformingOrganisationNameAndAddress fromString(String edifactString) {
        if (!edifactString.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException(
                "Can't create " + PerformingOrganisationNameAndAddress.class.getSimpleName() + " from " + edifactString
            );
        }

        String[] keySplit = Split.byPlus(edifactString);
        String performingOrganisationName = keySplit[PERFORMING_ORGANISATION_NAME_INDEX_IN_EDIFACT_STRING];

        return new PerformingOrganisationNameAndAddress(performingOrganisationName);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        return QUALIFIER
            + PLUS_SEPARATOR
            + PLUS_SEPARATOR
            + PLUS_SEPARATOR
            + performingOrganisationName;
    }

    @Override
    protected void validateStateful() throws EdifactValidationException {
        // no stateful fields to validate
    }

    @Override
    public void preValidate() throws EdifactValidationException {
        if (performingOrganisationName.isBlank()) {
            throw new EdifactValidationException(getKey() + ": Attribute performingOrganisationName is required");
        }
    }
}
