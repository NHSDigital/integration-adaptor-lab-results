package uk.nhs.digital.nhsconnect.lab.results.utils;

import lombok.NonNull;
import org.hl7.fhir.dstu3.model.Resource;
import org.springframework.stereotype.Component;

@Component
public class ResourceFullUrlGenerator {
    private static final String FULL_URL_PREFIX = "urn:uuid:";

    public String generateFullUrl(@NonNull final Resource resource) {
        return FULL_URL_PREFIX + resource.getId();
    }
}
