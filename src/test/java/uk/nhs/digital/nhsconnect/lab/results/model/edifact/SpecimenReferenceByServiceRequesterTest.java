package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

public class SpecimenReferenceByServiceRequesterTest {

    @Test
    void toEdifactTest() {
        var edifact = new SpecimenReferenceByServiceRequester("CH000064LX").toEdifact();

        Assertions.assertThat(edifact).isEqualTo("RFF+RTI:CH000064LX'");
    }

    @Test
    void testFromString() {
        var edifact = "RFF+RTI:CH000064LX'";
        var parsedFreeText = SpecimenReferenceByServiceRequester.fromString("RFF+RTI:CH000064LX'");
        Assertions.assertThat(parsedFreeText.getReferenceNumber()).isEqualTo("CH000064LX");
        Assertions.assertThat(parsedFreeText.toEdifact()).isEqualTo(edifact);
        Assertions.assertThatThrownBy(() -> SpecimenReferenceByServiceRequester.fromString("wrong value")).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testPreValidationEmptyString() {
        SpecimenReferenceByServiceRequester emptyFreeText = new SpecimenReferenceByServiceRequester(StringUtils.EMPTY);
        Assertions.assertThatThrownBy(emptyFreeText::preValidate)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("RFF: Speciment Reference number by service requester is blank or missing");
    }

    @Test
    public void testPreValidationBlankString() {
        SpecimenReferenceByServiceRequester emptyFreeText = new SpecimenReferenceByServiceRequester(" ");
        Assertions.assertThatThrownBy(emptyFreeText::preValidate)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("RFF: Speciment Reference number by service requester is blank or missing");
    }
}
