package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.apache.commons.lang3.StringUtils;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * Example: NAD+SLA+A2442389:902++DR J SMITH'
 * <br>
 * Example: NAD+SLA+++Haematology'
 * <br>
 * Example: NAD+SLA+++ST JAMES?'S UNIVERSITY HOSPITAL'
 * <br>
 */
@Getter
@Builder
@RequiredArgsConstructor
public class PerformerNameAndAddress extends Segment {

    private static final String KEY = "NAD";
    private static final String QUALIFIER = "SLA";
    public static final String KEY_QUALIFIER = KEY + "+" + QUALIFIER;

    private static final int PERFORMING_NAME_INDEX_IN_EDIFACT_STRING = 4;

    private final String identifier;
    private final HealthcareRegistrationIdentificationCode code;
    private final String performerName;
    private final String performingOrganisationName;

    public static PerformerNameAndAddress fromString(String edifactString) {
        if (!edifactString.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException(
                "Can't create " + PerformerNameAndAddress.class.getSimpleName() + " from " + edifactString
            );
        }

        String[] keySplit = Split.byPlus(edifactString);
        String[] colonSplit = Split.byColon(keySplit[2]);
        String performerID = colonSplit[0];

        final boolean isPerformingOrganisation = StringUtils.isBlank(performerID);
        if (isPerformingOrganisation) {
            // if identifier is blank - organisation/department
            String performingOrganisationName = keySplit[PERFORMING_NAME_INDEX_IN_EDIFACT_STRING];
            return PerformerNameAndAddress.builder()
                .performingOrganisationName(performingOrganisationName)
                .build();
        } else {
            String performerCode = colonSplit[1];
            String performerName = keySplit[PERFORMING_NAME_INDEX_IN_EDIFACT_STRING];
            return PerformerNameAndAddress.builder()
                .identifier(performerID)
                .code(HealthcareRegistrationIdentificationCode.fromCode(performerCode))
                .performerName(performerName)
                .build();
        }
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() throws EdifactValidationException {
        if (StringUtils.isBlank(identifier) && StringUtils.isBlank(performingOrganisationName)) {
            throw new EdifactValidationException(getKey() + ": Attribute performingOrganisationName is required");
        } else {
            if (code == null || StringUtils.isBlank(code.getCode())) {
                throw new EdifactValidationException(getKey() + ": Attribute code is required");
            }
            if (StringUtils.isBlank(performerName)) {
                throw new EdifactValidationException(getKey() + ": Attribute performerName is required");
            }
        }
    }
}
