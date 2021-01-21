package uk.nhs.digital.nhsconnect.lab.results.inbound;

import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import uk.nhs.digital.nhsconnect.lab.results.IntegrationBaseTest;

import javax.jms.JMSException;
import javax.jms.Message;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
class DeadLetterQueueTest extends IntegrationBaseTest {

    private static final String MESSAGE_CONTENT = "TRASH";

    @Test
    void whenSendingInvalidMessageToMeshInboundQueueThenMessageIsSentToDeadLetterQueue() throws JMSException {
        clearDeadLetterQueue(meshInboundQueueName);
        sendToMeshInboundQueue(MESSAGE_CONTENT);

        final Message message = getDeadLetterMeshInboundQueueMessage(meshInboundQueueName);
        final String messageBody = parseTextMessage(message);

        assertThat(messageBody).isEqualTo(MESSAGE_CONTENT);
    }

}
