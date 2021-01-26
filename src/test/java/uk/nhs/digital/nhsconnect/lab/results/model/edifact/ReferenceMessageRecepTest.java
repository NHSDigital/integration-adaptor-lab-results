package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SoftAssertionsExtension.class)
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

        assertThat("MIS:00000123 CA").isEqualTo(value);
    }

    @Test
    void when_preValidatedDataViolatesNullChecks_expect_throwsException(SoftAssertions softly) {
        softly.assertThatThrownBy(
            () -> new ReferenceMessageRecep(null, ReferenceMessageRecep.RecepCode.ERROR)
                .preValidate())
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("RFF: Attribute messageSequenceNumber is required");

        softly.assertThatThrownBy(
            () -> new ReferenceMessageRecep(123L, null)
                .preValidate())
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("RFF: Attribute recepCode is required");
    }

    @Test
    void when_parsing_expect_recepCreated(SoftAssertions softly) {
        var recepRow = ReferenceMessageRecep.fromString("RFF+MIS:00000005 CP");

        softly.assertThat(recepRow.getMessageSequenceNumber()).isEqualTo(5L);
        softly.assertThat(recepRow.getRecepCode()).isEqualTo(ReferenceMessageRecep.RecepCode.SUCCESS);

        recepRow = ReferenceMessageRecep.fromString("RFF+MIS:10000006 CA:5:QWE+ASD");

        softly.assertThat(recepRow.getMessageSequenceNumber()).isEqualTo(10000006L);
        softly.assertThat(recepRow.getRecepCode()).isEqualTo(ReferenceMessageRecep.RecepCode.ERROR);

        recepRow = ReferenceMessageRecep.fromString("RFF+MIS:99000006 CI+ASD++");

        softly.assertThat(recepRow.getMessageSequenceNumber()).isEqualTo(99000006L);
        softly.assertThat(recepRow.getRecepCode()).isEqualTo(ReferenceMessageRecep.RecepCode.INCOMPLETE);
    }

    @Test
    void when_parsingRecepCodeFromCode_expect_recepCodeIsCreated(SoftAssertions softly) {
        var toParse = new String[]{"CP", "CA", "CI"};

        for (int i = 0; i < ReferenceMessageRecep.RecepCode.values().length; i++) {
            var actual = ReferenceMessageRecep.RecepCode.fromCode(toParse[i]);
            var expected = ReferenceMessageRecep.RecepCode.values()[i];
            softly.assertThat(actual).isEqualTo(expected);
        }
    }

}