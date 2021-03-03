package uk.nhs.digital.nhsconnect.lab.results.inbound;

import lombok.NonNull;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.annotation.DirtiesContext;
import uk.nhs.digital.nhsconnect.lab.results.IntegrationBaseTest;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.MeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that the correct NHSACK is returned for valid interchanges and when errors occur in processing.
 * There is an NHSACK status code for each type of response:
 * <br>
 * IAF: Valid Interchange, all Messages accepted
 * IAP: Valid Interchange, some Messages accepted, some Messages rejected
 * IRA: Valid Interchange, but all Messages were rejected
 * IRM: Interchange rejected due to an error in one message
 * IRI: Interchange rejected due to interchange level error
 */
@DirtiesContext
class NhsAckResponseTest extends IntegrationBaseTest {

    @BeforeEach
    void setUp() {
        clearGpOutboundQueue();
        clearMeshMailboxes();
        clearMeshOutboundQueue();
    }

    @ParameterizedTest
    @CsvSource({
        "pathology_IAF.edifact.dat,pathology_IAF_regex.nhsack.dat",
        "pathology_IAP.edifact.dat,pathology_IAP_regex.nhsack.dat",
        "pathology_IRA.edifact.dat,pathology_IRA_regex.nhsack.dat",
        "pathology_IRM.edifact.dat,pathology_IRM_regex.nhsack.dat",
        "pathology_IRI.edifact.dat,pathology_IRI_regex.nhsack.dat"
    })
    void testReturnCorrectNhsAckForGivenEdifact(String edifactFile, String expectedNhsAckFile) throws IOException {
        final String inputEdifact = readResource(edifactFile);

        final String expectedNhsAck = readResource(expectedNhsAckFile).replace("\n", "");

        final MeshMessage meshMessage = new MeshMessage()
            .setWorkflowId(WorkflowId.PATHOLOGY)
            .setContent(inputEdifact);

        sendToMeshInboundQueue(meshMessage);

        final var nhsAck = waitForMeshMessage(getMeshClient());

        assertThat(nhsAck.getWorkflowId()).isEqualTo(WorkflowId.PATHOLOGY_ACK);

        final String nhsAckContent = nhsAck.getContent();

        assertThat(nhsAckContent).matches(expectedNhsAck);
    }

    private String readResource(@NonNull String fileName) throws IOException {
        var resourceStream = getClass().getClassLoader().getResourceAsStream("edifact/" + fileName);

        if (resourceStream == null) {
            throw new IllegalArgumentException("Content file is missing or empty");
        }

        return IOUtils.toString(resourceStream, StandardCharsets.UTF_8);
    }
}
