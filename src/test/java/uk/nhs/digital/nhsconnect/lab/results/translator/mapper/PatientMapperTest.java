package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.DateFormat;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Gender;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.PersonDateOfBirth;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.PersonName;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.PersonSex;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.InvestigationSubject;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.PatientDetails;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.ServiceReportDetails;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

@SuppressWarnings("checkstyle:MagicNumber")
@ExtendWith(MockitoExtension.class)
class PatientMapperTest {
    private static final String TITLE = "MISS";
    private static final String FIRST_FORENAME = "SARAH";
    private static final String SECOND_FORENAME = "ANGELA";
    private static final String SURNAME = "KENNEDY";
    private static final String NHS_NUMBER = "1234";
    private static final String BIRTH_DATE_YY = "1991";
    private static final String BIRTH_DATE_YYMM = "1991-10";
    private static final String BIRTH_DATE_YYMMDD = "1999-12-28";

    @InjectMocks
    private PatientMapper patientMapper;

    @Mock
    private Message message;

    @Mock
    private ServiceReportDetails serviceReportDetails;

    @Mock
    private InvestigationSubject investigationSubject;

    @Mock
    private PatientDetails patientDetails;

    @Test
    void testMapMessageToPatientThrowsExceptionWhenServiceReportDetailsAreNull() {
        assertThatThrownBy(() -> patientMapper.mapToPatient(message))
            .isInstanceOf(FhirValidationException.class)
            .hasMessage("Unable to map message to patient details");
    }

    @Test
    void testMapMessageToPatientThrowsExceptionWhenInvestigationSubjectIsNull() {
        when(message.getServiceReportDetails()).thenReturn(serviceReportDetails);

        assertThatThrownBy(() -> patientMapper.mapToPatient(message))
            .isInstanceOf(FhirValidationException.class)
            .hasMessage("Unable to map message to patient details");
    }

    @Test
    void testMapMessageToPatientThrowsExceptionWhenPatientDetailsAreNull() {
        when(message.getServiceReportDetails()).thenReturn(serviceReportDetails);
        when(serviceReportDetails.getSubject()).thenReturn(investigationSubject);

        assertThatThrownBy(() -> patientMapper.mapToPatient(message))
            .isInstanceOf(FhirValidationException.class)
            .hasMessage("Unable to map message to patient details");
    }

    @Test
    void testMapMessageToPatient() {
        when(message.getServiceReportDetails()).thenReturn(serviceReportDetails);
        when(serviceReportDetails.getSubject()).thenReturn(investigationSubject);
        when(investigationSubject.getDetails()).thenReturn(patientDetails);

        stubPersonName();
        stubGender();
        stubDateOfBirth(BIRTH_DATE_YYMMDD, DateFormat.CCYYMMDD);

        final Patient patient = patientMapper.mapToPatient(message);

        assertAll("patient",

            () -> assertThat(patient).isNotNull(),

            () -> assertThat(patient.getIdentifier()).hasSize(1).first()
                .satisfies(identifier -> assertAll(
                    () -> assertThat(identifier.getValue()).isEqualTo(NHS_NUMBER),
                    () -> assertThat(identifier.getSystem()).isEqualTo(PatientMapper.NHS_NUMBER_SYSTEM))),

            () -> assertThat(patient.getName()).hasSize(1).first()
                .satisfies(name -> assertAll(
                    () -> assertThat(name.getPrefixAsSingleString()).isEqualTo(TITLE),
                    () -> assertThat(name.getGivenAsSingleString()).isEqualTo(FIRST_FORENAME + " " + SECOND_FORENAME),
                    () -> assertThat(name.getFamily()).isEqualTo(SURNAME))),

            () -> assertThat(patient.getGender()).isEqualTo(Enumerations.AdministrativeGender.FEMALE),

            () -> {
                final Date birthDate = Date.from(LocalDate.of(1999, 12, 28).atStartOfDay()
                    .atZone(ZoneId.systemDefault()).toInstant());
                assertThat(patient.getBirthDate()).isEqualTo(birthDate);
            });
    }

