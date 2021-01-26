package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("checkstyle:MagicNumber")
class ReferenceInterchangeRecepTest {
    @Test
    void when_gettingKey_expect_returnsProperValue() {
        final String key = new ReferenceInterchangeRecep(
            123L, ReferenceInterchangeRecep.RecepCode.RECEIVED, 3)
            .getKey();

        assertThat(key).isEqualTo("RFF");
    }

    @Test
    void when_gettingValue_expect_returnsProperValue() {
        final String value = new ReferenceInterchangeRecep(
            123L, ReferenceInterchangeRecep.RecepCode.RECEIVED, 3)
            .getValue();

        assertThat(value).isEqualTo("RIS:00000123 OK:3");
    }

    @Test
    void when_preValidatedDataNullSequenceNumber_expect_throwsException() {
        assertThatThrownBy(
            () -> new ReferenceInterchangeRecep(null, ReferenceInterchangeRecep.RecepCode.RECEIVED, 3)
                .preValidate())
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("RFF: Attribute messageSequenceNumber is required");
    }

    @Test
    void when_preValidatedDataNullRecepCode_expect_throwsException() {
        assertThatThrownBy(
            () -> new ReferenceInterchangeRecep(123L, null, 3)
                .preValidate())
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("RFF: Attribute recepCode is required");
    }

    @Test
    void when_preValidatedDataNullMessageCount_expect_throwsException() {
        assertThatThrownBy(
            () -> new ReferenceInterchangeRecep(123L, ReferenceInterchangeRecep.RecepCode.RECEIVED, null)
                .preValidate())
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("RFF: Attribute messageCount is required");
    }

    @Test
    void when_parsingNoValidData_expect_recepCreated() {
        final var recepRow = ReferenceInterchangeRecep.fromString("RFF+RIS:99000006 NA:10:QWE:ASD++");

        assertThat(recepRow.getInterchangeSequenceNumber()).isEqualTo(99000006L);
        assertThat(recepRow.getRecepCode()).isEqualTo(ReferenceInterchangeRecep.RecepCode.NO_VALID_DATA);
        assertThat(recepRow.getMessageCount()).isEqualTo(10);
    }

    @Test
    void when_parsingReceived_expect_recepCreated() {
        final var recepRow = ReferenceInterchangeRecep.fromString("RFF+RIS:00000005 OK:4");

        assertThat(recepRow.getInterchangeSequenceNumber()).isEqualTo(5L);
        assertThat(recepRow.getRecepCode()).isEqualTo(ReferenceInterchangeRecep.RecepCode.RECEIVED);
        assertThat(recepRow.getMessageCount()).isEqualTo(4);
    }

    @Test
    void when_parsingInvalidData_expect_recepCreated() {
        final var recepRow = ReferenceInterchangeRecep.fromString("RFF+RIS:10000006 ER:5:QWE+ASD");

        assertThat(recepRow.getInterchangeSequenceNumber()).isEqualTo(10000006L);
        assertThat(recepRow.getRecepCode()).isEqualTo(ReferenceInterchangeRecep.RecepCode.INVALID_DATA);
        assertThat(recepRow.getMessageCount()).isEqualTo(5);
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
