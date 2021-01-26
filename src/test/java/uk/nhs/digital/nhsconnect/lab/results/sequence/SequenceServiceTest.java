package uk.nhs.digital.nhsconnect.lab.results.sequence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SequenceServiceTest {
    private static final String TRANSACTION_ID = "TN-sender";
    private static final String INTERCHANGE_ID = "SIS-sender-recipient";
    private static final String MESSAGE_ID = "SMS-sender-recipient";
    private static final Long SEQ_VALUE = 1L;

    @InjectMocks
    private SequenceService sequenceService;

    @Mock
    private SequenceRepository sequenceRepository;

    @Test
    void when_generateTransactionId_expect_correctValue() {
        when(sequenceRepository.getNextForTransaction(TRANSACTION_ID)).thenReturn(SEQ_VALUE);
        assertThat(sequenceService.generateTransactionNumber("sender")).isEqualTo(SEQ_VALUE);
    }

    @Test
    void when_generateInterchangeId_expect_correctValue() {
        when(sequenceRepository.getNext(INTERCHANGE_ID)).thenReturn(SEQ_VALUE);
        assertThat(sequenceService.generateInterchangeSequence("sender", "recipient"))
            .isEqualTo(SEQ_VALUE);
    }

    @Test
    void when_generateMessageId_expect_resetValue() {
        when(sequenceRepository.getNext(MESSAGE_ID)).thenReturn(SEQ_VALUE);
        assertThat(sequenceService.generateMessageSequence("sender", "recipient"))
            .isEqualTo(SEQ_VALUE);
    }

    @Test
    void when_generateIdsForInvalidSender_expect_exception() {
        assertThatThrownBy(() -> sequenceService.generateInterchangeSequence(null, "recipient"))
            .isExactlyInstanceOf(SequenceException.class);
    }

    @Test
    void when_generateIdsForInvalidRecipient_expect_exception() {
        assertThatThrownBy(() -> sequenceService.generateInterchangeSequence("sender", ""))
            .isExactlyInstanceOf(SequenceException.class);
    }

    @Test
    void when_generateTransactionIdsForNullSender_expect_exception() {
        assertThatThrownBy(() -> sequenceService.generateTransactionNumber(null))
            .isExactlyInstanceOf(SequenceException.class);
    }

    @Test
    void when_generateTransactionIdsForEmptySender_expect_exception() {
        assertThatThrownBy(() -> sequenceService.generateTransactionNumber(""))
            .isExactlyInstanceOf(SequenceException.class);
    }
}
