package uk.nhs.digital.nhsconnect.lab.results.utils;

import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Resource;
import org.springframework.stereotype.Component;

@Component
public class ResourceFullUrlGenerator {
    private static final String FULL_URL_PREFIX = "urn:uuid:";

    public String generate(@NonNull final Resource resource) {
        final var resourceId = resource.getId();
        if (StringUtils.isBlank(resourceId)) {
            throw new IllegalArgumentException("resource.id must be present and non-blank");
        }
        return FULL_URL_PREFIX + resourceId;
    }
}
