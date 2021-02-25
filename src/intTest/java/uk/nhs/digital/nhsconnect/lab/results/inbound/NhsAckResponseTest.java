package uk.nhs.digital.nhsconnect.lab.results.inbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import uk.nhs.digital.nhsconnect.lab.results.IntegrationBaseTest;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.MeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;
import uk.nhs.digital.nhsconnect.lab.results.sequence.SequenceService;
import uk.nhs.digital.nhsconnect.lab.results.utils.TimestampService;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DirtiesContext
public class NhsAckResponseTest extends IntegrationBaseTest {
    @Value("classpath:edifact/pathology_edifact_IAF.dat")
    private Resource edifactIAFResource;

    @Value("classpath:edifact/pathology_nhsAck_IAF.dat")
    private Resource nhsAckIAFResource;

    @Value("classpath:edifact/pathology_edifact_IAP.dat")
    private Resource edifactIAPResource;

    @Value("classpath:edifact/pathology_nhsAck_IAP.dat")
    private Resource nhsAckIAPResource;

    @Value("classpath:edifact/pathology_edifact_IRA.dat")
    private Resource edifactIRAResource;

    @Value("classpath:edifact/pathology_nhsAck_IRA.dat")
    private Resource nhsAckIRAResource;

    @Value("classpath:edifact/pathology_edifact_IRM.dat")
    private Resource edifactIRMResource;

    @Value("classpath:edifact/pathology_nhsAck_IRM.dat")
    private Resource nhsAckIRMResource;

    @Value("classpath:edifact/pathology_edifact_IRI.dat")
    private Resource edifactIRIResource;

    @Value("classpath:edifact/pathology_nhsAck_IRI.dat")
    private Resource nhsAckIRIResource;

    @Value("classpath:edifact/pathology_edifact_no_nhsAck.dat")
    private Resource edifactNoNHSACKResource;

    @MockBean
    private TimestampService timestampService;

    @MockBean
    private SequenceService sequenceService;

    private static final Instant FIXED_TIME = Instant.parse("2020-04-27T16:37:00Z");
    private static final Long FIXED_SEQUENCE_NUMBER = 1000L;

    @BeforeEach
    void setUp() {
        when(timestampService.getCurrentTimestamp()).thenReturn(FIXED_TIME);
        when(sequenceService.generateInterchangeSequence(any(), any())).thenReturn(FIXED_SEQUENCE_NUMBER);
        clearGpOutboundQueue();
        clearMeshMailboxes();
    }

    @Test
    void whenValidEdifactSentThenIAFNhsAckReturned()
            throws IOException {

        final String content = new String(Files.readAllBytes(edifactIAFResource.getFile().toPath()));

        final MeshMessage meshMessage = new MeshMessage()
                .setWorkflowId(WorkflowId.PATHOLOGY)
                .setContent(content);

        sendToMeshInboundQueue(meshMessage);

        final var nhsAck = waitForMeshMessage(getMeshClient());

        assertThat(nhsAck.getWorkflowId()).isEqualTo(WorkflowId.PATHOLOGY_ACK);

        final String nhsAckContent = nhsAck.getContent();
        final String expectedContent = new String(Files.readAllBytes(nhsAckIAFResource.getFile().toPath())).
                replace("\n", "");
        assertThat(nhsAckContent).isEqualTo(expectedContent);
    }

    @Test
    void whenPartiallyValidEdifactSentThenIAPNhsAckReturned()
            throws IOException {

        final String content = new String(Files.readAllBytes(edifactIAPResource.getFile().toPath()));

        final MeshMessage meshMessage = new MeshMessage()
                .setWorkflowId(WorkflowId.PATHOLOGY)
                .setContent(content);

        sendToMeshInboundQueue(meshMessage);

        final var nhsAck = waitForMeshMessage(getMeshClient());

        assertThat(nhsAck.getWorkflowId()).isEqualTo(WorkflowId.PATHOLOGY_ACK);

        final String nhsAckContent = nhsAck.getContent();
        final String expectedContent = new String(Files.readAllBytes(nhsAckIAPResource.getFile().toPath())).
                replace("\n", "");
        assertThat(nhsAckContent).isEqualTo(expectedContent);
    }

    @Test
    void whenAllEdifactMessagesAreInvalidThenIRANhsAckReturned()
            throws IOException {

        final String content = new String(Files.readAllBytes(edifactIRAResource.getFile().toPath()));

        final MeshMessage meshMessage = new MeshMessage()
                .setWorkflowId(WorkflowId.PATHOLOGY)
                .setContent(content);

        sendToMeshInboundQueue(meshMessage);

        final var nhsAck = waitForMeshMessage(getMeshClient());

        assertThat(nhsAck.getWorkflowId()).isEqualTo(WorkflowId.PATHOLOGY_ACK);

        final String nhsAckContent = nhsAck.getContent();
        final String expectedContent = new String(Files.readAllBytes(nhsAckIRAResource.getFile().toPath())).
                replace("\n", "");
        assertThat(nhsAckContent).isEqualTo(expectedContent);
    }

    @Test
    void whenInvalidInterchangeInEdifactSentThenIRINhsAckReturned()
            throws IOException {

        final String content = new String(Files.readAllBytes(edifactIRIResource.getFile().toPath()));

        final MeshMessage meshMessage = new MeshMessage()
                .setWorkflowId(WorkflowId.PATHOLOGY)
                .setContent(content);

        sendToMeshInboundQueue(meshMessage);

        final var nhsAck = waitForMeshMessage(getMeshClient());

        assertThat(nhsAck.getWorkflowId()).isEqualTo(WorkflowId.PATHOLOGY_ACK);

        final String nhsAckContent = nhsAck.getContent();
        final String expectedContent = new String(Files.readAllBytes(nhsAckIRIResource.getFile().toPath())).
                replace("\n", "");
        assertThat(nhsAckContent).isEqualTo(expectedContent);
    }

    @Test
    void whenEdifactMessagesCannotBeExtractedThenIRMNhsAckReturned()
            throws IOException {

        final String content = new String(Files.readAllBytes(edifactIRMResource.getFile().toPath()));

        final MeshMessage meshMessage = new MeshMessage()
                .setWorkflowId(WorkflowId.PATHOLOGY)
                .setContent(content);

        sendToMeshInboundQueue(meshMessage);

        final var nhsAck = waitForMeshMessage(getMeshClient());

        assertThat(nhsAck.getWorkflowId()).isEqualTo(WorkflowId.PATHOLOGY_ACK);

        final String nhsAckContent = nhsAck.getContent();
        final String expectedContent = new String(Files.readAllBytes(nhsAckIRMResource.getFile().toPath())).
                replace("\n", "");
        assertThat(nhsAckContent).isEqualTo(expectedContent);
    }
}
