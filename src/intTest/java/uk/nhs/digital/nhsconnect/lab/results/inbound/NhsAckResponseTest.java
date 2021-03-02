package uk.nhs.digital.nhsconnect.lab.results.inbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import uk.nhs.digital.nhsconnect.lab.results.IntegrationBaseTest;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.MeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;

import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that the correct NHSACK is returned for valid interchanges and when errors occur in processing.
 * There is an NHSACK status code for each type of response:
 *
 * IAF: Valid Interchange, all Messages accepted
 * IAP: Valid Interchange, some Messages accepted, some Messages rejected
 * IRA: Valid Interchange, but all Messages were rejected
 * IRM: Interchange rejected due to an error in one message
 * IRI: Interchange rejected due to interchange level error
 */
@DirtiesContext
class NhsAckResponseTest extends IntegrationBaseTest {
    @Value("classpath:edifact/pathology_IAF.edifact.dat")
    private Resource edifactIAFResource;

    @Value("classpath:edifact/pathology_IAF_regex.nhsack.dat")
    private Resource nhsAckIAFResource;

    @Value("classpath:edifact/pathology_IAP.edifact.dat")
    private Resource edifactIAPResource;

    @Value("classpath:edifact/pathology_IAP_regex.nhsack.dat")
    private Resource nhsAckIAPResource;

    @Value("classpath:edifact/pathology_IRA.edifact.dat")
    private Resource edifactIRAResource;

    @Value("classpath:edifact/pathology_IRA_regex.nhsack.dat")
    private Resource nhsAckIRAResource;

    @Value("classpath:edifact/pathology_IRM.edifact.dat")
    private Resource edifactIRMResource;

    @Value("classpath:edifact/pathology_IRM_regex.nhsack.dat")
    private Resource nhsAckIRMResource;

    @Value("classpath:edifact/pathology_IRI.edifact.dat")
    private Resource edifactIRIResource;

    @Value("classpath:edifact/pathology_IRI_regex.nhsack.dat")
    private Resource nhsAckIRIResource;

    @BeforeEach
    void setUp() {
        clearGpOutboundQueue();
        clearMeshMailboxes();
        clearMeshOutboundQueue();
    }

    @Test
    void whenValidEdifactSentThenCorrectNhsAckReturned()
            throws IOException {

        final String content = new String(Files.readAllBytes(edifactIAFResource.getFile().toPath()));

        final MeshMessage meshMessage = new MeshMessage()
                .setWorkflowId(WorkflowId.PATHOLOGY)
                .setContent(content);

        sendToMeshInboundQueue(meshMessage);

        final var nhsAck = waitForMeshMessage(getMeshClient());

        assertThat(nhsAck.getWorkflowId()).isEqualTo(WorkflowId.PATHOLOGY_ACK);

        final String nhsAckContent = nhsAck.getContent();
        final String expectedContent = new String(Files.readAllBytes(nhsAckIAFResource.getFile().toPath()))
                .replace("\n", "");
        assertThat(nhsAckContent).matches(expectedContent);
    }

    @Test
    void whenPartiallyValidEdifactSentThenCorrectNhsAckReturned()
            throws IOException {

        final String content = new String(Files.readAllBytes(edifactIAPResource.getFile().toPath()));

        final MeshMessage meshMessage = new MeshMessage()
                .setWorkflowId(WorkflowId.PATHOLOGY)
                .setContent(content);

        sendToMeshInboundQueue(meshMessage);

        final var nhsAck = waitForMeshMessage(getMeshClient());

        assertThat(nhsAck.getWorkflowId()).isEqualTo(WorkflowId.PATHOLOGY_ACK);

        final String nhsAckContent = nhsAck.getContent();
        final String expectedContent = new String(Files.readAllBytes(nhsAckIAPResource.getFile().toPath()))
                .replace("\n", "");
        assertThat(nhsAckContent).matches(expectedContent);
    }

    @Test
    void whenAllEdifactMessagesAreInvalidThenCorrectNhsAckReturned()
            throws IOException {

        final String content = new String(Files.readAllBytes(edifactIRAResource.getFile().toPath()));

        final MeshMessage meshMessage = new MeshMessage()
                .setWorkflowId(WorkflowId.PATHOLOGY)
                .setContent(content);

        sendToMeshInboundQueue(meshMessage);

        final var nhsAck = waitForMeshMessage(getMeshClient());

        assertThat(nhsAck.getWorkflowId()).isEqualTo(WorkflowId.PATHOLOGY_ACK);

        final String nhsAckContent = nhsAck.getContent();
        final String expectedContent = new String(Files.readAllBytes(nhsAckIRAResource.getFile().toPath()))
                .replace("\n", "");
        assertThat(nhsAckContent).matches(expectedContent);
    }

    @Test
    void whenInvalidInterchangeInEdifactSentThenCorrectNhsAckReturned()
            throws IOException {

        final String content = new String(Files.readAllBytes(edifactIRIResource.getFile().toPath()));

        final MeshMessage meshMessage = new MeshMessage()
                .setWorkflowId(WorkflowId.PATHOLOGY)
                .setContent(content);

        sendToMeshInboundQueue(meshMessage);

        final var nhsAck = waitForMeshMessage(getMeshClient());

        assertThat(nhsAck.getWorkflowId()).isEqualTo(WorkflowId.PATHOLOGY_ACK);

        final String nhsAckContent = nhsAck.getContent();
        final String expectedContent = new String(Files.readAllBytes(nhsAckIRIResource.getFile().toPath()))
                .replace("\n", "");
        assertThat(nhsAckContent).matches(expectedContent);
    }

    @Test
    void whenEdifactMessagesCannotBeExtractedThenCorrectNhsAckReturned()
            throws IOException {

        final String content = new String(Files.readAllBytes(edifactIRMResource.getFile().toPath()));

        final MeshMessage meshMessage = new MeshMessage()
                .setWorkflowId(WorkflowId.PATHOLOGY)
                .setContent(content);

        sendToMeshInboundQueue(meshMessage);

        final var nhsAck = waitForMeshMessage(getMeshClient());

        assertThat(nhsAck.getWorkflowId()).isEqualTo(WorkflowId.PATHOLOGY_ACK);

        final String nhsAckContent = nhsAck.getContent();
        final String expectedContent = new String(Files.readAllBytes(nhsAckIRMResource.getFile().toPath()))
                .replace("\n", "");
        assertThat(nhsAckContent).matches(expectedContent);
    }
}
