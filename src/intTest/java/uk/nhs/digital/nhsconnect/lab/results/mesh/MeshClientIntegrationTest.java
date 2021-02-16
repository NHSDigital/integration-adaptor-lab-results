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
import uk.nhs.digital.nhsconnect.lab.results.mesh.exception.MeshApiConnectionException;
import uk.nhs.digital.nhsconnect.lab.results.mesh.exception.MeshWorkflowUnknownException;
import uk.nhs.digital.nhsconnect.lab.results.mesh.http.MeshHttpClientBuilder;
import uk.nhs.digital.nhsconnect.lab.results.mesh.http.MeshRequests;
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

    private static final int MB_100 = 100_000_000;
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
    void when_callingMeshSendMessageEndpoint_expect_messageIdIsReturned() {
        final MeshMessageId meshMessageId = getMeshClient().sendEdifactMessage(OUTBOUND_MESH_MESSAGE);
        assertThat(meshMessageId).isNotNull();
        assertThat(meshMessageId.getMessageID()).isNotEmpty();
    }

    @Test
    void when_callingMeshGetMessageEndpoint_expect_messageIsReturned() {
        final MeshMessageId testMessageId = getMeshClient().sendEdifactMessage(OUTBOUND_MESH_MESSAGE);

        final var inboundMeshMessage = getLabResultsMeshClient().getEdifactMessage(testMessageId.getMessageID());
        assertThat(inboundMeshMessage.getContent()).isEqualTo(CONTENT);
        assertThat(inboundMeshMessage.getWorkflowId()).isEqualTo(WorkflowId.REGISTRATION);
    }

    @Test
    void when_callingGetMessageWithLargeContentAndWrongWorkflowId_expect_meshWorkflowUnknownExceptionIsThrown() {
        final MeshMessageId testMessageId = sendLargeMessageWithWrongWorkflowId();

        assertThatThrownBy(() -> getLabResultsMeshClient().getEdifactMessage(testMessageId.getMessageID()))
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
            request.setEntity(new StringEntity("a".repeat(MB_100))); // 100mb
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
    void when_callingMeshAcknowledgeEndpoint_expect_noExceptionIsThrown() {
        final MeshMessageId testMessageId = getMeshClient().sendEdifactMessage(OUTBOUND_MESH_MESSAGE);

        assertThatCode(() -> getLabResultsMeshClient().acknowledgeMessage(testMessageId.getMessageID()))
            .doesNotThrowAnyException();
    }

    @Test
    void when_pollingFromMesh_expect_emptyListIsReturned() {
        assertThat(getMeshClient().getInboxMessageIds()).isEqualTo(List.of());
    }

    @Test
    void when_pollingFromMeshAfterSendingMsg_expect_listWithMsgIdIsReturned() {
        final MeshMessageId testMessageId = getMeshClient().sendEdifactMessage(OUTBOUND_MESH_MESSAGE);

        assertThat(getLabResultsMeshClient().getInboxMessageIds()).contains(testMessageId.getMessageID());
    }

    @Test
    void when_authenticating_expect_noExceptionThrown() {
        assertThatCode(() -> getMeshClient().authenticate()).doesNotThrowAnyException();
    }

    @Test
    void when_downloadMessageThatDoesNotExist_expect_throwException() {
        assertThatExceptionOfType(MeshApiConnectionException.class).isThrownBy(
            () -> getMeshClient().getEdifactMessage("thisisaninvalidmessageid1234567890")
        );
    }

    @Test
    void when_downloadMessageThatIsGone_expect_throwException() {
        final MeshMessageId testMessageId = getMeshClient().sendEdifactMessage(OUTBOUND_MESH_MESSAGE);
        final var messageId = testMessageId.getMessageID();
        getLabResultsMeshClient().acknowledgeMessage(messageId);

        assertThatExceptionOfType(MeshApiConnectionException.class).isThrownBy(
            () -> getMeshClient().getEdifactMessage(messageId)
        );
    }
}
