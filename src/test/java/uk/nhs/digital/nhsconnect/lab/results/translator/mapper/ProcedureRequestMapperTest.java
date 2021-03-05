package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MissingSegmentException;
import uk.nhs.digital.nhsconnect.lab.results.utils.ResourceFullUrlGenerator;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.hl7.fhir.dstu3.model.ProcedureRequest.ProcedureRequestIntent;

@SuppressWarnings("checkstyle:magicnumber")
@ExtendWith(MockitoExtension.class)
class ProcedureRequestMapperTest {

    @InjectMocks
    private ProcedureRequestMapper procedureRequestMapper;
    @Mock
    private UUIDGenerator uuidGenerator;
    @Mock
    private ResourceFullUrlGenerator fullUrlGenerator;

    @Test
    void testMapMessageToProcedureRequestNoPatientClinicalInfo() {
        final Message message = new Message(Collections.emptyList());

        final var procedureRequest = procedureRequestMapper.mapToProcedureRequest(message, null);

        assertThat(procedureRequest).isEmpty();
    }

    @Test
    void testMapToProcedureRequestThrowsExceptionWhenFreeTextIsMissing() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S10+10",
            "CIN+UN"
        ));

        assertThatThrownBy(() -> procedureRequestMapper.mapToProcedureRequest(message, null))
            .isInstanceOf(FhirValidationException.class)
            .hasMessage("Unable to map message. "
                + "The FreeText segment is mandatory in Clinical Information");
    }

    @Test
    void testMapToProcedureRequestThrowsExceptionWhenTypeOfClinicalObservationIsMissing() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S10+10",
            "FTX+CID+++COELIAC"
        ));

        assertThatThrownBy(() -> procedureRequestMapper.mapToProcedureRequest(message, null))
            .isInstanceOf(MissingSegmentException.class)
            .hasMessage("EDIFACT section is missing segment CIN");
    }

    @Test
    void testMapToProcedureRequestFreeText() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S10+10",
            "CIN+UN",
            "FTX+CID+++COELIAC"
        ));

        final var procedureRequest = procedureRequestMapper.mapToProcedureRequest(message, null).get();

        assertThat(procedureRequest.getNote()).hasSize(1)
            .first()
            .satisfies(note -> assertThat(note.getText()).isEqualTo("COELIAC"));
    }

    @Test
    void testMapToProcedureRequestFreeTexts() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S10+10",
            "CIN+UN",
            "FTX+CID+++COELIAC",
            "FTX+CID+++JAUNDICE  ??OBSTRUCTIVE",
            "FTX+CID+++GASTRIC ULCER DECLINE"
        ));

        var procedureRequest = procedureRequestMapper.mapToProcedureRequest(message, null).get();

        assertThat(procedureRequest.getNote()).hasSize(3)
            .extracting(Annotation::getText)
            .containsExactly("COELIAC", "JAUNDICE  ?OBSTRUCTIVE", "GASTRIC ULCER DECLINE");
    }

    @Test
    void testMapToProcedureRequestStatus() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S10+10",
            "CIN+UN",
            "FTX+CID+++COELIAC"
        ));

        final var procedureRequest = procedureRequestMapper.mapToProcedureRequest(message, null).get();

        assertThat(procedureRequest.getStatus().toCode()).isEqualTo("unknown");
    }

    @Test
    void testMapToProcedureRequestUndefinedStatus() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S10+10",
            "CIN+Undefined Status",
            "FTX+CID+++COELIAC"
        ));

        assertThatThrownBy(() -> procedureRequestMapper.mapToProcedureRequest(message, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No Report Status Code for 'Undefined Status'");
    }

    @Test
    void testMapToProcedureRequestIntent() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S10+10",
            "CIN+UN",
            "FTX+CID+++COELIAC"
        ));

        final var procedureRequest = procedureRequestMapper.mapToProcedureRequest(message, null).get();

        assertThat(procedureRequest.getIntent()).isEqualTo(ProcedureRequestIntent.NULL);
    }

    @Test
    void testMapToProcedureRequestPatientReference() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S10+10",
            "CIN+UN",
            "FTX+CID+++COELIAC"
        ));
        when(fullUrlGenerator.generate(any(Patient.class))).thenReturn("patient-full-url");

        final var procedureRequest = procedureRequestMapper.mapToProcedureRequest(message, mock(Patient.class)).get();

        assertThat(procedureRequest)
            .satisfies(subject -> assertThat(subject.getSubject()).extracting(Reference::getReference)
                .isEqualTo("patient-full-url"));
    }
}
