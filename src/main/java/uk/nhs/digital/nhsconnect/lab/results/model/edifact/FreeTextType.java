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

    public String getKeyQualifier() {
        return FreeTextSegment.KEY
            + Segment.PLUS_SEPARATOR
            + qualifier;
    }

    public static FreeTextType fromCode(final String code) {
        return Arrays.stream(FreeTextType.values())
            .filter(c -> c.qualifier.equals(code))
            .findFirst()
            .orElse(null);
    }
}
