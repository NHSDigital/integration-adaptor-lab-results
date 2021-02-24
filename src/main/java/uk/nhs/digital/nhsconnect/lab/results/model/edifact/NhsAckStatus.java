package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum NhsAckStatus {
    ACCEPTED("IAF"),
    PARTIALLY_ACCEPTED("IAP"),
    INTERCHANGE_REJECTED("IRI"),
    MESSAGE_REJECTED("IRM"),
    ALL_MESSAGES_REJECTED("IRA");

    private final String nhsAckStatus;

    @Override
    public String toString() {
        return nhsAckStatus;
    }
}
