package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;

import java.util.Arrays;

public enum DateFormat {
    CCYYMMDD("102"),
    CCYYMMDDHHMM("203"),
    CCYY("602"),
    CCYYMM("610");

    @Getter
    private final String code;

    DateFormat(String code) {
        this.code = code;
    }

    public static DateFormat fromCode(final String code) {
        return Arrays.stream(DateFormat.values())
                .filter(dateFormat -> dateFormat.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No dateFormat name for '" + code + "'"));
    }
}
