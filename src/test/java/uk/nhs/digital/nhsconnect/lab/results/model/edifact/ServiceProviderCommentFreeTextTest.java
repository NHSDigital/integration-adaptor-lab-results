package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceProviderCommentFreeTextTest {

    @Test
    void testFromString() {
        var parsedFreeText = ServiceProviderCommentFreeText.fromString(
            "FTX+SPC+++red blood cell seen, Note low platelets");
        assertThat(parsedFreeText.getServiceProviderComment()).isEqualTo("red blood cell seen, Note low platelets");
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        assertThatThrownBy(() -> ServiceProviderCommentFreeText.fromString("wrong value"))
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testValidationEmptyString() {
        ServiceProviderCommentFreeText emptyFreeText = new ServiceProviderCommentFreeText(StringUtils.EMPTY);
        assertThatThrownBy(emptyFreeText::validate)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("FTX: Attribute freeTextValue is blank or missing");
    }

    @Test
    void testValidationBlankString() {
        ServiceProviderCommentFreeText emptyFreeText = new ServiceProviderCommentFreeText(" ");
        assertThatThrownBy(emptyFreeText::validate)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("FTX: Attribute freeTextValue is blank or missing");
    }
}
