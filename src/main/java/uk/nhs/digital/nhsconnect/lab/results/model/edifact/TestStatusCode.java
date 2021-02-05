package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum TestStatusCode {
    REGISTERED("RE", "Registered"),
    PRELIMINARY("PR", "Preliminary"),
    FINAL("FI", "Final"),
    AMENDED("AM", "Amended"),
    CORRECTED("CO", "Corrected"),
    CANCELLED("CA", "Cancelled"),
    ENTERED_IN_ERROR("EN", "Entered in Error"),
    UNKNOWN("UN", "Unknown");

    private final String code;
    private final String description;

    public static TestStatusCode fromCode(@NonNull String code) {
        return Arrays.stream(TestStatusCode.values())
                .filter(c -> code.equals(c.getCode()))
                .findFirst()
                .orElseThrow();
    }
}
