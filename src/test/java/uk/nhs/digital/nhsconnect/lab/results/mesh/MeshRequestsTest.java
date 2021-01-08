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

    private static final String MESH_HOST = "https://localhost:8829/messageexchange/";
    private static final String MAILBOX_ID = "mailboxId";
    private static final String MESSAGE_ID = "messageId";
    private static final String MESSAGE_RECIPIENT = "recipient";

    private final MeshConfig meshConfig = new MeshConfig(MAILBOX_ID,
        "password",
        "SharedKey",
            MESH_HOST,
        "false",
        StringUtils.EMPTY,
        StringUtils.EMPTY,
        StringUtils.EMPTY);
    private final MeshHeaders meshHeaders = new MeshHeaders(meshConfig);

    @Test
    void when_GettingMessage_Then_ExpectHttpGetAndCorrectUri() {
        MeshRequests meshRequests = new MeshRequests(meshConfig, meshHeaders);

        var request = meshRequests.getMessage(MESSAGE_ID);

        assertThat(request).isExactlyInstanceOf(HttpGet.class);
        assertThat(request.getURI().toString()).isEqualTo(MESH_HOST + MAILBOX_ID + "/inbox/" + MESSAGE_ID);
    }

    @Test
    void when_SendingRegistrationMessage_Then_ExpectHttpPostAndCorrectUri() {
        MeshRequests meshRequests = new MeshRequests(meshConfig, meshHeaders);

        var request = meshRequests.sendMessage(MESSAGE_RECIPIENT, WorkflowId.REGISTRATION);

        assertSending(request, WorkflowId.REGISTRATION);
    }

    @Test
    void when_SendingRecepMessage_Then_ExpectHttpPostAndCorrectUri() {
        MeshRequests meshRequests = new MeshRequests(meshConfig, meshHeaders);

        var request = meshRequests.sendMessage(MESSAGE_RECIPIENT, WorkflowId.RECEP);

        assertSending(request, WorkflowId.RECEP);
    }

    @Test
    void when_GettingMessageIds_Then_ExpectHttpGetAndCorrectUri() {
        MeshRequests meshRequests = new MeshRequests(meshConfig, meshHeaders);

        var request = meshRequests.getMessageIds();

        assertThat(request).isExactlyInstanceOf(HttpGet.class);
        assertThat(request.getURI().toString()).isEqualTo(MESH_HOST + MAILBOX_ID + "/inbox");
    }

    @Test
    void when_AcknowledgeMessage_Then_ExpectHttpPutAndCorrectUri() {
        MeshRequests meshRequests = new MeshRequests(meshConfig, meshHeaders);

        var request = meshRequests.acknowledge(MESSAGE_ID);

        assertThat(request).isExactlyInstanceOf(HttpPut.class);
        assertThat(request.getURI().toString()).isEqualTo(MESH_HOST + MAILBOX_ID + "/inbox/" +  MESSAGE_ID + "/status/acknowledged");
    }

    @Test
    void when_Authenticate_Then_ExpectHttpPostAndCorrectUri() {
        MeshRequests meshRequests = new MeshRequests(meshConfig, meshHeaders);

        var request = meshRequests.authenticate();

        assertThat(request).isExactlyInstanceOf(HttpPost.class);
        assertThat(request.getURI().toString()).isEqualTo(MESH_HOST + MAILBOX_ID);
    }

    private void assertSending(HttpEntityEnclosingRequestBase request, WorkflowId workflowId) {
        assertThat(request).isExactlyInstanceOf(HttpPost.class);
        assertThat(request.getURI().toString()).isEqualTo(MESH_HOST + MAILBOX_ID  + "/outbox");
        Header[] mexToHeader = request.getHeaders("Mex-To");
        assertThat(mexToHeader.length).isEqualTo(1);
        assertThat(mexToHeader[0].getValue()).isEqualTo(MESSAGE_RECIPIENT);
        assertThat(request.getHeaders("Mex-WorkflowID")[0].getValue()).isEqualTo(workflowId.getWorkflowId());
    }
}