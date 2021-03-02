package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ReportStatusCode {
    UNSPECIFIED("UN", "Unspecified");

    private final String code;
    private final String description;

    public static ReportStatusCode fromCode(@NonNull String code) {
        return Arrays.stream(ReportStatusCode.values())
                .filter(c -> code.equals(c.getCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No Report Status Code for '" + code + "'"));
    }

}
