package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpecimenReferenceByServiceRequesterTest {

    @Test
    void testFromString() {
        var parsedFreeText = SpecimenReferenceByServiceRequester.fromString("RFF+RTI:CH000064LX");
        assertThat(parsedFreeText.getReferenceNumber()).isEqualTo("CH000064LX");
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        assertThatThrownBy(() -> SpecimenReferenceByServiceRequester.fromString("wrong value"))
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testValidationEmptyString() {
        SpecimenReferenceByServiceRequester emptyFreeText = new SpecimenReferenceByServiceRequester(StringUtils.EMPTY);
        assertThatThrownBy(emptyFreeText::validate)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("RFF: Specimen Reference number by service requester is blank or missing");
    }

    @Test
    void testValidationBlankString() {
        SpecimenReferenceByServiceRequester emptyFreeText = new SpecimenReferenceByServiceRequester(" ");
        assertThatThrownBy(emptyFreeText::validate)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("RFF: Specimen Reference number by service requester is blank or missing");
    }
}
