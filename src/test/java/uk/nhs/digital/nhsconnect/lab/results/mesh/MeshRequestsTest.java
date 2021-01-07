package uk.nhs.digital.nhsconnect.lab.results.mesh;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.mesh.http.MeshConfig;
import uk.nhs.digital.nhsconnect.lab.results.mesh.http.MeshHeaders;
import uk.nhs.digital.nhsconnect.lab.results.mesh.http.MeshRequests;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;

import static org.assertj.core.api.Assertions.assertThat;

class MeshRequestsTest {

    private final MeshConfig meshConfig = new MeshConfig("mailboxId",
        "password",
        "SharedKey",
        "https://localhost:8829/messageexchange/",
        "false",
        StringUtils.EMPTY,
        StringUtils.EMPTY,
        StringUtils.EMPTY);
    private final MeshHeaders meshHeaders = new MeshHeaders(meshConfig);

    @Test
    void when_GettingMessage_Then_ExpectHttpGetAndCorrectUri() {
        MeshRequests meshRequests = new MeshRequests(meshConfig, meshHeaders);

        var request = meshRequests.getMessage("messageId");

        assertThat(request).isExactlyInstanceOf(HttpGet.class);
        assertThat(request.getURI().toString()).isEqualTo("https://localhost:8829/messageexchange/mailboxId/inbox/messageId");
    }

    @Test
    void when_SendingRegistrationMessage_Then_ExpectHttpPostAndCorrectUri() {
        MeshRequests meshRequests = new MeshRequests(meshConfig, meshHeaders);

        String recipient = "recipient";
        var request = meshRequests.sendMessage(recipient, WorkflowId.REGISTRATION);

        assertSending(request, recipient, WorkflowId.REGISTRATION);
    }

    @Test
    void when_SendingRecepMessage_Then_ExpectHttpPostAndCorrectUri() {
        MeshRequests meshRequests = new MeshRequests(meshConfig, meshHeaders);

        String recipient = "recipient";
        var request = meshRequests.sendMessage(recipient, WorkflowId.RECEP);

        assertSending(request, recipient, WorkflowId.RECEP);
    }

    @Test
    void when_GettingMessageIds_Then_ExpectHttpGetAndCorrectUri() {
        MeshRequests meshRequests = new MeshRequests(meshConfig, meshHeaders);

        var request = meshRequests.getMessageIds();

        assertThat(request).isExactlyInstanceOf(HttpGet.class);
        assertThat(request.getURI().toString()).isEqualTo("https://localhost:8829/messageexchange/mailboxId/inbox");
    }

    @Test
    void when_AcknowledgeMessage_Then_ExpectHttpPutAndCorrectUri() {
        MeshRequests meshRequests = new MeshRequests(meshConfig, meshHeaders);

        var request = meshRequests.acknowledge("messageId");

        assertThat(request).isExactlyInstanceOf(HttpPut.class);
        assertThat(request.getURI().toString()).isEqualTo("https://localhost:8829/messageexchange/mailboxId/inbox/messageId/status/acknowledged");
    }

    @Test
    void when_Authenticate_Then_ExpectHttpPostAndCorrectUri() {
        MeshRequests meshRequests = new MeshRequests(meshConfig, meshHeaders);

        var request = meshRequests.authenticate();

        assertThat(request).isExactlyInstanceOf(HttpPost.class);
        assertThat(request.getURI().toString()).isEqualTo("https://localhost:8829/messageexchange/mailboxId");
    }

    private void assertSending(HttpEntityEnclosingRequestBase request, String recipient, WorkflowId workflowId) {
        assertThat(request).isExactlyInstanceOf(HttpPost.class);
        assertThat(request.getURI().toString()).isEqualTo("https://localhost:8829/messageexchange/mailboxId/outbox");
        Header[] mexToHeader = request.getHeaders("Mex-To");
        assertThat(mexToHeader.length).isEqualTo(1);
        assertThat(mexToHeader[0].getValue()).isEqualTo(recipient);
        assertThat(request.getHeaders("Mex-WorkflowID")[0].getValue()).isEqualTo(workflowId.getWorkflowId());
    }
}