package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * Example: NAD+SLA+A2442389:902++DR J SMITH'
 * <br>
 * Example: NAD+SLA+++ST JAMES?'S UNIVERSITY HOSPITAL'
 */
@Getter
@Setter
@Builder
@RequiredArgsConstructor
public class PerformerNameAndAddress extends Segment {

    private static final String KEY = "NAD";
    private static final String QUALIFIER = "SLA";
    public static final String KEY_QUALIFIER = KEY + "+" + QUALIFIER;

    private static final int PERFORMING_NAME_INDEX_IN_EDIFACT_STRING = 4;
    private static final int PERFORMER_ID_INDEX_IN_EDIFACT_STRING = 2;
    private static final int PERFORMER_CODE_INDEX_IN_EDIFACT_STRING = 3;

    private final String identifier;
    private final HealthcareRegistrationIdentificationCode code;
    private final String performingOrganisationName;
    private final String performerName;

    public static PerformerNameAndAddress fromString(String edifactString) {
        if (!edifactString.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException(
                "Can't create " + PerformerNameAndAddress.class.getSimpleName() + " from " + edifactString
            );
        }

        String[] keySplit = Split.byPlus(edifactString);
        String[] colonSplit = Split.byColon(keySplit[2]);
        String performerID = colonSplit[0];

        if (performerID.isBlank()) {
            // if identifier is blank - organisation/department
            String performingOrganisationName = keySplit[PERFORMING_NAME_INDEX_IN_EDIFACT_STRING];
            return new PerformerNameAndAddress("", null, performingOrganisationName, "");
        } else {
            String performerCode = colonSplit[1];
            String performerName = keySplit[PERFORMING_NAME_INDEX_IN_EDIFACT_STRING];
            return new PerformerNameAndAddress(
                performerID,
                HealthcareRegistrationIdentificationCode.fromCode(performerCode),
                "",
                performerName
            );
        }
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() throws EdifactValidationException {
        if (this.identifier.isBlank()) {
            if (performingOrganisationName == null || performingOrganisationName.isBlank()) {
                throw new EdifactValidationException(getKey() + ": Attribute performingOrganisationName is required");
            }
        } else {
            if (code == null || code.getCode().isBlank()) {
                throw new EdifactValidationException(getKey() + ": Attribute code is required");
            }
            if (performerName == null || performerName.isBlank()) {
                throw new EdifactValidationException(getKey() + ": Attribute performerName is required");
            }
        }
    }
}
