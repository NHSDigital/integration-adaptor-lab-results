package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MissingSegmentException;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SuppressWarnings("checkstyle:magicnumber")
@ExtendWith(MockitoExtension.class)
class ProcedureRequestMapperTest {

    @InjectMocks
    private ProcedureRequestMapper procedureRequestMapper;
    @Mock
    private UUIDGenerator uuidGenerator;

    @Test
    void testMapMessageToProcedureRequestNoPatientClinicalInfo() {
        final Message message = new Message(Collections.emptyList());

        final var procedureRequest = procedureRequestMapper.mapToProcedureRequest(message);

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

        assertThatThrownBy(() -> procedureRequestMapper.mapToProcedureRequest(message))
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

        assertThatThrownBy(() -> procedureRequestMapper.mapToProcedureRequest(message))
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

        final var procedureRequest = procedureRequestMapper.mapToProcedureRequest(message).get();

        assertThat(procedureRequest.getNote()).hasSize(1)
            .satisfies(note -> assertThat(note.get(0).getText()).isEqualTo("COELIAC"));
    }

    @Test
    void testMapToProcedureRequestFreeTexts() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S10+10",
            "CIN+UN",
            "FTX+CID+++COELIAC",
            "FTX+CID+++ON AZATHIOPRINE",
            "FTX+CID+++GASTRIC ULCER DECLINE"
        ));

        var procedureRequest = procedureRequestMapper.mapToProcedureRequest(message).get();

        assertThat(procedureRequest.getNote()).hasSize(3)
            .satisfies(note -> assertAll(
                () -> assertThat(note.get(0).getText()).isEqualTo("COELIAC"),
                () -> assertThat(note.get(1).getText()).isEqualTo("ON AZATHIOPRINE"),
                () -> assertThat(note.get(2).getText()).isEqualTo("GASTRIC ULCER DECLINE")
            ));
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

        final var procedureRequest = procedureRequestMapper.mapToProcedureRequest(message).get();

        assertThat(procedureRequest.getStatus().toCode()).isEqualTo("unknown");
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

        final var procedureRequest = procedureRequestMapper.mapToProcedureRequest(message).get();

        assertThat(procedureRequest.getIntent().toCode()).isEqualTo("?");
    }
}
