package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Inbound implements TransactionType {
    STUB("123", "ABC");

    private final String code;
    private final String abbreviation;
}