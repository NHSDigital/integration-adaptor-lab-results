package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum HealthcareRegistrationIdentificationCode {
    GP("900", "National GP"),
    GP_PRACTICE("901", "National GP Practice"),
    CONSULTANT("902", "National Consultant");

    private final String code;
    private final String description;

    public static HealthcareRegistrationIdentificationCode fromCode(@NonNull String code) {
        return Arrays.stream(HealthcareRegistrationIdentificationCode.values())
            .filter(c -> code.equals(c.getCode()))
            .findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException("No HealthcareRegistrationIdentificationCode for '" + code + "'"));
    }
}
