package uk.nhs.digital.nhsconnect.lab.results.inbound;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.HealthAuthorityNameAndAddress;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Interchange;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.InterchangeHeader;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.InterchangeTrailer;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.MessageHeader;
import uk.nhs.digital.nhsconnect.lab.results.sequence.SequenceService;
import uk.nhs.digital.nhsconnect.lab.results.utils.TimestampService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecepProducerServiceTest {
    private static final String RECEP_EXAMPLE_PATH = "/edifact/recep_example.txt";
    private static final String SENDER = "GP11";
    private static final String RECIPIENT = "HA456";
    private static final String REF_SENDER = RECIPIENT;
    private static final String REF_RECIPIENT = SENDER;
    private static final String HA_CIPHER = "GP1";
    private static final String GP_CODE = "123456,123";
    private static final Long INTERCHANGE_SEQUENCE = 45L;
    private static final Long MESSAGE_SEQUENCE_1 = 56L;
    @SuppressWarnings("checkstyle:magicnumber")
    private static final Instant FIXED_TIME = ZonedDateTime
            .of(2020, 4, 27, 17, 37, 0, 0, TimestampService.UK_ZONE)
            .toInstant();
    private static final long RECEP_INTERCHANGE_SEQUENCE = 123123;
    private static final long RECEP_MESSAGE_SEQUENCE = 234234;

    @InjectMocks
    private RecepProducerService recepProducerService;

    @Mock
    private SequenceService sequenceService;

    @Mock
    private TimestampService timestampService;

    @Test
    void when_producingRecep_expect_validRecepIsCreated() throws IOException {
        when(timestampService.getCurrentTimestamp()).thenReturn(FIXED_TIME);
        when(sequenceService.generateInterchangeSequence(REF_SENDER, REF_RECIPIENT))
                .thenReturn(RECEP_INTERCHANGE_SEQUENCE);
        when(sequenceService.generateMessageSequence(REF_SENDER, REF_RECIPIENT)).thenReturn(RECEP_MESSAGE_SEQUENCE);
        var message = mock(Message.class);
        when(message.findFirstGpCode()).thenReturn(GP_CODE);

        var recep = recepProducerService.produceRecep(createInterchange(message));

        assertEquals(recep, readFile());

        verify(sequenceService).generateInterchangeSequence(REF_SENDER, REF_RECIPIENT);
        verify(sequenceService).generateMessageSequence(REF_SENDER, REF_RECIPIENT);

        verifyNoMoreInteractions(sequenceService);
    }

    private Interchange createInterchange(Message message) {
        var interchange = mock(Interchange.class);
        var healthAuthorityNameAndAddress = mock(HealthAuthorityNameAndAddress.class);

        when(interchange.getInterchangeHeader()).thenReturn(
                new InterchangeHeader(SENDER, RECIPIENT, FIXED_TIME).setSequenceNumber(INTERCHANGE_SEQUENCE));
        when(interchange.getInterchangeTrailer()).thenReturn(
                new InterchangeTrailer(1));
        when(interchange.getMessages()).thenReturn(List.of(message));
        when(message.getMessageHeader()).thenReturn(
                new MessageHeader().setSequenceNumber(MESSAGE_SEQUENCE_1));
        when(message.getHealthAuthorityNameAndAddress()).thenReturn(healthAuthorityNameAndAddress);
        when(healthAuthorityNameAndAddress.getIdentifier()).thenReturn(HA_CIPHER);

        return interchange;
    }

    private String readFile() throws IOException {
        try (InputStream is = this.getClass().getResourceAsStream(RecepProducerServiceTest.RECEP_EXAMPLE_PATH)) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }
}