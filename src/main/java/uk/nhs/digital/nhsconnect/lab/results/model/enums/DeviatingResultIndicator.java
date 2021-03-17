package uk.nhs.digital.nhsconnect.lab.results.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum DeviatingResultIndicator {
    ABOVE_HIGH_REFERENCE_LIMIT("HI", "Above high reference limit"),
    BELOW_LOW_REFERENCE_LIMIT("LO", "Below low reference limit"),
    OUTSIDE_REFERENCE_LIMIT("OR", "Outside reference range"),
    POTENTIALLY_ABNORMAL("PA", "Potentially abnormal");

    private final String code;
    private final String description;

    public static DeviatingResultIndicator fromCode(String code) {
        return Arrays.stream(DeviatingResultIndicator.values())
            .filter(c -> c.code.equals(code))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No deviating result indicator for '" + code + "'"));
    }
}
