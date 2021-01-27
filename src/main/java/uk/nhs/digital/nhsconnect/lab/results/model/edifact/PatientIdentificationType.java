package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;

import java.util.Arrays;
import java.util.NoSuchElementException;

public enum PatientIdentificationType {
    OFFICIAL_PATIENT_IDENTIFICATION("OPI");

    @Getter
    private final String code;

    PatientIdentificationType(String code) {
        this.code = code;
    }

    public static PatientIdentificationType fromCode(final String code) {
        return Arrays.stream(PatientIdentificationType.values())
            .filter(patientIdentificationType -> patientIdentificationType.getCode().equals(code))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException(String.format("%s element not found", code)));
    }
}
