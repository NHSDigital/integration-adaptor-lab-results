package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;

/**
 * Example NAD+FHS+XX1:954'
 */
@Getter
@Setter
@RequiredArgsConstructor
public class HealthAuthorityNameAndAddress extends Segment {
    public static final String KEY = "NAD";
    public static final String QUALIFIER = "FHS";
    public static final String KEY_QUALIFIER = KEY + "+" + QUALIFIER;

    @NonNull
    private String identifier;

    @NonNull
    private String code;

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        return QUALIFIER + "+" + identifier + ":" + code;
    }

    @Override
    protected void validateStateful() {
        // Do nothing
    }

    @Override
    public void preValidate() throws EdifactValidationException {
        if (!StringUtils.hasText(identifier)) {
            throw new EdifactValidationException(getKey() + ": Attribute identifier is required");
        }
        if (!StringUtils.hasText(code)) {
            throw new EdifactValidationException(getKey() + ": Attribute code is required");
        }
    }

    public static HealthAuthorityNameAndAddress fromString(final String edifactString) {
        if (!edifactString.startsWith(KEY_QUALIFIER)) {
            throw new IllegalArgumentException("Can't create " + HealthAuthorityNameAndAddress.class.getSimpleName()
                    + " from " + edifactString);
        }
        final String[] keySplit = Split.byPlus(edifactString);
        final String identifier = Split.byColon(keySplit[2])[0];
        final String code = Split.byColon(keySplit[2])[1];
        return new HealthAuthorityNameAndAddress(identifier, code);
    }
}
