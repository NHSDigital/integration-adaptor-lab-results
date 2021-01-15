package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Outbound implements TransactionType {

    // TO-DO
    ;

    @Override
    public String getCode() {
        return null;
    }
}