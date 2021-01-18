package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Inbound implements TransactionType {
    APPROVAL("F4", "APF");

    private final String code;
    private final String abbreviation;
}