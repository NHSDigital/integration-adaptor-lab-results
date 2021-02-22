package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum FreeTextType {
    CLINICAL_INFO("CID"),
    COMPLEX_REFERENCE_RANGE("CRR"),
    INVESTIGATION_RESULT("RIT"),
    REFERENCE_POPULATION_DEFINITION("RPD"),
    SERVICE_PROVIDER_COMMENT("SPC");

    private final String qualifier;

    public static FreeTextType fromCode(final String code) {
        return Arrays.stream(FreeTextType.values())
            .filter(c -> c.qualifier.equals(code))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No free text type for '" + code + "'"));
    }
}
