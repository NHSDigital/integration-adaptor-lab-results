package uk.nhs.digital.nhsconnect.lab.results.mesh;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.digital.nhsconnect.lab.results.IntegrationBaseTest;
import uk.nhs.digital.nhsconnect.lab.results.IntegrationTestsExtension;
import uk.nhs.digital.nhsconnect.lab.results.mesh.exception.MeshWorkflowUnknownException;
import uk.nhs.digital.nhsconnect.lab.results.mesh.exception.MeshApiConnectionException;
import uk.nhs.digital.nhsconnect.lab.results.mesh.http.MeshHttpClientBuilder;
import uk.nhs.digital.nhsconnect.lab.results.mesh.http.MeshRequests;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.InboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.MeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.MeshMessageId;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.OutboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith({SpringExtension.class, IntegrationTestsExtension.class})
@SpringBootTest
@Slf4j
@DirtiesContext
class MeshClientIntegrationTest extends IntegrationBaseTest {

    private static final String RECIPIENT = "XX11";
    private static final String CONTENT = "test_message";
    private static final OutboundMeshMessage OUTBOUND_MESH_MESSAGE = OutboundMeshMessage.create(
        RECIPIENT, WorkflowId.REGISTRATION, CONTENT, null, null
    );

    @Autowired
    private MeshRequests meshRequests;
    @Autowired
    private RecipientMailboxIdMappings recipientMailboxIdMappings;
    @Autowired
    private MeshHttpClientBuilder meshHttpClientBuilder;

    @BeforeEach
    void beforeEach() {
        clearMeshMailboxes();
    }

    @Test
    void when_CallingMeshSendMessageEndpoint_Then_MessageIdIsReturned() {
        MeshMessageId meshMessageId = meshClient.sendEdifactMessage(OUTBOUND_MESH_MESSAGE);
        assertThat(meshMessageId).isNotNull();
        assertThat(meshMessageId.getMessageID()).isNotEmpty();
    }

    @Test
    void when_CallingMeshGetMessageEndpoint_Then_MessageIsReturned() {
        MeshMessageId testMessageId = meshClient.sendEdifactMessage(OUTBOUND_MESH_MESSAGE);

        InboundMeshMessage meshMessage = labResultsMeshClient.getEdifactMessage(testMessageId.getMessageID());
        assertThat(meshMessage.getContent()).isEqualTo(CONTENT);
        assertThat(meshMessage.getWorkflowId()).isEqualTo(WorkflowId.REGISTRATION);
    }

    @Test
    void when_CallingGetMessageWithLargeContentAndWrongWorkflowId_Then_MeshWorkflowUnknownExceptionIsThrown() {
        MeshMessageId testMessageId = sendLargeMessageWithWrongWorkflowId();

        assertThatThrownBy(() -> labResultsMeshClient.getEdifactMessage(testMessageId.getMessageID()))
            .isInstanceOf(MeshWorkflowUnknownException.class)
            .hasMessageContaining("NOT_LAB_RESULTS");
    }

    @SneakyThrows
    private MeshMessageId sendLargeMessageWithWrongWorkflowId() {
        OutboundMeshMessage messageForMappingMailboxId = new MeshMessage().setHaTradingPartnerCode("XX11");
        var recipientMailbox = recipientMailboxIdMappings.getRecipientMailboxId(messageForMappingMailboxId);

        try (CloseableHttpClient client = meshHttpClientBuilder.build()) {
            var request = meshRequests.sendMessage(recipientMailbox, WorkflowId.REGISTRATION);
            request.removeHeaders("Mex-WorkflowID");
            request.setHeader("Mex-WorkflowID", "NOT_LAB_RESULTS");
            request.setEntity(new StringEntity("a".repeat(100000000))); // 100mb
            try (CloseableHttpResponse response = client.execute(request)) {
                assertThat(response.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.ACCEPTED.value());
                return parseInto(MeshMessageId.class, response);
            }
        }
    }

    private <T> T parseInto(Class<T> clazz, CloseableHttpResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonParser parser = objectMapper.reader().createParser(EntityUtils.toString(response.getEntity()));
        return objectMapper.readValue(parser, clazz);
    }

    @Test
    void when_CallingMeshAcknowledgeEndpoint_Then_NoExceptionIsThrown() {
        MeshMessageId testMessageId = meshClient.sendEdifactMessage(OUTBOUND_MESH_MESSAGE);

        assertThatCode(() -> labResultsMeshClient.acknowledgeMessage(testMessageId.getMessageID()))
            .doesNotThrowAnyException();
    }

    @Test
    void when_PollingFromMesh_Then_EmptyListIsReturned() {
        assertThat(meshClient.getInboxMessageIds()).isEqualTo(List.of());
    }

    @Test
    void when_PollingFromMeshAfterSendingMsg_Then_ListWithMsgIdIsReturned() {
        MeshMessageId testMessageId = meshClient.sendEdifactMessage(OUTBOUND_MESH_MESSAGE);

        assertThat(labResultsMeshClient.getInboxMessageIds()).contains(testMessageId.getMessageID());
    }

    @Test
    void when_Authenticating_Then_NoExceptionThrown() {
        assertThatCode(() -> meshClient.authenticate()).doesNotThrowAnyException();
    }

    @Test
    void when_downloadMessageThatDoesNotExist_then_throwException() {
        assertThatExceptionOfType(MeshApiConnectionException.class).isThrownBy(
            () -> meshClient.getEdifactMessage("thisisaninvalidmessageid1234567890")
        );
    }

    @Test
    void when_downloadMessageThatIsGone_then_throwException() {
        MeshMessageId testMessageId = meshClient.sendEdifactMessage(OUTBOUND_MESH_MESSAGE);
        var messageId = testMessageId.getMessageID();
        labResultsMeshClient.acknowledgeMessage(messageId);

        assertThatExceptionOfType(MeshApiConnectionException.class).isThrownBy(
            () -> meshClient.getEdifactMessage(messageId)
        );
    }
}
