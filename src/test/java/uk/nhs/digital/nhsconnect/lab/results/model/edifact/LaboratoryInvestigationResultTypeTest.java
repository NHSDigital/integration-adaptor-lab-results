package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertAll;

class LaboratoryInvestigationResultTypeTest {

    @Test
    void testFromCodeReturnsResultTypeForValidString() {
        assertAll(
            () -> assertThat(LaboratoryInvestigationResultType.fromCode("NV"))
                .isEqualTo(LaboratoryInvestigationResultType.NUMERICAL_VALUE),
            () -> assertThat(LaboratoryInvestigationResultType.fromCode("CV"))
                .isEqualTo(LaboratoryInvestigationResultType.CODED_VALUE)
        );
    }

    @Test
    void testFromCodeThrowsExceptionForInvalidString() {
        assertThatIllegalArgumentException().isThrownBy(() -> LaboratoryInvestigationResultType.fromCode("INVALID"))
            .withMessage("No laboratory investigation result type for \"INVALID\"");
    }
}
