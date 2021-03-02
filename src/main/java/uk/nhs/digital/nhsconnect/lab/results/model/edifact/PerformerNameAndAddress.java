package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.apache.commons.lang3.StringUtils;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * Example: Details of performing practitioner
 * NAD+SLA+A2442389:902++DR J SMITH'
 * SPR+PRO'
 * <br>
 * Example: Details of performing organization's department
 * NAD+SLA+++Haematology'
 * SPR+DPT'
 * <br>
 * Example: Details of performing organization
 * NAD+SLA+++ST JAMES?'S UNIVERSITY HOSPITAL'
 * SPR+ORG'
 */
@Getter
@Builder
@RequiredArgsConstructor
public class PerformerNameAndAddress extends Segment {

    private static final String KEY = "NAD";
    private static final String QUALIFIER = "SLA";
    public static final String KEY_QUALIFIER = KEY + "+" + QUALIFIER;

    private final String identifier;
    private final HealthcareRegistrationIdentificationCode code;
    private final String practitionerName;
    private final String organizationName;

    @SuppressWarnings("checkstyle:MagicNumber")
    public static PerformerNameAndAddress fromString(String edifactString) {
        if (!edifactString.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException(
                "Can't create " + PerformerNameAndAddress.class.getSimpleName() + " from " + edifactString
            );
        }

        String[] keySplit = Split.byPlus(edifactString);
        String[] colonSplit = Split.byColon(keySplit[2]);
        String identifier = colonSplit[0];

        final boolean isOrganization = StringUtils.isBlank(identifier);
        if (isOrganization) {
            // if identifier is blank - organization/department
            String organizationName = keySplit[4].replaceAll("\\?'", "'");

            return PerformerNameAndAddress.builder()
                .organizationName(organizationName)
                .build();
        }

        String code = colonSplit[1];
        String practitionerName = keySplit[4];

        return PerformerNameAndAddress.builder()
            .identifier(identifier)
            .code(HealthcareRegistrationIdentificationCode.fromCode(code))
            .practitionerName(practitionerName)
            .build();
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() throws EdifactValidationException {
        if (StringUtils.isBlank(identifier) && StringUtils.isBlank(organizationName)) {
            throw new EdifactValidationException(getKey() + ": Attribute organizationName is required");
        } else {
            if (code == null || StringUtils.isBlank(code.getCode())) {
                throw new EdifactValidationException(getKey() + ": Attribute code is required");
            }
            if (StringUtils.isBlank(practitionerName)) {
                throw new EdifactValidationException(getKey() + ": Attribute practitionerName is required");
            }
        }
    }
}
