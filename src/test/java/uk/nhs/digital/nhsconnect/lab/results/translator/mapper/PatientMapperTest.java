package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.StringType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Reference;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.DateFormat;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.Gender;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@SuppressWarnings("checkstyle:MagicNumber")
@ExtendWith(MockitoExtension.class)
class PatientMapperTest {
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
    @Mock
    private Reference reference;

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
        when(uuidGenerator.generateUUID()).thenReturn("some-uuid");

        final PersonName personName = PersonName.builder()
            .title("MISS")
            .firstForename("SARAH")
            .secondForename("ANGELA")
            .surname("KENNEDY")
            .nhsNumber("1234")
            .build();
        when(patientDetails.getName()).thenReturn(personName);

        final Optional<PersonSex> personSex = Optional.of(PersonSex.builder()
            .gender(Gender.FEMALE)
            .build());
        when(patientDetails.getSex()).thenReturn(personSex);

        final Optional<PersonDateOfBirth> personDateOfBirth = Optional.of(PersonDateOfBirth.builder()
            .dateOfBirth("1991-10-28")
            .dateFormat(DateFormat.CCYYMMDD)
            .build());
        when(patientDetails.getDateOfBirth()).thenReturn(personDateOfBirth);

        final String[] addressLines = {"LINE1", "LINE2", "LINE3", "LINE4", "LINE5"};
        final UnstructuredAddress unstructuredAddress = new UnstructuredAddress("", addressLines, "POST CODE");
        when(investigationSubject.getAddress()).thenReturn(Optional.of(unstructuredAddress));

        final Patient patient = patientMapper.mapToPatient(message);

