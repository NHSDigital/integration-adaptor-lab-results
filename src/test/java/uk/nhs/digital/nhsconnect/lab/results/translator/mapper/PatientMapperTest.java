package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.StringType;
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
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.UnstructuredAddress;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.InvestigationSubject;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.PatientDetails;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.ServiceReportDetails;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
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
    private static final String UUID = "some-uuid";
    private static final String BIRTH_DATE_FULL = "1991-10-28";
    private static final String BIRTH_DATE_YEAR_AND_MONTH = "1991-10";
    private static final String BIRTH_DATE_YEAR_ONLY = "1991";
    private static final LocalDate LOCAL_DATE_FULL = LocalDate.of(1991, 10, 28);
    private static final LocalDate LOCAL_DATE_FOR_YEAR_AND_MONTH = LocalDate.of(1991, 10, 1);
    private static final LocalDate LOCAL_DATE_FOR_YEAR_ONLY = LocalDate.of(1991, 1, 1);
    private static final String POSTCODE = "POST CODE";
    private static final String ADDRESS_LINE_1 = "LINE1";
    private static final String ADDRESS_LINE_2 = "LINE2";
    private static final String ADDRESS_LINE_3 = "LINE3";
    private static final String ADDRESS_LINE_4 = "LINE4";
    private static final String ADDRESS_LINE_5 = "LINE5";

    @InjectMocks
    private PatientMapper patientMapper;
    @Mock
    private UUIDGenerator uuidGenerator;
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
        when(uuidGenerator.generateUUID()).thenReturn(UUID);

        final PersonName personName = PersonName.builder()
            .title(TITLE)
            .firstForename(FIRST_FORENAME)
            .secondForename(SECOND_FORENAME)
            .surname(SURNAME)
            .nhsNumber(NHS_NUMBER)
            .build();
        when(patientDetails.getName()).thenReturn(personName);

        final Optional<PersonSex> personSex = Optional.of(PersonSex.builder()
            .gender(Gender.FEMALE)
            .build());
        when(patientDetails.getSex()).thenReturn(personSex);

        stubDateOfBirth(BIRTH_DATE_FULL, DateFormat.CCYYMMDD);

        final String[] addressLines = {ADDRESS_LINE_1, ADDRESS_LINE_2, ADDRESS_LINE_3, ADDRESS_LINE_4, ADDRESS_LINE_5};
        stubAddress(addressLines);

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
                final Date birthDate = Date.from(LOCAL_DATE_FULL.atStartOfDay()
                    .atZone(ZoneId.systemDefault()).toInstant());
                assertThat(patient.getBirthDate()).isEqualTo(birthDate);
            },

            () -> assertThat(patient.getAddress()).hasSize(1).first()
                .satisfies(address -> assertAll(
                    () -> {
                        final List<StringType> lines = List.of(new StringType(ADDRESS_LINE_1),
                            new StringType(ADDRESS_LINE_2), new StringType(ADDRESS_LINE_3),
                            new StringType(ADDRESS_LINE_4), new StringType(ADDRESS_LINE_5));

                        assertThat(address.getLine()).usingRecursiveComparison().isEqualTo(lines);
                    },
                    () -> assertThat(address.getPostalCode()).isEqualTo(POSTCODE)))
        );
    }

    @Test
    void testMapMessageToPatientDoesNotMapGivenNameIfFirstForenameIsNull() {
        when(message.getServiceReportDetails()).thenReturn(serviceReportDetails);
        when(serviceReportDetails.getSubject()).thenReturn(investigationSubject);
        when(investigationSubject.getDetails()).thenReturn(patientDetails);
        when(uuidGenerator.generateUUID()).thenReturn(UUID);

        final PersonName personName = PersonName.builder()
            .title(TITLE)
            .secondForename(SECOND_FORENAME)
            .surname(SURNAME)
            .build();
        when(patientDetails.getName()).thenReturn(personName);

        final Patient patient = patientMapper.mapToPatient(message);

        assertAll("patient",
            () -> assertThat(patient).isNotNull(),
            () -> assertThat(patient.getName()).hasSize(1).first()
                .satisfies(name -> assertAll(
                    () -> assertThat(name.getPrefixAsSingleString()).isEqualTo(TITLE),
                    () -> assertThat(name.getGivenAsSingleString()).isEmpty(),
                    () -> assertThat(name.getFamily()).isEqualTo(SURNAME))));
    }

    @Test
    void testMapMessageToPatientMapsGivenNameIfSecondForenameIsNull() {
        when(message.getServiceReportDetails()).thenReturn(serviceReportDetails);
        when(serviceReportDetails.getSubject()).thenReturn(investigationSubject);
        when(investigationSubject.getDetails()).thenReturn(patientDetails);
        when(uuidGenerator.generateUUID()).thenReturn(UUID);

        final PersonName personName = PersonName.builder()
            .title(TITLE)
            .firstForename(FIRST_FORENAME)
            .surname(SURNAME)
            .build();
        when(patientDetails.getName()).thenReturn(personName);

        final Patient patient = patientMapper.mapToPatient(message);

        assertAll("patient",
            () -> assertThat(patient).isNotNull(),
            () -> assertThat(patient.getName()).hasSize(1).first()
                .satisfies(name -> assertAll(
                    () -> assertThat(name.getPrefixAsSingleString()).isEqualTo(TITLE),
                    () -> assertThat(name.getGivenAsSingleString()).isEqualTo(FIRST_FORENAME),
                    () -> assertThat(name.getFamily()).isEqualTo(SURNAME))));
    }

    @Test
    void testMapMessageToPatientDoesNotMapDetailsWhenNull() {
        when(message.getServiceReportDetails()).thenReturn(serviceReportDetails);
        when(serviceReportDetails.getSubject()).thenReturn(investigationSubject);
        when(investigationSubject.getDetails()).thenReturn(patientDetails);
        when(uuidGenerator.generateUUID()).thenReturn(UUID);

        final Patient patient = patientMapper.mapToPatient(message);

        assertAll("patient",
            () -> assertThat(patient).isNotNull(),
            () -> assertThat(patient.getIdentifier()).isEmpty(),
            () -> assertThat(patient.getName()).isEmpty(),
            () -> assertThat(patient.getGender()).isNull(),
            () -> assertThat(patient.getBirthDate()).isNull());
    }

    @Test
    void testMapMessageToPatientWithBirthDateIsYearOnly() {
        when(message.getServiceReportDetails()).thenReturn(serviceReportDetails);
        when(serviceReportDetails.getSubject()).thenReturn(investigationSubject);
        when(investigationSubject.getDetails()).thenReturn(patientDetails);
        when(uuidGenerator.generateUUID()).thenReturn(UUID);

        stubDateOfBirth(BIRTH_DATE_YEAR_ONLY, DateFormat.CCYY);

        final Patient patient = patientMapper.mapToPatient(message);

        assertAll("patient",
            () -> assertThat(patient).isNotNull(),
            () -> assertThat(patient.getIdentifier()).isEmpty(),
            () -> assertThat(patient.getName()).isEmpty(),
            () -> assertThat(patient.getGender()).isNull(),

            () -> {
                final Date birthDate = Date.from(LOCAL_DATE_FOR_YEAR_ONLY.atStartOfDay()
                    .atZone(ZoneId.systemDefault()).toInstant());
                assertThat(patient.getBirthDate()).isEqualTo(birthDate);
            });
    }

    @Test
    void testMapMessageToPatientWithBirthDateYearMonthOnly() {
        when(message.getServiceReportDetails()).thenReturn(serviceReportDetails);
        when(serviceReportDetails.getSubject()).thenReturn(investigationSubject);
        when(investigationSubject.getDetails()).thenReturn(patientDetails);
        when(uuidGenerator.generateUUID()).thenReturn(UUID);

        stubDateOfBirth(BIRTH_DATE_YEAR_AND_MONTH, DateFormat.CCYYMM);

        final Patient patient = patientMapper.mapToPatient(message);

        assertAll("patient",
            () -> assertThat(patient).isNotNull(),
            () -> assertThat(patient.getIdentifier()).isEmpty(),
            () -> assertThat(patient.getName()).isEmpty(),
            () -> assertThat(patient.getGender()).isNull(),

            () -> {
                final Date birthDate = Date.from(LOCAL_DATE_FOR_YEAR_AND_MONTH.atStartOfDay()
                    .atZone(ZoneId.systemDefault()).toInstant());
                assertThat(patient.getBirthDate()).isEqualTo(birthDate);
            });
    }

    @Test
    void testMapMessageToPatientDoesNotMapAddressWhenNull() {
        when(message.getServiceReportDetails()).thenReturn(serviceReportDetails);
        when(serviceReportDetails.getSubject()).thenReturn(investigationSubject);
        when(investigationSubject.getDetails()).thenReturn(patientDetails);
        when(uuidGenerator.generateUUID()).thenReturn(UUID);

        final PersonName personName = PersonName.builder()
            .title(TITLE)
            .firstForename(FIRST_FORENAME)
            .secondForename(SECOND_FORENAME)
            .surname(SURNAME)
            .nhsNumber(NHS_NUMBER)
            .build();
        when(patientDetails.getName()).thenReturn(personName);

        final Patient patient = patientMapper.mapToPatient(message);

        assertAll("patient",
            () -> assertThat(patient).isNotNull(),
            () -> assertThat(patient.getName()).hasSize(1).first()
                .satisfies(name -> assertAll(
                    () -> assertThat(name.getPrefixAsSingleString()).isEqualTo(TITLE),
                    () -> assertThat(name.getGivenAsSingleString()).isEqualTo(FIRST_FORENAME + " " + SECOND_FORENAME),
                    () -> assertThat(name.getFamily()).isEqualTo(SURNAME))),

            () -> assertThat(patient.getAddress()).isEmpty());
    }

    @Test
    void testMapMessageToPatientDoesNotMapAddressLinesIsNull() {
        when(message.getServiceReportDetails()).thenReturn(serviceReportDetails);
        when(serviceReportDetails.getSubject()).thenReturn(investigationSubject);
        when(investigationSubject.getDetails()).thenReturn(patientDetails);
        when(uuidGenerator.generateUUID()).thenReturn(UUID);

        final PersonName personName = PersonName.builder()
            .title(TITLE)
            .firstForename(FIRST_FORENAME)
            .surname(SURNAME)
            .build();
        when(patientDetails.getName()).thenReturn(personName);

        stubAddress(null);

        final Patient patient = patientMapper.mapToPatient(message);

        assertAll("patient",
            () -> assertThat(patient).isNotNull(),

            () -> assertThat(patient.getName()).hasSize(1).first()
                .satisfies(name -> assertAll(
                    () -> assertThat(name.getPrefixAsSingleString()).isEqualTo(TITLE),
                    () -> assertThat(name.getGivenAsSingleString()).isEqualTo(FIRST_FORENAME),
                    () -> assertThat(name.getFamily()).isEqualTo(SURNAME))),

            () -> assertThat(patient.getAddress()).hasSize(1).first()
                .satisfies(address -> assertAll(
                    () -> assertThat(address.getLine()).isEmpty(),
                    () -> assertThat(address.getPostalCode()).isEqualTo(POSTCODE)))
        );
    }

    private void stubDateOfBirth(final String dateOfBirth, final DateFormat dateFormat) {
        final Optional<PersonDateOfBirth> personDateOfBirth = Optional.of(PersonDateOfBirth.builder()
            .dateOfBirth(dateOfBirth)
            .dateFormat(dateFormat)
            .build());
        when(patientDetails.getDateOfBirth()).thenReturn(personDateOfBirth);
    }

    private void stubAddress(String[] addressLines) {
        final UnstructuredAddress unstructuredAddress = new UnstructuredAddress("", addressLines, POSTCODE);
        when(investigationSubject.getAddress()).thenReturn(Optional.of(unstructuredAddress));
    }

}
