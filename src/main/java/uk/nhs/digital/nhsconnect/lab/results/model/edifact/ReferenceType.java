package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum ReferenceType {
    DIAGNOSTIC_REPORT("SRI"),
    INVESTIGATION("ARL"),
    PARTNER_AGREED_ID("AHI"),
    SERVICE_SUBJECT("SSI"),
    SPECIMEN("ASL"),
    SPECIMEN_BY_PROVIDER("STI"),
    SPECIMEN_BY_REQUESTER("RTI");

    @Getter
    private final String qualifier;

    public static ReferenceType fromCode(final String code) {
        return Arrays.stream(ReferenceType.values())
            .filter(referenceType -> referenceType.qualifier.equals(code))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No reference qualifier for '" + code + "'"));
    }
}
