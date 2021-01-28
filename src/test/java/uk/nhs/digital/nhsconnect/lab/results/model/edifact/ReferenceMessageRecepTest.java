package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class ReferenceMessageRecepTest {
    private static final long SEQ = 123L;

    @Test
    void when_gettingKey_expect_returnsProperValue() {
        String key = new ReferenceMessageRecep(SEQ, ReferenceMessageRecep.RecepCode.ERROR)
            .getKey();

        assertThat(key).isEqualTo(ReferenceMessageRecep.KEY);
    }

    @Test
    void when_gettingValue_expect_returnsProperValue() {
        String value = new ReferenceMessageRecep(SEQ, ReferenceMessageRecep.RecepCode.ERROR)
            .getValue();

        assertThat(value).isEqualTo("MIS:00000123 CA");
    }

    @Test
    void when_preValidatedDataNullSequenceNumber_expect_throwsException() {
        assertThatThrownBy(
            () -> new ReferenceMessageRecep(null, ReferenceMessageRecep.RecepCode.ERROR)
                .preValidate())
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("RFF: Attribute messageSequenceNumber is required");
    }

    @Test
    void when_preValidatedDataNullRecepCode_expect_throwsException() {
        assertThatThrownBy(
            () -> new ReferenceMessageRecep(SEQ, null).preValidate())
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("RFF: Attribute recepCode is required");
    }

    @Test
    void when_parsingSuccess_expect_recepCreated() {
        final long seq = 5L;
        final var recepRow = ReferenceMessageRecep.fromString(
            String.format("RFF+MIS:%08d CP", seq));

        assertAll(
            () -> assertThat(recepRow.getMessageSequenceNumber()).isEqualTo(seq),
            () -> assertThat(recepRow.getRecepCode()).isEqualTo(ReferenceMessageRecep.RecepCode.SUCCESS)
        );
    }

    @Test
    void when_parsingError_expect_recepCreated() {
        final long seq = 10_000_006L;
        final var recepRow = ReferenceMessageRecep.fromString(
            String.format("RFF+MIS:%08d CA:5:QWE+ASD", seq));

        assertAll(
            () -> assertThat(recepRow.getMessageSequenceNumber()).isEqualTo(seq),
            () -> assertThat(recepRow.getRecepCode()).isEqualTo(ReferenceMessageRecep.RecepCode.ERROR)
        );
    }

    @Test
    void when_parsingIncomplete_expect_recepCreated() {
        final long seq = 99_000_006L;
        final var recepRow = ReferenceMessageRecep.fromString(
            String.format("RFF+MIS:%08d CI+ASD++", seq));

        assertAll(
            () -> assertThat(recepRow.getMessageSequenceNumber()).isEqualTo(seq),
            () -> assertThat(recepRow.getRecepCode()).isEqualTo(ReferenceMessageRecep.RecepCode.INCOMPLETE)
        );
    }

    @Test
    void when_parsingRecepCodeCP_expect_recepCodeIsCreated() {
        final var actual = ReferenceMessageRecep.RecepCode.fromCode("CP");
        assertThat(actual).isEqualTo(ReferenceMessageRecep.RecepCode.SUCCESS);
    }

    @Test
    void when_parsingRecepCodeCA_expect_recepCodeIsCreated() {
        final var actual = ReferenceMessageRecep.RecepCode.fromCode("CA");
        assertThat(actual).isEqualTo(ReferenceMessageRecep.RecepCode.ERROR);
    }

    @Test
    void when_parsingRecepCodeCI_expect_recepCodeIsCreated() {
        final var actual = ReferenceMessageRecep.RecepCode.fromCode("CI");
        assertThat(actual).isEqualTo(ReferenceMessageRecep.RecepCode.INCOMPLETE);
    }
}
