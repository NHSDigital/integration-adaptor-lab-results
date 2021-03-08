package uk.nhs.digital.nhsconnect.lab.results.inbound;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class ChecksumServiceTest {

    private static final String SENDER = "some_sender";
    private static final String RECIPIENT = "some_recipient";
    private static final long INTERCHANGE_SEQUENCE_NUMBER = 123;
    private static final long MESSAGE_SEQUENCE_NUMBER = 234;
    private static final String CHECKSUM = "2D398477EC8F0F7B9FB2B5D8E3D56918";

    private final ChecksumService checksumService = new ChecksumService();

    @Test
    void checksumIsCreated() {
        var actualChecksum = checksumService
            .createChecksum(SENDER, RECIPIENT, INTERCHANGE_SEQUENCE_NUMBER, MESSAGE_SEQUENCE_NUMBER);

        assertThat(actualChecksum).isEqualTo(CHECKSUM);
    }

    @Test
    void allArgumentsAreRequired() {
        assertAll(
            () -> assertThatThrownBy(() -> checksumService
                .createChecksum(null, RECIPIENT, INTERCHANGE_SEQUENCE_NUMBER, MESSAGE_SEQUENCE_NUMBER))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("sender is marked non-null but is null"),
            () -> assertThatThrownBy(() -> checksumService
                .createChecksum(SENDER, null, INTERCHANGE_SEQUENCE_NUMBER, MESSAGE_SEQUENCE_NUMBER))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("recipient is marked non-null but is null"),
            () -> assertThatThrownBy(() -> checksumService
                .createChecksum(SENDER, RECIPIENT, null, MESSAGE_SEQUENCE_NUMBER))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("interchangeSequenceNumber is marked non-null but is null"),
            () -> assertThatThrownBy(() -> checksumService
                .createChecksum(SENDER, RECIPIENT, INTERCHANGE_SEQUENCE_NUMBER, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("messageSequenceNumber is marked non-null but is null")
        );
    }
}
