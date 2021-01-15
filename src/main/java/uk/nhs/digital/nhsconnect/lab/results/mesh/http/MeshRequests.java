package uk.nhs.digital.nhsconnect.lab.results.mesh.http;

import lombok.RequiredArgsConstructor;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MeshRequests {

    private final MeshConfig meshConfig;

    private final MeshHeaders meshHeaders;

    public HttpPost authenticate() {
        final var request = new HttpPost(meshConfig.getHost() + meshConfig.getMailboxId());
        request.setHeaders(meshHeaders.createAuthenticateHeaders());
        return request;
    }

    public HttpPost sendMessage(String recipient, WorkflowId workflowId) {
        final var request = new HttpPost(meshConfig.getHost() + meshConfig.getMailboxId() + "/outbox");
        request.setHeaders(meshHeaders.createSendHeaders(recipient, workflowId));
        return request;
    }

    public HttpGet getMessage(String messageId) {
        final var request = new HttpGet(meshConfig.getHost() + meshConfig.getMailboxId() + "/inbox/" + messageId);
        request.setHeaders(meshHeaders.createMinimalHeaders());
        return request;
    }

    public HttpGet getMessageIds() {
        final var request = new HttpGet(meshConfig.getHost() + meshConfig.getMailboxId() + "/inbox");
        request.setHeaders(meshHeaders.createMinimalHeaders());
        return request;
    }

    public HttpPut acknowledge(String messageId) {
        final var request = new HttpPut(meshConfig.getHost() + meshConfig.getMailboxId() + "/inbox/" + messageId + "/status/acknowledged");
        request.setHeaders(meshHeaders.createMinimalHeaders());
        return request;
    }

}
