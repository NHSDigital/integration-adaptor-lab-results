package uk.nhs.digital.nhsconnect.lab.results;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.digital.nhsconnect.lab.results.mesh.RecipientMailboxIdMappings;
import uk.nhs.digital.nhsconnect.lab.results.mesh.http.MeshClient;
import uk.nhs.digital.nhsconnect.lab.results.mesh.http.MeshConfig;
import uk.nhs.digital.nhsconnect.lab.results.mesh.http.MeshHeaders;
import uk.nhs.digital.nhsconnect.lab.results.mesh.http.MeshHttpClientBuilder;
import uk.nhs.digital.nhsconnect.lab.results.mesh.http.MeshRequests;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.MeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.OutboundMeshMessage;

import javax.annotation.PostConstruct;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class, SoftAssertionsExtension.class, IntegrationTestsExtension.class})
@SpringBootTest
@Slf4j
public abstract class IntegrationBaseTest {

    protected static final int WAIT_FOR_IN_SECONDS = 10;
    protected static final int POLL_INTERVAL_MS = 100;
    protected static final int POLL_DELAY_MS = 10;

    @Autowired
    protected MeshClient meshClient;
    @Autowired
    protected MeshConfig meshConfig;
    @Autowired
    private RecipientMailboxIdMappings recipientMailboxIdMappings;
    @Autowired
    private MeshHttpClientBuilder meshHttpClientBuilder;

    protected MeshClient labResultsMeshClient;

    @PostConstruct
    private void postConstruct() {
        labResultsMeshClient = buildMeshClientForLabResultsMailbox();
    }


    protected void waitForCondition(Supplier<Boolean> supplier) {
        await()
                .atMost(WAIT_FOR_IN_SECONDS, SECONDS)
                .pollInterval(POLL_INTERVAL_MS, MILLISECONDS)
                .pollDelay(POLL_DELAY_MS, MILLISECONDS)
                .until(supplier::get);
    }

    protected void clearMeshMailboxes() {
        waitForCondition(() -> acknowledgeAllMeshMessages(meshClient));
        waitForCondition(() -> acknowledgeAllMeshMessages(labResultsMeshClient));
    }

    private Boolean acknowledgeAllMeshMessages(MeshClient meshClient) {
        // acknowledge message will remove it from MESH
        meshClient.getInboxMessageIds().forEach(meshClient::acknowledgeMessage);
        return meshClient.getInboxMessageIds().isEmpty();
    }

    /**
     * This MeshClient is "inverted" so that it can act as a Lab Results system.
     * It receives messages on the labresults mailbox and sends them to the gp mailbox.
     */
    @SneakyThrows
    private MeshClient buildMeshClientForLabResultsMailbox() {
        // getting this from config is
        String labResultsMailboxId = recipientMailboxIdMappings.getRecipientMailboxId(new MeshMessage().setHaTradingPartnerCode("XX11"));
        String gpMailboxId = meshConfig.getMailboxId();
        RecipientMailboxIdMappings mockRecipientMailboxIdMappings = mock(RecipientMailboxIdMappings.class);
        when(mockRecipientMailboxIdMappings.getRecipientMailboxId(any(OutboundMeshMessage.class))).thenReturn(gpMailboxId);
        // getters perform a transformation
        String endpointCert = (String) FieldUtils.readField(meshConfig, "endpointCert", true);
        String endpointPrivateKey = (String) FieldUtils.readField(meshConfig, "endpointPrivateKey", true);
        String subCaCert = (String) FieldUtils.readField(meshConfig, "subCAcert", true);
        MeshConfig labResultsMailboxConfig = new MeshConfig(labResultsMailboxId, meshConfig.getMailboxPassword(),
                meshConfig.getSharedKey(), meshConfig.getHost(), meshConfig.getCertValidation(), endpointCert,
                endpointPrivateKey, subCaCert);
        MeshHeaders meshHeaders = new MeshHeaders(labResultsMailboxConfig);
        MeshRequests meshRequests = new MeshRequests(labResultsMailboxConfig, meshHeaders);
        return new MeshClient(meshRequests, mockRecipientMailboxIdMappings, meshHttpClientBuilder);
    }
}
