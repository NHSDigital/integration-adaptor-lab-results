package uk.nhs.digital.nhsconnect.lab.results;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.ValueMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.digital.nhsconnect.lab.results.inbound.EdifactParser;
import uk.nhs.digital.nhsconnect.lab.results.inbound.queue.MeshInboundQueueService;
import uk.nhs.digital.nhsconnect.lab.results.mesh.RecipientMailboxIdMappings;
import uk.nhs.digital.nhsconnect.lab.results.mesh.http.MeshClient;
import uk.nhs.digital.nhsconnect.lab.results.mesh.http.MeshConfig;
import uk.nhs.digital.nhsconnect.lab.results.mesh.http.MeshHeaders;
import uk.nhs.digital.nhsconnect.lab.results.mesh.http.MeshHttpClientBuilder;
import uk.nhs.digital.nhsconnect.lab.results.mesh.http.MeshRequests;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.InboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.MeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.OutboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.utils.JmsReader;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jms.JMSException;
import javax.jms.Message;
import java.util.concurrent.atomic.AtomicReference;
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
@Timeout(IntegrationBaseTest.TIMEOUT_SECONDS)
public abstract class IntegrationBaseTest {

    public static final String DLQ_PREFIX = "DLQ.";
    protected static final int WAIT_FOR_IN_SECONDS = 10;
    protected static final int POLL_INTERVAL_MS = 100;
    protected static final int POLL_DELAY_MS = 10;
    private static final int JMS_RECEIVE_TIMEOUT = 500;
    protected static final int TIMEOUT_SECONDS = 10;
    protected static final ValueMatcher<Object> IGNORE = (a, b) -> true;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Getter
    @Autowired
    private MeshClient meshClient;

    @Autowired
    private MeshConfig meshConfig;
    @Autowired
    private RecipientMailboxIdMappings recipientMailboxIdMappings;
    @Autowired
    private MeshHttpClientBuilder meshHttpClientBuilder;
    @Autowired
    private MeshInboundQueueService meshInboundQueueService;
    @Autowired
    @Getter(AccessLevel.PROTECTED)
    private EdifactParser edifactParser;

    @Getter
    @Value("${labresults.amqp.meshInboundQueueName}")
    private String meshInboundQueueName;

    @Getter
    @Value("${labresults.amqp.meshOutboundQueueName}")
    private String meshOutboundQueueName;

    @Getter
    @Value("${labresults.amqp.gpOutboundQueueName}")
    private String gpOutboundQueueName;

    @Getter
    @Value("classpath:edifact/pathology.edifact.dat")
    private Resource edifactResource;

    @Getter
    @Value("classpath:fhir/pathology.fhir.json")
    private Resource fhirResource;

    @Getter
    @Value("classpath:edifact/pathology_recep.edifact.dat")
    private Resource recepResource;

    private long originalReceiveTimeout;

    @Getter
    private MeshClient labResultsMeshClient;

    @PostConstruct
    private void postConstruct() {
        originalReceiveTimeout = this.jmsTemplate.getReceiveTimeout();
        this.jmsTemplate.setReceiveTimeout(JMS_RECEIVE_TIMEOUT);
        labResultsMeshClient = buildMeshClientForLabResultsMailbox();
    }

    @PreDestroy
    private void preDestroy() {
        this.jmsTemplate.setReceiveTimeout(originalReceiveTimeout);
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

    protected String parseTextMessage(Message message) throws JMSException {
        if (message == null) {
            return null;
        }
        return JmsReader.readMessage(message);
    }

    /**
     * This MeshClient is "inverted" so that it can act as a Lab Results system.
     * It receives messages on the labresults mailbox and sends them to the gp mailbox.
     */
    @SneakyThrows(IllegalAccessException.class)
    private MeshClient buildMeshClientForLabResultsMailbox() {
        // getting this from config is
        final String labResultsMailboxId = recipientMailboxIdMappings.getRecipientMailboxId(
            new MeshMessage().setRecipient("XX11"));
        final String gpMailboxId = meshConfig.getMailboxId();
        final RecipientMailboxIdMappings mockRecipientMailboxIdMappings = mock(RecipientMailboxIdMappings.class);
        when(mockRecipientMailboxIdMappings.getRecipientMailboxId(any(OutboundMeshMessage.class)))
            .thenReturn(gpMailboxId);
        // getters perform a transformation
        final String endpointCert = (String) FieldUtils.readField(meshConfig, "endpointCert", true);
        final String endpointPrivateKey = (String) FieldUtils.readField(meshConfig, "endpointPrivateKey", true);
        final String subCaCert = (String) FieldUtils.readField(meshConfig, "subCAcert", true);
        final MeshConfig labResultsMailboxConfig = new MeshConfig(labResultsMailboxId, meshConfig.getMailboxPassword(),
            meshConfig.getSharedKey(), meshConfig.getHost(), meshConfig.getCertValidation(), endpointCert,
            endpointPrivateKey, subCaCert);
        final MeshHeaders meshHeaders = new MeshHeaders(labResultsMailboxConfig);
        final MeshRequests meshRequests = new MeshRequests(labResultsMailboxConfig, meshHeaders);
        return new MeshClient(meshRequests, mockRecipientMailboxIdMappings, meshHttpClientBuilder);
    }

    @SneakyThrows
    protected Message getGpOutboundQueueMessage() {
        return waitFor(() -> jmsTemplate.receive(gpOutboundQueueName));
    }

    @SneakyThrows
    protected Message getDeadLetterMeshInboundQueueMessage(String queueName) {
        return waitFor(() -> jmsTemplate.receive(DLQ_PREFIX + queueName));
    }

    protected <T> T waitFor(Supplier<T> supplier) {
        var dataToReturn = new AtomicReference<T>();
        await()
            .atMost(WAIT_FOR_IN_SECONDS, SECONDS)
            .pollInterval(POLL_INTERVAL_MS, MILLISECONDS)
            .pollDelay(POLL_DELAY_MS, MILLISECONDS)
            .until(() -> {
                var data = supplier.get();
                if (data != null) {
                    dataToReturn.set(data);
                    return true;
                }
                return false;
            });

        return dataToReturn.get();
    }

    protected void clearGpOutboundQueue() {
        waitForCondition(() -> jmsTemplate.receive(gpOutboundQueueName) == null);
    }

    @SneakyThrows
    protected void clearDeadLetterQueue(String queueName) {
        waitForCondition(() -> jmsTemplate.receive(DLQ_PREFIX + queueName) == null);
    }

    protected void sendToMeshInboundQueue(InboundMeshMessage meshMessage) {
        meshInboundQueueService.publish(meshMessage);
    }

    protected void sendToMeshInboundQueue(String data) {
        jmsTemplate.send(meshInboundQueueName, session -> session.createTextMessage(data));
    }
}
