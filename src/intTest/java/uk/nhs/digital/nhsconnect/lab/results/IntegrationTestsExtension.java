package uk.nhs.digital.nhsconnect.lab.results;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.digital.nhsconnect.lab.results.container.ActiveMqContainer;
import uk.nhs.digital.nhsconnect.lab.results.container.FakeMeshContainer;
import uk.nhs.digital.nhsconnect.lab.results.container.MongoDbContainer;
import uk.nhs.digital.nhsconnect.lab.results.mesh.scheduler.SchedulerTimestampRepository;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.springframework.jms.support.destination.JmsDestinationAccessor.RECEIVE_TIMEOUT_NO_WAIT;
import static uk.nhs.digital.nhsconnect.lab.results.IntegrationBaseTest.DLQ_PREFIX;

@Slf4j
public class IntegrationTestsExtension implements BeforeAllCallback, BeforeEachCallback {

    private static final String DLQ_PREFIX = "DLQ.";

    @Override
    public void beforeAll(ExtensionContext context) {
        ActiveMqContainer.getInstance().start();
        MongoDbContainer.getInstance().start();
        FakeMeshContainer.getInstance().start();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        final var applicationContext = SpringExtension.getApplicationContext(context);

        var jmsTemplate = applicationContext.getBean(JmsTemplate.class);

        var meshInboundQueueName = Objects.requireNonNull(
                applicationContext.getEnvironment().getProperty("labresults.amqp.meshInboundQueueName"));
        var meshOutboundQueueName = Objects.requireNonNull(
                applicationContext.getEnvironment().getProperty("labresults.amqp.meshOutboundQueueName"));
        var gpOutboundQueueName = Objects.requireNonNull(
                applicationContext.getEnvironment().getProperty("labresults.amqp.gpOutboundQueueName"));

        var receiveTimeout = jmsTemplate.getReceiveTimeout();
        jmsTemplate.setReceiveTimeout(RECEIVE_TIMEOUT_NO_WAIT);
        List.of(meshInboundQueueName, meshOutboundQueueName, gpOutboundQueueName)
                .stream()
                .map(queueName -> List.of(queueName, DLQ_PREFIX + queueName))
                .flatMap(Collection::stream)
                .forEach(queueName -> {
                    while (jmsTemplate.receive(queueName) != null) {
                        LOGGER.info("Purged '" + queueName + "' message");
                    }
                });
        jmsTemplate.setReceiveTimeout(receiveTimeout);

        final var schedulerTimestampRepository = applicationContext.getBean(SchedulerTimestampRepository.class);
        schedulerTimestampRepository.deleteAll();
    }
}