    @Test
    void testMapMessageToPatientDoesNotMapDetailsWhenNull() {
        when(message.getServiceReportDetails()).thenReturn(serviceReportDetails);
        when(serviceReportDetails.getSubject()).thenReturn(investigationSubject);
        when(investigationSubject.getDetails()).thenReturn(patientDetails);

        final Patient patient = patientMapper.mapToPatient(message);

        assertAll("patient",
            () -> assertThat(patient).isNotNull(),
            () -> assertThat(patient.getIdentifier()).isEmpty(),
            () -> assertThat(patient.getName()).isEmpty(),
            () -> assertThat(patient.getGender()).isNull(),
            () -> assertThat(patient.getBirthDate()).isNull());
    }

    @Test
    void testMapMessageToPatientWithBirthDateYearOnly() {
        when(message.getServiceReportDetails()).thenReturn(serviceReportDetails);
        when(serviceReportDetails.getSubject()).thenReturn(investigationSubject);
        when(investigationSubject.getDetails()).thenReturn(patientDetails);

        stubDateOfBirth(BIRTH_DATE_YY, DateFormat.CCYY);

        final Patient patient = patientMapper.mapToPatient(message);

        assertAll("patient",
            () -> assertThat(patient).isNotNull(),
            () -> assertThat(patient.getIdentifier()).isEmpty(),
            () -> assertThat(patient.getName()).isEmpty(),
            () -> assertThat(patient.getGender()).isNull(),

            () -> {
                final Date birthDate = Date.from(LocalDate.of(1991, 1, 1).atStartOfDay()
                    .atZone(ZoneId.systemDefault()).toInstant());
                assertThat(patient.getBirthDate()).isEqualTo(birthDate);
            });
    }

    @Test
    void testMapMessageToPatientWithBirthDateYearMonthOnly() {
        when(message.getServiceReportDetails()).thenReturn(serviceReportDetails);
        when(serviceReportDetails.getSubject()).thenReturn(investigationSubject);
        when(investigationSubject.getDetails()).thenReturn(patientDetails);

        stubDateOfBirth(BIRTH_DATE_YYMM, DateFormat.CCYYMM);

        final Patient patient = patientMapper.mapToPatient(message);

        assertAll("patient",
            () -> assertThat(patient).isNotNull(),
            () -> assertThat(patient.getIdentifier()).isEmpty(),
            () -> assertThat(patient.getName()).isEmpty(),
            () -> assertThat(patient.getGender()).isNull(),

            () -> {
                final Date birthDate = Date.from(LocalDate.of(1991, 10, 1).atStartOfDay()
                    .atZone(ZoneId.systemDefault()).toInstant());
                assertThat(patient.getBirthDate()).isEqualTo(birthDate);
            });
    }

    private void stubDateOfBirth(final String dateOfBirth, final DateFormat dateFormat) {
        final Optional<PersonDateOfBirth> personDateOfBirth = Optional.of(PersonDateOfBirth.builder()
            .dateOfBirth(dateOfBirth)
            .dateFormat(dateFormat)
            .build());
        when(patientDetails.getDateOfBirth()).thenReturn(personDateOfBirth);
    }

    private void stubPersonName() {
        final PersonName personName = PersonName.builder()
            .title(TITLE)
            .firstForename(FIRST_FORENAME)
            .secondForename(SECOND_FORENAME)
            .surname(SURNAME)
            .nhsNumber(NHS_NUMBER)
            .build();
        when(patientDetails.getName()).thenReturn(personName);
    }

    private void stubGender() {
        final Optional<PersonSex> personSex = Optional.of(PersonSex.builder()
            .gender(Gender.FEMALE)
            .build());
        when(patientDetails.getSex()).thenReturn(personSex);
    }

}
