package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReferenceInterchangeRecepTest {
    private static final long SEQ = 123L;

    @Test
    void when_gettingValue_expect_returnsProperValue() {
        final String value = new ReferenceInterchangeRecep(
            SEQ, ReferenceInterchangeRecep.RecepCode.RECEIVED, 1)
            .getValue();

        assertThat(value).isEqualTo("RIS:00000123 OK:1");
    }

    @Test
    void when_gettingKey_expect_returnsProperValue() {
        final String key = new ReferenceInterchangeRecep(
            SEQ, ReferenceInterchangeRecep.RecepCode.RECEIVED, 1)
            .getKey();

        assertThat(key).isEqualTo("RFF");
    }

    @Test
    void when_preValidatedDataNullSequenceNumber_expect_throwsException() {
        assertThatThrownBy(
            () -> new ReferenceInterchangeRecep(null, ReferenceInterchangeRecep.RecepCode.RECEIVED, 1)
                .preValidate())
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("RFF: Attribute messageSequenceNumber is required");
    }

    @Test
    void when_preValidatedDataNullRecepCode_expect_throwsException() {
        assertThatThrownBy(
            () -> new ReferenceInterchangeRecep(SEQ, null, 1)
                .preValidate())
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("RFF: Attribute recepCode is required");
    }

    @Test
    void when_preValidatedDataNullMessageCount_expect_throwsException() {
        assertThatThrownBy(
            () -> new ReferenceInterchangeRecep(SEQ, ReferenceInterchangeRecep.RecepCode.RECEIVED, null)
                .preValidate())
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("RFF: Attribute messageCount is required");
    }

    @Test
    void when_parsingNoValidData_expect_recepCreated() {
        final long seq = 99_000_006L;
        final int msgCount = 10;
        final var recepRow = ReferenceInterchangeRecep.fromString(
            String.format("RFF+RIS:%08d NA:%d:QWE:ASD++", seq, msgCount));

        assertThat(recepRow.getInterchangeSequenceNumber()).isEqualTo(seq);
        assertThat(recepRow.getRecepCode()).isEqualTo(ReferenceInterchangeRecep.RecepCode.NO_VALID_DATA);
        assertThat(recepRow.getMessageCount()).isEqualTo(msgCount);
    }

    @Test
    void when_parsingReceived_expect_recepCreated() {
        final long seq = 5L;
        final int msgCount = 4;
        final var recepRow = ReferenceInterchangeRecep.fromString(
            String.format("RFF+RIS:%08d OK:%d", seq, msgCount));

        assertThat(recepRow.getInterchangeSequenceNumber()).isEqualTo(seq);
        assertThat(recepRow.getRecepCode()).isEqualTo(ReferenceInterchangeRecep.RecepCode.RECEIVED);
        assertThat(recepRow.getMessageCount()).isEqualTo(msgCount);
    }

    @Test
    void when_parsingInvalidData_expect_recepCreated() {
        final long seq = 10_000_006L;
        final int msgCount = 5;
        final var recepRow = ReferenceInterchangeRecep.fromString(
            String.format("RFF+RIS:%08d ER:%d:QWE+ASD", seq, msgCount));

        assertThat(recepRow.getInterchangeSequenceNumber()).isEqualTo(seq);
        assertThat(recepRow.getRecepCode()).isEqualTo(ReferenceInterchangeRecep.RecepCode.INVALID_DATA);
        assertThat(recepRow.getMessageCount()).isEqualTo(msgCount);
    }

    @Test
    void when_parsingOK_expect_recepCodeIsCreated() {
        final var actual = ReferenceInterchangeRecep.RecepCode.fromCode("OK");
        assertThat(actual).isEqualTo(ReferenceInterchangeRecep.RecepCode.RECEIVED);
    }

    @Test
    void when_parsingNA_expect_recepCodeIsCreated() {
        final var actual = ReferenceInterchangeRecep.RecepCode.fromCode("NA");
        assertThat(actual).isEqualTo(ReferenceInterchangeRecep.RecepCode.NO_VALID_DATA);
    }

    @Test
    void when_parsingER_expect_recepCodeIsCreated() {
        final var actual = ReferenceInterchangeRecep.RecepCode.fromCode("ER");
        assertThat(actual).isEqualTo(ReferenceInterchangeRecep.RecepCode.INVALID_DATA);
    }
}
