package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.ServiceProviderCode;

/**
 * Example: SPR+ORG'
 */
@Getter
@Setter
@Builder
@RequiredArgsConstructor
public class ServiceProvider extends Segment {

    public static final String KEY = "SPR";

    @NonNull
    private final ServiceProviderCode serviceProviderCode;

    public static ServiceProvider fromString(final String edifactString) {
        if (!edifactString.startsWith(KEY)) {
            throw new IllegalArgumentException("Can't create " + ServiceProvider.class.getSimpleName()
                + " from " + edifactString);
        }

        final String[] keySplit = Split.byPlus(edifactString);
        final String code = keySplit[1];

        return new ServiceProvider(ServiceProviderCode.fromCode(code));
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validate() throws EdifactValidationException {
        if (serviceProviderCode.getCode().isBlank()) {
            throw new EdifactValidationException(KEY + ": Attribute code in serviceProviderCode is required");
        }
    }
}
