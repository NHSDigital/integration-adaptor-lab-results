package uk.nhs.digital.nhsconnect.lab.results.inbound;

import lombok.NonNull;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ChecksumService {
    private static final String DELIMITER = "-";
    public String createChecksum(
            @NonNull String sender,
            @NonNull String recipient,
            @NonNull Long interchangeSequenceNumber,
            @NonNull Long messageSequenceNumber) {

        var data = Stream.of(sender, recipient, interchangeSequenceNumber, messageSequenceNumber)
            .map(String::valueOf)
            .collect(Collectors.joining(DELIMITER));

        return DigestUtils.md5Hex(data).toUpperCase();
    }
}
