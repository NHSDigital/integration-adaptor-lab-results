package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum LaboratoryInvestigationResultType {
    NUMERICAL_VALUE("NV"),
    CODED_VALUE("CV");

    private final String code;

    public static LaboratoryInvestigationResultType fromCode(@NonNull String code) {
        return Arrays.stream(LaboratoryInvestigationResultType.values())
            .filter(c -> code.equals(c.getCode()))
            .findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException("No laboratory investigation result type for \"" + code + "\"")
            );
    }
}
