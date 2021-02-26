package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * Example: NAD+PO+G3380314:900++SCOTT'
 * <br>
 * Example: NAD+PO+++NORTH DOWN GP'
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
    private final HealthcareRegistrationIdentificationCode healthcareRegistrationIdentificationCode;
    private final String requesterName;
    private final String requestingOrganizationName;

    @SuppressWarnings("checkstyle:MagicNumber")
    public static RequesterNameAndAddress fromString(final String edifactString) {
        if (!edifactString.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException("Can't create " + RequesterNameAndAddress.class.getSimpleName()
                + " from " + edifactString);
        }

        String[] keySplit = Split.byPlus(edifactString);
        String identifier = Split.byColon(keySplit[2])[0];
        String name = keySplit[4];

        if (identifier.isBlank()) {
            return RequesterNameAndAddress.builder()
                .requestingOrganizationName(name)
                .build();
        }

        String code = Split.byColon(keySplit[2])[1];

        return RequesterNameAndAddress.builder()
            .identifier(identifier)
            .healthcareRegistrationIdentificationCode(HealthcareRegistrationIdentificationCode.fromCode(code))
            .requesterName(name)
            .build();
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() throws EdifactValidationException {
        if (!StringUtils.isBlank(requesterName) && StringUtils.isBlank(identifier)) {
            throw new EdifactValidationException(KEY + ": Attribute identifier is required");
        }

        if (!StringUtils.isBlank(identifier) && healthcareRegistrationIdentificationCode == null) {
            throw new EdifactValidationException(
                KEY + ": Attribute healthcareRegistrationIdentificationCode is required");
        }

        if (!StringUtils.isBlank(identifier) && StringUtils.isBlank(requesterName)) {
            throw new EdifactValidationException(KEY + ": Attribute requesterName is required");
        }

        if (StringUtils.isBlank(identifier) && StringUtils.isBlank(requestingOrganizationName)) {
            throw new EdifactValidationException(KEY + ": Attribute requestingOrganizationName is required");
        }
    }
}
