package uk.nhs.digital.nhsconnect.lab.results.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.hl7.fhir.dstu3.model.Coding;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum DeviatingResultIndicator {
    ABOVE_HIGH_REFERENCE_LIMIT("HI", "H", "High"),
    BELOW_LOW_REFERENCE_LIMIT("LO", "L", "Low"),
    OUTSIDE_REFERENCE_LIMIT("OR", "OR", "Outside reference range"),
    POTENTIALLY_ABNORMAL("PA", "PA", "Potentially abnormal");

    private static final String STANDARD_INTERPRETATION_CODE_SYSTEM = "http://hl7.org/fhir/v2/0078";
    private static final String EXTENDED_INTERPRETATION_CODE_SYSTEM = "https://fhir.hl7.org.uk/STU3/CodeSystem/CareConnect-ExtendedInterpretationCode-1";

    private final String edifactCode;
    private final String fhirCode;
    private final String description;

    public static DeviatingResultIndicator fromEdifactCode(String edifactCode) {
        return Arrays.stream(DeviatingResultIndicator.values())
            .filter(c -> c.edifactCode.equals(edifactCode))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No deviating result indicator for '" + edifactCode + "'"));
    }

    public Coding toCoding() {
        return new Coding()
            .setDisplay(this.getDescription())
            .setCode(this.getFhirCode())
            .setSystem(getSystem(this));
    }

    private String getSystem(DeviatingResultIndicator deviatingResultIndicator) {
        switch (deviatingResultIndicator) {
            case ABOVE_HIGH_REFERENCE_LIMIT:
            case BELOW_LOW_REFERENCE_LIMIT:
                return STANDARD_INTERPRETATION_CODE_SYSTEM;
            case OUTSIDE_REFERENCE_LIMIT:
            case POTENTIALLY_ABNORMAL:
                return EXTENDED_INTERPRETATION_CODE_SYSTEM;
            default:
                throw new NotImplementedException();
        }
    }
}
