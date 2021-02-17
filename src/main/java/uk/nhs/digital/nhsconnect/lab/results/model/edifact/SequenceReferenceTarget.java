package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum SequenceReferenceTarget {
    INVESTIGATION("ARL"),
    SPECIMEN("ASL");

    @Getter
    private final String qualifier;

    public static SequenceReferenceTarget fromCode(final String code) {
        return Arrays.stream(SequenceReferenceTarget.values())
            .filter(sequenceReferenceTarget -> sequenceReferenceTarget.qualifier.equals(code))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No sequenceReferenceTarget qualifier for '" + code + "'"));
    }
}
