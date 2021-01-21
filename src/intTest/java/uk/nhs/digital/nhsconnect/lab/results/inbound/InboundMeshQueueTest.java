package uk.nhs.digital.nhsconnect.lab.results.inbound;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import uk.nhs.digital.nhsconnect.lab.results.IntegrationBaseTest;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.MeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Inbound;
import uk.nhs.digital.nhsconnect.lab.results.utils.TimestampService;

import javax.jms.JMSException;
import javax.jms.Message;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZonedDateTime;

import static org.mockito.Mockito.when;

/**
 * Tests the processing of a REGISTRATION interchange by publishing it onto the inbound MESH message queue. This bypasses the
 * MESH polling loop / MESH Client / MESH API.
 */
@DirtiesContext
public class InboundMeshQueueTest extends IntegrationBaseTest {

    private static final Inbound TRANSACTION_TYPE = Inbound.APPROVAL;
    private static final Instant GENERATED_TIMESTAMP = ZonedDateTime.of(2020, 6, 10, 14,
            38, 0, 0, TimestampService.UK_ZONE)
            .toInstant();
    private static final String ISO_GENERATED_TIMESTAMP = new TimestampService().formatInISO(GENERATED_TIMESTAMP);

    @MockBean
    private TimestampService timestampService;

    @BeforeEach
    void setUp() {
        when(timestampService.getCurrentTimestamp()).thenReturn(GENERATED_TIMESTAMP);
        when(timestampService.formatInISO(GENERATED_TIMESTAMP)).thenReturn(ISO_GENERATED_TIMESTAMP);
        clearGpOutboundQueue();
        clearMeshMailboxes();
    }

    @Test
    void whenMeshInboundQueueMessageIsReceivedThenMessageIsHandled(SoftAssertions softly) throws IOException, JMSException {
        final MeshMessage meshMessage = new MeshMessage()
                .setWorkflowId(WorkflowId.REGISTRATION)
                .setContent(new String(Files.readAllBytes(edifactResource.getFile().toPath())))
                .setMeshMessageId("12345");

        sendToMeshInboundQueue(meshMessage);

        final Message message = getGpOutboundQueueMessage();
        final String content = parseTextMessage(message);
        final String expectedContent = new String(Files.readAllBytes(fhirResource.getFile().toPath()));

        softly.assertThat(message.getStringProperty("TransactionType")).isEqualTo(TRANSACTION_TYPE.name().toLowerCase());
        softly.assertThat(content).isEqualTo(expectedContent);
    }
}
