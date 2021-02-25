package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.fhir.PathologyRecord;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;
import static uk.nhs.digital.nhsconnect.lab.results.fixtures.FhirFixtures.generatePatient;
import static uk.nhs.digital.nhsconnect.lab.results.fixtures.FhirFixtures.generateRequester;
import static uk.nhs.digital.nhsconnect.lab.results.fixtures.FhirFixtures.generatePractitioner;

@ExtendWith(MockitoExtension.class)
class PathologyRecordMapperTest {

    private static final String NAME_TEXT = "Dr Bob Hope";
    private static final String BIRTH_DATE = "2001-01-12";
    private static final AdministrativeGender GENDER = AdministrativeGender.MALE;

    @Mock
    private PractitionerMapper practitionerMapper;

    @Mock
    private PatientMapper patientMapper;

    @InjectMocks
    private PathologyRecordMapper pathologyRecordMapper;

    @Test
    @SuppressWarnings("checkstyle:MagicNumber")
    void testMapMessageToPathologyRecord() {
        final Message message = new Message(new ArrayList<>());

        when(practitionerMapper.mapRequester(message)).thenReturn(Optional.of(generateRequester(NAME_TEXT, GENDER)));
        when(patientMapper.mapToPatient(message)).thenReturn(generatePatient(NAME_TEXT, GENDER, BIRTH_DATE));
        when(practitionerMapper.mapRequester(message)).thenReturn(
                Optional.of(generatePractitioner("Dr Bob Hope", AdministrativeGender.MALE, "id-1"))
        );

        when(practitionerMapper.mapPerformer(message)).thenReturn(
                Optional.of(generatePractitioner("Dr Darcy Lewis", AdministrativeGender.FEMALE, "id-2"))
        );

        final PathologyRecord pathologyRecord = pathologyRecordMapper.mapToPathologyRecord(message);

        final Practitioner requester = pathologyRecord.getRequester();
        Practitioner requester = pathologyRecord.getRequester();
        Practitioner performer = pathologyRecord.getPerformer();

        assertAll(
            () -> assertThat(requester.getName())
                .hasSize(1)
                .first()
                .extracting(HumanName::getText)
                .isEqualTo(NAME_TEXT),
            () -> assertThat(requester.getGender()).isEqualTo(GENDER)
        );

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
                    .hasSize(1)
                    .first()
                    .extracting(HumanName::getText)
                    .isEqualTo("Dr Bob Hope"),
            () -> assertThat(requester.getGender().toCode())
                    .isEqualTo("male"),
            () -> assertThat(requester.getId())
                    .isEqualTo("id-1"),
            () -> assertThat(performer.getName())
                    .hasSize(1)
                    .first()
                    .extracting(HumanName::getText)
                    .isEqualTo("Dr Darcy Lewis"),
            () -> assertThat(performer.getGender().toCode())
                    .isEqualTo("female"),
            () -> assertThat(performer.getId())
                    .isEqualTo("id-2")
        );
    }
}
