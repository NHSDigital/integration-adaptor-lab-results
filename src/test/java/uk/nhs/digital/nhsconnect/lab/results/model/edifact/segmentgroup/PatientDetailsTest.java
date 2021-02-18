package uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.PersonDateOfBirth;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.PersonName;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.PersonSex;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MissingSegmentException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;


class PatientDetailsTest {
    @Test
    void testIndicator() {
        assertThat(PatientDetails.INDICATOR).isEqualTo("S07");
    }

    @Test
    void testGetPersonName() {
        final var details = new PatientDetails(List.of(
            "ignore me",
            "PNA+PAT+9435492908:OPI+++SU:KENNEDY+FO:SARAH+TI:MISS+MI:ANGELA",
            "ignore me"
        ));
        assertThat(details.getPersonName())
            .isNotNull()
            .extracting(PersonName::getValue)
            .isEqualTo("PAT+9435492908:OPI+++SU:KENNEDY+FO:SARAH+TI:MISS+MI:ANGELA");
    }

    @Test
    void testGetPersonDateOfBirth() {
        final var details = new PatientDetails(List.of(
            "ignore me",
            "DTM+329:19450730:102",
            "ignore me"
        ));
        assertThat(details.getPersonDateOfBirth())
            .isPresent()
            .map(PersonDateOfBirth::getValue)
            .contains("329:19450730:102");
    }

    @Test
    void testGetPersonSex() {
        final var details = new PatientDetails(List.of(
            "ignore me",
            "PDI+2",
            "ignore me"
        ));
        assertThat(details.getPersonSex())
            .isPresent()
            .map(PersonSex::getValue)
            .contains("2");
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void testLazyGettersWhenMissing() {
        final var details = new PatientDetails(List.of());
        assertAll(
            () -> assertThatThrownBy(details::getPersonName)
                .isExactlyInstanceOf(MissingSegmentException.class)
                .hasMessage("EDIFACT section is missing segment PNA+PAT"),
            () -> assertThat(details.getPersonDateOfBirth()).isEmpty(),
            () -> assertThat(details.getPersonSex()).isEmpty()
        );
    }
}
