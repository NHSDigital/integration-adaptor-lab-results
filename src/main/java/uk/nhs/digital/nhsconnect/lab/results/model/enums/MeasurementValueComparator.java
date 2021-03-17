package uk.nhs.digital.nhsconnect.lab.results.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum MeasurementValueComparator {
    GREATER_THAN_OR_EQUAL_TO("5", ">="),
    GREATER_THAN("6", ">"),
    LESS_THAN("7", "<"),
    LESS_THAN_OR_EQUAL_TO("8", "<=");

    private final String code;
    private final String description;

    public static MeasurementValueComparator fromCode(String code) {
        return Arrays.stream(MeasurementValueComparator.values())
            .filter(c -> c.code.equals(code))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No measurement value comparator for '" + code + "'"));
    }
}
