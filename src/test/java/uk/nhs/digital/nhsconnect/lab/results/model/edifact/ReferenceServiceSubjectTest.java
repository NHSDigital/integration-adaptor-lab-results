package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class ReferenceServiceSubjectTest {
    @Test
    void testGetKey() {
        assertThat(new ReferenceServiceSubject("").getKey()).isEqualTo("RFF");
    }

    @Test
    void testFromStringValid() {
        var reference = ReferenceServiceSubject.fromString("RFF+SSI:NUMBER");
        assertAll(
            () -> assertThat(reference.getValue()).isEqualTo("SSI:NUMBER"),
            () -> assertThat(reference.getNumber()).isEqualTo("NUMBER")
        );
    }

    @Test
    void testFromStringInvalidWrongPrefix() {
        assertThatThrownBy(() -> ReferenceServiceSubject.fromString("WRONG+SSI:NUMBER"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't create ReferenceServiceSubject from WRONG+SSI:NUMBER");
    }

    @Test
    void testPreValidateMissingNumber() {
        assertThatThrownBy(() -> new ReferenceServiceSubject("").preValidate())
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("RFF: Attribute number is required");
    }
}
