package uk.nhs.digital.nhsconnect.lab.results.model.enums;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum CodingType {
    READ_CODE("911", "5 byte Read (Version 2) code"),
    SNOMED_CT_CODE("921", "SNOMED CT code");

    private final String code;
    private final String description;

    public static CodingType fromCode(@NonNull final String code) {
        return Arrays.stream(CodingType.values())
            .filter(c -> code.equals(c.getCode()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No laboratory investigation code for '" + code + "'"));
    }
}
