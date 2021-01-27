package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SuppressWarnings("checkstyle:MagicNumber")
class ReferenceMessageRecepTest {

    @Test
    void when_gettingKey_expect_returnsProperValue() {
        String key = new ReferenceMessageRecep(
            123L, ReferenceMessageRecep.RecepCode.ERROR)
            .getKey();

        assertThat(key).isEqualTo("RFF");
    }

    @Test
    void when_gettingValue_expect_returnsProperValue() {
        String value = new ReferenceMessageRecep(
            123L, ReferenceMessageRecep.RecepCode.ERROR)
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
            () -> new ReferenceMessageRecep(123L, null)
                .preValidate())
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("RFF: Attribute recepCode is required");
    }

    @Test
    void when_parsingSuccess_expect_recepCreated() {
        final var recepRow = ReferenceMessageRecep.fromString("RFF+MIS:00000005 CP");

        assertAll(
            () -> assertThat(recepRow.getMessageSequenceNumber()).isEqualTo(5L),
            () -> assertThat(recepRow.getRecepCode()).isEqualTo(ReferenceMessageRecep.RecepCode.SUCCESS)
        );
    }

    @Test
    void when_parsingError_expect_recepCreated() {
        final var recepRow = ReferenceMessageRecep.fromString("RFF+MIS:10000006 CA:5:QWE+ASD");

        assertAll(
            () -> assertThat(recepRow.getMessageSequenceNumber()).isEqualTo(10000006L),
            () -> assertThat(recepRow.getRecepCode()).isEqualTo(ReferenceMessageRecep.RecepCode.ERROR)
        );
    }

    @Test
    void when_parsingIncomplete_expect_recepCreated() {
        final var recepRow = ReferenceMessageRecep.fromString("RFF+MIS:99000006 CI+ASD++");

        assertAll(
            () -> assertThat(recepRow.getMessageSequenceNumber()).isEqualTo(99000006L),
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
