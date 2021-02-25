package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Specimen;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Date;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.nhs.digital.nhsconnect.lab.results.fixtures.FhirFixtures.generatePatient;
import static uk.nhs.digital.nhsconnect.lab.results.fixtures.FhirFixtures.generateRequester;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("checkstyle:MagicNumber")
class PathologyRecordMapperTest {

    private static final String NAME_TEXT = "Dr Bob Hope";
    private static final String BIRTH_DATE = "2001-01-12";
    private static final AdministrativeGender GENDER = AdministrativeGender.MALE;

    @Mock
    private PractitionerMapper practitionerMapper;

    @Mock
    private PatientMapper patientMapper;

    @Mock
    private SpecimenMapper specimenMapper;

    @InjectMocks
    private PathologyRecordMapper pathologyRecordMapper;

    @Test
    @SuppressWarnings("checkstyle:MagicNumber")
    void testMapMessageToPathologyRecordWithPractitioner() {
        final Message message = new Message(emptyList());

        when(practitionerMapper.mapRequester(message))
            .thenReturn(Optional.of(generateRequester(NAME_TEXT, GENDER)));

        final var pathologyRecord = pathologyRecordMapper.mapToPathologyRecord(message);

        final Practitioner requester = pathologyRecord.getRequester();
        assertAll(
            () -> assertThat(requester.getName())
                .hasSize(1)
                .first()
                .extracting(HumanName::getText)
                .isEqualTo(NAME_TEXT),
            () -> assertThat(requester.getGender()).isEqualTo(GENDER)
        );
    }

    @Test
    void testMapMessageToPathologyRecordWithPatient() {
        final Message message = new Message(emptyList());
        when(practitionerMapper.mapRequester(message)).thenReturn(Optional.empty());
        when(patientMapper.mapToPatient(message)).thenReturn(generatePatient(NAME_TEXT, GENDER, BIRTH_DATE));

        final var pathologyRecord = pathologyRecordMapper.mapToPathologyRecord(message);

        final Patient patient = pathologyRecord.getPatient();
        assertAll(
            () -> assertThat(patient.getName())
                .hasSize(1)
                .first()
                .extracting(HumanName::getText)
                .isEqualTo(NAME_TEXT),
            () -> assertThat(patient.getGender()).isEqualTo(GENDER),
            () -> assertThat(patient.getBirthDate())
                .isEqualTo(Date.from(LocalDate.of(2001, 1, 12).atStartOfDay()
                    .atZone(ZoneId.systemDefault()).toInstant()))
        );
    }

    @Test
    void testMapMessageToPathologyRecordWithSpecimens() {
        final Message message = new Message(emptyList());

        when(practitionerMapper.mapRequester(message)).thenReturn(Optional.empty());
        final var mockSpecimen1 = mock(Specimen.class);
        final var mockSpecimen2 = mock(Specimen.class);
        when(specimenMapper.mapToSpecimens(message)).thenReturn(List.of(mockSpecimen1, mockSpecimen2));

        final var pathologyRecord = pathologyRecordMapper.mapToPathologyRecord(message);

        assertThat(pathologyRecord.getSpecimens())
            .hasSize(2)
            .contains(mockSpecimen1, mockSpecimen2);
    }
}
