package uk.nhs.digital.nhsconnect.lab.results;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.json.JSONException;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.ValueMatcher;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
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
import uk.nhs.digital.nhsconnect.lab.results.utils.JmsReader;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
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
    private static final ValueMatcher<Object> IGNORE = (a, b) -> true;
    private static final String TIMESTAMP_REGEX =
        "[0-9]{4}-[0-1][0-9]-[0-3][0-9]T[0-2][0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]{1,3}\\+[0-9]{2}:[0-9]{2}";
    private static final ValueMatcher<Object> AS_INSTANTS = (d1, d2) -> {
        if (Objects.equals(d1, d2)) {
            return true;
        }
        try {
            var date1 = OffsetDateTime.parse((String) d1);
            var date2 = OffsetDateTime.parse((String) d2);
            return date1.toInstant().equals(date2.toInstant());
        } catch (DateTimeParseException e) {
            return false;
        }
    };
    private static final ValueMatcher<Object> IS_TIMESTAMP = (d1, d2) ->
        String.valueOf(d1).matches(TIMESTAMP_REGEX) && String.valueOf(d2).matches(TIMESTAMP_REGEX);

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
    @Value("classpath:edifact/pathology_2.edifact.dat")
    private Resource pathology2EdifactResource;

    @Getter
    @Value("classpath:edifact/pathology_3.edifact.dat")
    private Resource pathology3EdifactResource;

    @Getter
    @Value("classpath:edifact/screening.edifact.dat")
    private Resource screeningEdifactResource;

    @Getter
    @Value("classpath:fhir/pathology_2.fhir.json")
    private Resource pathology2FhirResource;

    @Getter
    @Value("classpath:fhir/pathology_3.fhir.json")
    private Resource pathology3FhirResource;

    @Getter
    @Value("classpath:fhir/screening.fhir.json")
    private Resource screeningFhirResource;

    @Getter
    @Value("classpath:edifact/pathology.nhsack.dat")
    private Resource nhsAckResource;

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
        final String labResultsMailboxId = recipientMailboxIdMappings.getRecipientMailboxId("000000024600002");
        final String gpMailboxId = meshConfig.getMailboxId();
        final RecipientMailboxIdMappings mockRecipientMailboxIdMappings = mock(RecipientMailboxIdMappings.class);
        when(mockRecipientMailboxIdMappings.getRecipientMailboxId(anyString()))
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

    protected InboundMeshMessage waitForMeshMessage(MeshClient meshClient) {
        List<String> messageIds = waitFor(() -> {
            List<String> inboxMessageIds = meshClient.getInboxMessageIds();
            return inboxMessageIds.isEmpty() ? null : inboxMessageIds;
        });
        return meshClient.getEdifactMessage(messageIds.get(0));
    }

    protected boolean gpOutboundQueueIsEmpty() {
        return jmsTemplate.receive(gpOutboundQueueName) == null;
    }

    protected void clearGpOutboundQueue() {
        waitForCondition(() -> jmsTemplate.receive(gpOutboundQueueName) == null);
    }

    protected void clearMeshOutboundQueue() {
        waitForCondition(() -> jmsTemplate.receive(meshOutboundQueueName) == null);
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

    protected void assertFhirEquals(String expected, String actual) throws JSONException {
        JSONAssert.assertEquals(
            expected,
            actual,
            new CustomComparator(
                JSONCompareMode.STRICT,
                new Customization("id", IGNORE),
                new Customization("meta.lastUpdated", IS_TIMESTAMP),
                new Customization("identifier.value", IGNORE),
                new Customization("entry[*].fullUrl", IGNORE),
                new Customization("entry[*].resource.**.reference", IGNORE),
                new Customization("entry[*].resource.id", IGNORE),
                new Customization("entry[*].resource.issued", AS_INSTANTS),
                new Customization("entry[*].resource.receivedTime", AS_INSTANTS),
                new Customization("entry[*].resource.timestamp", IS_TIMESTAMP),
                new Customization("entry[*].resource.collection.collectedDateTime", AS_INSTANTS)
            )
        );
    }
}
