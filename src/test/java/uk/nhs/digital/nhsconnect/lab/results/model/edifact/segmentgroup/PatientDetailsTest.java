package uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.DateFormat;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Gender;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.PatientIdentificationType;
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
        var name = assertThat(details.getName())
                .isNotNull();

        name.extracting(PersonName::getNhsNumber).isEqualTo("9435492908");
        name.extracting(PersonName::getFirstForename).isEqualTo("SARAH");
        name.extracting(PersonName::getSecondForename).isEqualTo("ANGELA");
        name.extracting(PersonName::getSurname).isEqualTo("KENNEDY");
        name.extracting(PersonName::getTitle).isEqualTo("MISS");
        name.extracting(PersonName::getPatientIdentificationType)
                .isEqualTo(PatientIdentificationType.OFFICIAL_PATIENT_IDENTIFICATION);
    }

    @Test
    void testGetPersonDateOfBirth() {
        final var details = new PatientDetails(List.of(
                "ignore me",
                "DTM+329:19450730:102",
                "ignore me"
        ));
        var dateOfBirth = assertThat(details.getDateOfBirth()).isPresent();
        dateOfBirth.map(PersonDateOfBirth::getDateOfBirth).hasValue("1945-07-30");
        dateOfBirth.map(PersonDateOfBirth::getDateFormat).hasValue(DateFormat.CCYYMMDD);
    }

    @Test
    void testGetPersonSex() {
        final var details = new PatientDetails(List.of(
                "ignore me",
                "PDI+2",
                "ignore me"
        ));
        assertThat(details.getSex())
                .isPresent()
                .map(PersonSex::getGender)
                .contains(Gender.FEMALE);
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void testLazyGettersWhenMissing() {
        final var details = new PatientDetails(List.of());
        assertAll(
                () -> assertThatThrownBy(details::getName)
                        .isExactlyInstanceOf(MissingSegmentException.class)
                        .hasMessage("EDIFACT section is missing segment PNA+PAT"),
                () -> assertThat(details.getDateOfBirth()).isEmpty(),
                () -> assertThat(details.getSex()).isEmpty()
        );
    }
}
