package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;

import java.util.Arrays;

public enum Gender {
    UNKNOWN("0"),
    MALE("1"),
    FEMALE("2"),
    OTHER("9");

    @Getter
    private final String code;

    Gender(String code) {
        this.code = code;
    }

    public static Gender fromCode(final String code) {
        return Arrays.stream(Gender.values())
                .filter(gender -> gender.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No gender name for '" + code + "'"));
    }
}