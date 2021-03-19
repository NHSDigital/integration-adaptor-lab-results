package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.HealthcareRegistrationIdentificationCode;

import java.util.Optional;

/**
 * Example: Details of requesting practitioner with identifier
 * <pre>
 * NAD+PO+G3380314:900++SCOTT'
 * SPR+PRO'
 * </pre>
 * Example: Details of requesting practitioner without identifier
 * <pre>
 * NAD+PO+++SCOTT'
 * SPR+PRO'
 * </pre>
 * Example: Details of requesting organization
 * <pre>
 * NAD+PO+++NORTH DOWN GP'
 * SPR+ORG'
 * </pre>
 */
@Setter
@Builder
@RequiredArgsConstructor
public class RequesterNameAndAddress extends Segment {

    private static final String KEY = "NAD";
    private static final String QUALIFIER = "PO";
    public static final String KEY_QUALIFIER = KEY + PLUS_SEPARATOR + QUALIFIER;

    private final String identifier;
    private final HealthcareRegistrationIdentificationCode code;
    private final String name;

    @SuppressWarnings("checkstyle:MagicNumber")
    public static RequesterNameAndAddress fromString(final String edifactString) {
        if (!edifactString.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException("Can't create " + RequesterNameAndAddress.class.getSimpleName()
                + " from " + edifactString);
        }

        final String[] keySplit = Split.byPlus(edifactString);
        final String[] colonSplit = Split.byColon(keySplit[2]);

        final var builder = RequesterNameAndAddress.builder();
        arrayGetSafe(colonSplit, 0)
            .ifPresent(builder::identifier);
        arrayGetSafe(colonSplit, 1)
            .map(HealthcareRegistrationIdentificationCode::fromCode)
            .ifPresent(builder::code);
        arrayGetSafe(keySplit, 4)
            .ifPresent(builder::name);

        return builder.build();
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public Optional<String> getIdentifier() {
        return Optional.ofNullable(identifier);
    }

    public Optional<HealthcareRegistrationIdentificationCode> getCode() {
        return Optional.ofNullable(code);
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    @Override
    public void validate() throws EdifactValidationException {
        if (StringUtils.isBlank(name)) {
            throw new EdifactValidationException(KEY + ": Attribute name is required");
        }
    }
}
