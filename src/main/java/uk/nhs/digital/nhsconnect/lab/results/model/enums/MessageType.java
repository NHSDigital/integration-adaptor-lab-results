package uk.nhs.digital.nhsconnect.lab.results.model.enums;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum MessageType {
    NHSACK("NHS001", "NHS Acknowledgement MIG variant number 1"),
    PATHOLOGY("NHS003", "NHS Laboratory Service Report MIG variant number 3"),
    SCREENING("NHS004", "NHS Laboratory Service Report MIG variant number 4");

    private final String code;
    private final String description;

    public static MessageType fromCode(@NonNull String code) {
        return Arrays.stream(MessageType.values())
            .filter(c -> code.equals(c.getCode()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No message type for \"" + code + "\""));
    }
}
