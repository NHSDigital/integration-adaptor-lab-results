package uk.nhs.digital.nhsconnect.lab.results.utils;

import lombok.SneakyThrows;
import org.apache.qpid.jms.message.JmsTextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jms.Message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JmsReaderTest {

    @Mock
    private Message message;

    @Mock
    private JmsTextMessage jmsTextMessage;

    private static final String CONTENT = "test_message";

    @SneakyThrows
    @Test
    void when_messageIsNotNull_expect_sameMessageIsReturned() {
        when(message.getBody(String.class)).thenReturn(CONTENT);

        String msg = JmsReader.readMessage(message);
        assertEquals(msg, CONTENT);

        when(jmsTextMessage.getText()).thenReturn(CONTENT);

        String jmsMsg = JmsReader.readMessage(jmsTextMessage);
        assertEquals(jmsMsg, CONTENT);
    }

    @SneakyThrows
    @Test
    void when_messageIsNull_expect_nullIsReturned() {
        when(message.getBody(String.class)).thenReturn(null);
        String msg = JmsReader.readMessage(message);

        assertNull(msg);

        when(jmsTextMessage.getText()).thenReturn(null);
        String jmsMsg = JmsReader.readMessage(jmsTextMessage);

        assertNull(jmsMsg);
    }
}
