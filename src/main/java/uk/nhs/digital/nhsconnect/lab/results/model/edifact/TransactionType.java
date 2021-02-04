package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum TransactionType {

    APPROVAL("F4", "APF");

    private final String code;
    private final String abbreviation;

    static TransactionType fromCode(final String code) {
        return Arrays.stream(TransactionType.values())
            .filter(transactionType -> transactionType.getCode().equals(code))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }

}