        assertAll("patient",
            () -> assertThat(patient).isNotNull(),
            () -> assertThat(patient.getId()).isEqualTo("some-uuid"),
            () -> assertThat(patient.getIdentifier()).hasSize(1).first()
                .satisfies(identifier -> assertAll(
                    () -> assertThat(identifier.getId()).isNull(),
                    () -> assertThat(identifier.getValue()).isEqualTo("1234"),
                    () -> assertThat(identifier.getSystem()).isEqualTo(PatientMapper.NHS_NUMBER_SYSTEM))),

            () -> assertThat(patient.getName()).hasSize(1).first()
                .satisfies(name -> assertAll(
                    () -> assertThat(name.getPrefixAsSingleString()).isEqualTo("MISS"),
                    () -> assertThat(name.getGivenAsSingleString()).isEqualTo("SARAH ANGELA"),
                    () -> assertThat(name.getFamily()).isEqualTo("KENNEDY"))),

            () -> assertThat(patient.getGender()).isEqualTo(Enumerations.AdministrativeGender.FEMALE),

            () -> {
                final Date birthDate = Date.from(LocalDate.of(1991, 10, 28).atStartOfDay()
                    .atZone(ZoneId.systemDefault()).toInstant());
                assertThat(patient.getBirthDate()).isEqualTo(birthDate);
            },

            () -> assertThat(patient.getAddress()).hasSize(1).first()
                .satisfies(address -> assertAll(
                    () -> {
                        final List<StringType> lines = List.of(new StringType("LINE1"),
                            new StringType("LINE2"), new StringType("LINE3"),
                            new StringType("LINE4"), new StringType("LINE5"));

                        assertThat(address.getLine()).usingRecursiveComparison().isEqualTo(lines);
                    },
                    () -> assertThat(address.getPostalCode()).isEqualTo("POST CODE")))
        );
    }

    @Test
    void testMapMessageToPatientDoesNotMapGivenNameIfFirstForenameIsNull() {
        when(message.getServiceReportDetails()).thenReturn(serviceReportDetails);
        when(serviceReportDetails.getSubject()).thenReturn(investigationSubject);
        when(investigationSubject.getDetails()).thenReturn(patientDetails);
        when(uuidGenerator.generateUUID()).thenReturn("some-uuid");

        final PersonName personName = PersonName.builder()
            .title("MISS")
            .secondForename("ANGELA")
            .surname("KENNEDY")
            .build();
        when(patientDetails.getName()).thenReturn(personName);

        final Patient patient = patientMapper.mapToPatient(message);

        assertAll("patient",
            () -> assertThat(patient).isNotNull(),
            () -> assertThat(patient.getId()).isEqualTo("some-uuid"),
            () -> assertThat(patient.getIdentifier()).isEmpty(),
            () -> assertThat(patient.getName()).hasSize(1).first()
                .satisfies(name -> assertAll(
                    () -> assertThat(name.getPrefixAsSingleString()).isEqualTo("MISS"),
                    () -> assertThat(name.getGivenAsSingleString()).isEmpty(),
                    () -> assertThat(name.getFamily()).isEqualTo("KENNEDY"))),
            () -> assertThat(patient.getGender()).isNull(),
            () -> assertThat(patient.getBirthDate()).isNull(),
            () -> assertThat(patient.getAddress()).isEmpty());
    }

    @Test
    void testMapMessageToPatientMapsGivenNameIfSecondForenameIsNull() {
        when(message.getServiceReportDetails()).thenReturn(serviceReportDetails);
        when(serviceReportDetails.getSubject()).thenReturn(investigationSubject);
        when(investigationSubject.getDetails()).thenReturn(patientDetails);
        when(uuidGenerator.generateUUID()).thenReturn("some-uuid");

        final PersonName personName = PersonName.builder()
            .title("MISS")
            .firstForename("SARAH")
            .surname("KENNEDY")
            .build();
        when(patientDetails.getName()).thenReturn(personName);

        final Patient patient = patientMapper.mapToPatient(message);

        assertAll("patient",
            () -> assertThat(patient).isNotNull(),
            () -> assertThat(patient.getId()).isEqualTo("some-uuid"),
            () -> assertThat(patient.getIdentifier()).isEmpty(),
            () -> assertThat(patient.getName()).hasSize(1).first()
                .satisfies(name -> assertAll(
                    () -> assertThat(name.getPrefixAsSingleString()).isEqualTo("MISS"),
                    () -> assertThat(name.getGivenAsSingleString()).isEqualTo("SARAH"),
                    () -> assertThat(name.getFamily()).isEqualTo("KENNEDY"))),
            () -> assertThat(patient.getGender()).isNull(),
            () -> assertThat(patient.getBirthDate()).isNull(),
            () -> assertThat(patient.getAddress()).isEmpty());
    }

    @Test
    void testMapMessageToPatientDoesNotMapDetailsWhenNull() {
        when(message.getServiceReportDetails()).thenReturn(serviceReportDetails);
        when(serviceReportDetails.getSubject()).thenReturn(investigationSubject);
        when(investigationSubject.getDetails()).thenReturn(patientDetails);
        when(uuidGenerator.generateUUID()).thenReturn("some-uuid");
        when(patientDetails.getName()).thenReturn(PersonName.builder().build());

        final Patient patient = patientMapper.mapToPatient(message);

        assertAll("patient",
            () -> assertThat(patient).isNotNull(),
            () -> assertThat(patient.getId()).isEqualTo("some-uuid"),
            () -> assertThat(patient.getIdentifier()).isEmpty(),
            () -> assertThat(patient.getName()).isNotEmpty(),
            () -> assertThat(patient.getGender()).isNull(),
            () -> assertThat(patient.getBirthDate()).isNull(),
            () -> assertThat(patient.getAddress()).isEmpty());
    }

    @Test
    void testMapMessageToPatientWhenBirthDateIsYearOnly() {
        when(message.getServiceReportDetails()).thenReturn(serviceReportDetails);
        when(serviceReportDetails.getSubject()).thenReturn(investigationSubject);
        when(investigationSubject.getDetails()).thenReturn(patientDetails);
        when(uuidGenerator.generateUUID()).thenReturn("some-uuid");
        when(patientDetails.getName()).thenReturn(PersonName.builder().build());

        final Optional<PersonDateOfBirth> personDateOfBirth = Optional.of(PersonDateOfBirth.builder()
            .dateOfBirth("1991")
            .dateFormat(DateFormat.CCYY)
            .build());
        when(patientDetails.getDateOfBirth()).thenReturn(personDateOfBirth);

        final Patient patient = patientMapper.mapToPatient(message);

        assertAll("patient",
            () -> assertThat(patient).isNotNull(),
            () -> assertThat(patient.getId()).isEqualTo("some-uuid"),
            () -> assertThat(patient.getIdentifier()).isEmpty(),
            () -> assertThat(patient.getName()).isNotEmpty(),
            () -> assertThat(patient.getGender()).isNull(),
            () -> {
                final Date birthDate = Date.from(LocalDate.of(1991, 1, 1).atStartOfDay()
                    .atZone(ZoneId.systemDefault()).toInstant());
                assertThat(patient.getBirthDate()).isEqualTo(birthDate);
            },
            () -> assertThat(patient.getAddress()).isEmpty());
    }

    @Test
    void testMapMessageToPatientWhenBirthDateYearMonthOnly() {
        when(message.getServiceReportDetails()).thenReturn(serviceReportDetails);
        when(serviceReportDetails.getSubject()).thenReturn(investigationSubject);
        when(investigationSubject.getDetails()).thenReturn(patientDetails);
        when(uuidGenerator.generateUUID()).thenReturn("some-uuid");
        when(patientDetails.getName()).thenReturn(PersonName.builder().build());

        final Optional<PersonDateOfBirth> personDateOfBirth = Optional.of(PersonDateOfBirth.builder()
            .dateOfBirth("1991-10")
            .dateFormat(DateFormat.CCYYMM)
            .build());
        when(patientDetails.getDateOfBirth()).thenReturn(personDateOfBirth);

        final Patient patient = patientMapper.mapToPatient(message);

        assertAll("patient",
            () -> assertThat(patient).isNotNull(),
            () -> assertThat(patient.getId()).isEqualTo("some-uuid"),
            () -> assertThat(patient.getIdentifier()).isEmpty(),
            () -> assertThat(patient.getName()).isNotEmpty(),
            () -> assertThat(patient.getGender()).isNull(),

            () -> {
                final Date birthDate = Date.from(LocalDate.of(1991, 10, 1).atStartOfDay()
                    .atZone(ZoneId.systemDefault()).toInstant());
                assertThat(patient.getBirthDate()).isEqualTo(birthDate);
            },
            () -> assertThat(patient.getAddress()).isEmpty());
    }

    @Test
    void testMapMessageToPatientDoesNotMapAddressLinesWhenNull() {
        when(message.getServiceReportDetails()).thenReturn(serviceReportDetails);
        when(serviceReportDetails.getSubject()).thenReturn(investigationSubject);
        when(investigationSubject.getDetails()).thenReturn(patientDetails);
        when(uuidGenerator.generateUUID()).thenReturn("some-uuid");

        when(patientDetails.getName()).thenReturn(PersonName.builder().build());

        final UnstructuredAddress unstructuredAddress = new UnstructuredAddress("", null, "POST CODE");
        when(investigationSubject.getAddress()).thenReturn(Optional.of(unstructuredAddress));

        final Patient patient = patientMapper.mapToPatient(message);

        assertAll("patient",
            () -> assertThat(patient).isNotNull(),
            () -> assertThat(patient.getId()).isEqualTo("some-uuid"),
            () -> assertThat(patient.getIdentifier()).isEmpty(),
            () -> assertThat(patient.getName()).isNotEmpty(),
            () -> assertThat(patient.getGender()).isNull(),
            () -> assertThat(patient.getBirthDate()).isNull(),
            () -> assertThat(patient.getAddress()).hasSize(1).first()
                .satisfies(address -> assertAll(
                    () -> assertThat(address.getLine()).isEmpty(),
                    () -> assertThat(address.getPostalCode()).isEqualTo("POST CODE")))
        );
    }

    @Test
    void testMapMessageToPatientMapsOtherIdentifierWhenNhsNumberIsNull() {
        when(message.getServiceReportDetails()).thenReturn(serviceReportDetails);
        when(serviceReportDetails.getSubject()).thenReturn(investigationSubject);
        when(investigationSubject.getDetails()).thenReturn(patientDetails);
        when(investigationSubject.getReferenceServiceSubject()).thenReturn(Optional.of(reference));
        when(reference.getNumber()).thenReturn("123");
        when(uuidGenerator.generateUUID()).thenReturn("some-uuid");
        when(patientDetails.getName()).thenReturn(PersonName.builder().build());

        final Patient patient = patientMapper.mapToPatient(message);

        assertAll("patient",
            () -> assertThat(patient).isNotNull(),
            () -> assertThat(patient.getId()).isEqualTo("some-uuid"),
            () -> assertThat(patient.getIdentifier()).hasSize(1).first()
                .satisfies(identifier -> assertAll(
                    () -> assertThat(identifier.getId()).isEqualTo("123"),
                    () -> assertThat(identifier.getValue()).isNull(),
                    () -> assertThat(identifier.getSystem()).isNull())),

            () -> assertThat(patient.getName()).isNotEmpty(),
            () -> assertThat(patient.getGender()).isNull(),
            () -> assertThat(patient.getBirthDate()).isNull(),
            () -> assertThat(patient.getAddress()).isEmpty());
    }

    @Test
    void testMapMessageToPatientMapsOnlyNhsNumberWhenBothNhsNumberAndReferenceExists() {
        when(message.getServiceReportDetails()).thenReturn(serviceReportDetails);
        when(serviceReportDetails.getSubject()).thenReturn(investigationSubject);
        when(investigationSubject.getDetails()).thenReturn(patientDetails);
        lenient().when(investigationSubject.getReferenceServiceSubject()).thenReturn(Optional.of(reference));
        lenient().when(reference.getNumber()).thenReturn("1111");
        when(uuidGenerator.generateUUID()).thenReturn("some-uuid");

        final PersonName personName = PersonName.builder()
            .nhsNumber("1234")
            .build();
        when(patientDetails.getName()).thenReturn(personName);

        final Patient patient = patientMapper.mapToPatient(message);

        assertAll("patient",
            () -> assertThat(patient).isNotNull(),
            () -> assertThat(patient.getId()).isEqualTo("some-uuid"),
            () -> assertThat(patient.getIdentifier()).hasSize(1).first()
                .satisfies(identifier -> assertAll(
                    () -> assertThat(identifier.getId()).isNull(),
                    () -> assertThat(identifier.getValue()).isEqualTo("1234"),
                    () -> assertThat(identifier.getSystem()).isEqualTo(PatientMapper.NHS_NUMBER_SYSTEM))),

            () -> assertThat(patient.getName()).isNotEmpty(),
            () -> assertThat(patient.getGender()).isNull(),
            () -> assertThat(patient.getBirthDate()).isNull(),
            () -> assertThat(patient.getAddress()).isEmpty());
    }

}
