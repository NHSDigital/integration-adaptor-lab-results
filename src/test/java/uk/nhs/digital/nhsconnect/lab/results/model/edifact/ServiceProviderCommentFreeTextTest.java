package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceProviderCommentFreeTextTest {

    @Test
    void toEdifactTest() {
        var edifact = new ServiceProviderCommentFreeText("Something").toEdifact();

        assertThat(edifact).isEqualTo("FTX+SPC+++Something'");
    }

    @Test
    void testFromString() {
        var edifact = "FTX+SPC+++red blood cell seen, Note low platelets'";
        var parsedFreeText = ServiceProviderCommentFreeText.fromString(
            "FTX+SPC+++red blood cell seen, Note low platelets");
        assertThat(parsedFreeText.getServiceProviderComment()).isEqualTo("red blood cell seen, Note low platelets");
        assertThat(parsedFreeText.toEdifact()).isEqualTo(edifact);
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        assertThatThrownBy(() -> ServiceProviderCommentFreeText.fromString("wrong value"))
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testPreValidationEmptyString() {
        ServiceProviderCommentFreeText emptyFreeText = new ServiceProviderCommentFreeText(StringUtils.EMPTY);
        assertThatThrownBy(emptyFreeText::preValidate)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("FTX: Attribute freeTextValue is blank or missing");
    }

    @Test
    void testPreValidationBlankString() {
        ServiceProviderCommentFreeText emptyFreeText = new ServiceProviderCommentFreeText(" ");
        assertThatThrownBy(emptyFreeText::preValidate)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("FTX: Attribute freeTextValue is blank or missing");
    }
}
