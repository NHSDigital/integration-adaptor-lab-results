package uk.nhs.digital.nhsconnect.lab.results.mesh.token;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.mesh.http.MeshConfig;

import java.time.Instant;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationHashGeneratorTest {

    @Mock
    private MeshConfig meshConfig;

    private static final String MAILBOX_ID = "mailbox_id";
    private static final String MAILBOX_PASSWORD = "mailbox_password";
    private static final String SHARED_KEY = "shared_key";

    private static final Instant FIXED_TIME_LOCAL = ZonedDateTime.parse("1991-11-06T12:30:00.000Z").toInstant();
    private static final String UUID = "73eefd69-811f-44d0-81f8-a54ff352a991";

    @BeforeEach
    void setUp() {
        when(meshConfig.getMailboxId()).thenReturn(MAILBOX_ID);
        when(meshConfig.getMailboxPassword()).thenReturn(MAILBOX_PASSWORD);
        when(meshConfig.getSharedKey()).thenReturn(SHARED_KEY);
    }

    @Test
    void testCorrectHashGenerated() {
        final AuthorizationHashGenerator authorizationHashGenerator = new AuthorizationHashGenerator();

        final Nonce nonce = new Nonce(UUID);

        final String timestamp = new TokenTimestamp(FIXED_TIME_LOCAL).getValue();
        final String hash = authorizationHashGenerator.computeHash(meshConfig, nonce, timestamp);
        assertThat(hash).isEqualTo("e80adf34b261ba9a377ac4776e1354f2ce814c5f6ecec71b2ce540b94836c530");
    }

}
