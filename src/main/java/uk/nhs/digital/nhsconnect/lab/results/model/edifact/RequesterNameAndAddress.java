package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * Example: Details of requesting practitioner
 * NAD+PO+G3380314:900++SCOTT'
 * SPR+PRO'
 * <br>
 * Example: Details of requesting organization
 * NAD+PO+++NORTH DOWN GP'
 * SPR+ORG'
 */
@Getter
@Setter
@Builder
@RequiredArgsConstructor
public class RequesterNameAndAddress extends Segment {

    private static final String KEY = "NAD";
    private static final String QUALIFIER = "PO";
    public static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;

    private final String identifier;
    private final HealthcareRegistrationIdentificationCode code;
    private final String practitionerName;
    private final String organizationName;

    @SuppressWarnings("checkstyle:MagicNumber")
    public static RequesterNameAndAddress fromString(final String edifactString) {
        if (!edifactString.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException("Can't create " + RequesterNameAndAddress.class.getSimpleName()
                + " from " + edifactString);
        }

        String[] keySplit = Split.byPlus(edifactString);
        String identifier = Split.byColon(keySplit[2])[0];

        final boolean isOrganization = StringUtils.isBlank(identifier);
        if (isOrganization) {
            // if identifier is blank - organization
            String organizationName = keySplit[4].replaceAll("\\?'", "'");

            return RequesterNameAndAddress.builder()
                .organizationName(organizationName)
                .build();
        }

        String code = Split.byColon(keySplit[2])[1];
        String practitionerName = keySplit[4];

        return RequesterNameAndAddress.builder()
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
        if (StringUtils.isBlank(identifier)) {
            if (!StringUtils.isBlank(practitionerName)) {
                throw new EdifactValidationException(KEY + ": Attribute identifier is required");
            }

            if (StringUtils.isBlank(organizationName)) {
                throw new EdifactValidationException(KEY + ": Attribute organizationName is required");
            }
        } else {
            if (code == null) {
                throw new EdifactValidationException(
                    KEY + ": Attribute code is required");
            }

            if (StringUtils.isBlank(practitionerName)) {
                throw new EdifactValidationException(KEY + ": Attribute practitionerName is required");
            }
        }
    }
}
