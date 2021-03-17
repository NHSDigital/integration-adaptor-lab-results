package uk.nhs.digital.nhsconnect.lab.results.model.enums;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ServiceProviderCode {
    DEPARTMENT("DPT", "Department (within an organization)"),
    ORGANIZATION("ORG", "Healthcare organization"),
    PROFESSIONAL("PRO", "Healthcare professional");

    private final String code;
    private final String description;

    public static ServiceProviderCode fromCode(@NonNull String code) {
        return Arrays.stream(ServiceProviderCode.values())
            .filter(c -> code.equals(c.getCode()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No service provider code for '" + code + "'"));
    }
}
