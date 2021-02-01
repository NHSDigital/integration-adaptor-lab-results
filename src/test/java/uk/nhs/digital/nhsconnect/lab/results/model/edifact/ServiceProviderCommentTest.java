package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ServiceProviderCommentTest {

    @Test
    void toEdifactTest() {
        var edifact = new ServiceProviderComment("Something").toEdifact();

        assertThat(edifact).isEqualTo("FTX+SPC+++Something'");
    }

    @Test
    void testFromString() {
        var edifact = "FTX+SPC+++red blood cell seen, Note low platelets'";
        var parsedFreeText = ServiceProviderComment.fromString("FTX+SPC+++red blood cell seen, Note low platelets");
        assertThat(parsedFreeText.getServiceProviderComment()).isEqualTo("red blood cell seen, Note low platelets");
        assertThat(parsedFreeText.toEdifact()).isEqualTo(edifact);
        assertThatThrownBy(() -> ServiceProviderComment.fromString("wrong value")).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testPreValidationEmptyString() {
        ServiceProviderComment emptyFreeText = new ServiceProviderComment(StringUtils.EMPTY);
        assertThatThrownBy(emptyFreeText::preValidate)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("FTX: Attribute freeTextValue is blank or missing");
    }

    @Test
    public void testPreValidationBlankString() {
        ServiceProviderComment emptyFreeText = new ServiceProviderComment(" ");
        assertThatThrownBy(emptyFreeText::preValidate)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("FTX: Attribute freeTextValue is blank or missing");
    }
}
