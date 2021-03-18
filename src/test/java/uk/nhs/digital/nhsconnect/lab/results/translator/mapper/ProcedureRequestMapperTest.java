package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.ProcedureRequest.ProcedureRequestIntent;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

        final Optional<ProcedureRequest> procedureRequest = procedureRequestMapper.mapToProcedureRequest(
            message, null, null, null, null, null);

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

        assertThatThrownBy(() -> procedureRequestMapper.mapToProcedureRequest(
            message, null, null, null, null, null))
            .isInstanceOf(FhirValidationException.class)
            .hasMessage("Unable to map message. "
                + "The FreeText segment is mandatory in Clinical Information");
    }

    @Test
    void testMapToProcedureRequestThrowsExceptionWhenClinicalInformationCodeIsMissing() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S10+10",
            "FTX+CID+++COELIAC"
        ));

        assertThatThrownBy(() -> procedureRequestMapper.mapToProcedureRequest(
            message, null, null, null, null, null))
            .isInstanceOf(MissingSegmentException.class)
            .hasMessage("EDIFACT section is missing segment CIN");
    }

    @Test
    void testMapToProcedureRequestWithOneFreeTextSegment() {
        when(uuidGenerator.generateUUID()).thenReturn("test-uuid");
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S10+10",
            "CIN+UN",
            "FTX+CID+++COELIAC"
        ));

        final Optional<ProcedureRequest> procedureRequest = procedureRequestMapper.mapToProcedureRequest(
            message, null, null, null, null, null);

        assertThat(procedureRequest).isNotEmpty()
            .hasValueSatisfying(procedure -> assertAll(
                () -> assertThat(procedure.getNote())
                    .hasSize(1)
                    .first()
                    .extracting(Annotation::getText)
                    .isEqualTo("COELIAC"),
                () -> assertThat(procedure.getId()).isEqualTo("test-uuid")
            ));
    }

    @Test
    void testMapToProcedureRequestWithMultipleFreeTextSegments() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S10+10",
            "CIN+UN",
            "FTX+CID+++COELIAC",
            "FTX+CID+++JAUNDICE  ??OBSTRUCTIVE",
            "FTX+CID+++GASTRIC ULCER DECLINE"
        ));

        final Optional<ProcedureRequest> procedureRequest = procedureRequestMapper.mapToProcedureRequest(
            message, null, null, null, null, null);

        assertThat(procedureRequest).isNotEmpty()
            .hasValueSatisfying(procedure -> assertAll(
                () -> assertThat(procedure.getNote())
                    .hasSize(3)
                    .extracting(Annotation::getText)
                    .containsExactly("COELIAC", "JAUNDICE  ?OBSTRUCTIVE", "GASTRIC ULCER DECLINE")
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

        final Optional<ProcedureRequest> procedureRequest = procedureRequestMapper.mapToProcedureRequest(
            message, null, null, null, null, null);

        assertThat(procedureRequest)
            .isNotEmpty()
            .hasValueSatisfying(procedure ->
                assertThat(procedure.getStatus().toCode()).isEqualTo("unknown"));
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

        assertThatThrownBy(() -> procedureRequestMapper.mapToProcedureRequest(
            message, null, null, null, null, null))
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

        final Optional<ProcedureRequest> procedureRequest = procedureRequestMapper.mapToProcedureRequest(
            message, null, null, null, null, null);

        assertThat(procedureRequest)
            .isNotEmpty()
            .hasValueSatisfying(procedure ->
                assertThat(procedure.getIntent()).isEqualTo(ProcedureRequestIntent.ORDER));
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

        final Optional<ProcedureRequest> procedureRequest = procedureRequestMapper.mapToProcedureRequest(
            message, mock(Patient.class), null, null, null, null);

        assertThat(procedureRequest)
            .isNotEmpty()
            .hasValueSatisfying(procedure ->
                assertThat(procedure.getSubject().getReference()).isEqualTo("patient-full-url"));
    }

    @Test
    void testMapToProcedureRequestRequesterReferenceWhenPerformingOrganisationIsPresented() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S10+10",
            "CIN+UN",
            "FTX+CID+++COELIAC"
        ));
        when(fullUrlGenerator.generate(nullable(Organization.class)))
            .thenReturn("requesting-organisation-full-url");

        final Optional<ProcedureRequest> procedureRequest = procedureRequestMapper.mapToProcedureRequest(
            message, null, mock(Practitioner.class), mock(Organization.class), null, null);

        assertThat(procedureRequest)
            .isNotEmpty()
            .hasValueSatisfying(procedure ->
                assertThat(procedure.getRequester().getAgent().getReference())
                    .isEqualTo("requesting-organisation-full-url"));
    }

    @Test
    void testMapToProcedureRequestRequesterReferenceWhenOnlyRequestingPractitionerIsPresented() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S10+10",
            "CIN+UN",
            "FTX+CID+++COELIAC"
        ));
        when(fullUrlGenerator.generate(nullable(Practitioner.class)))
            .thenReturn("requesting-practitioner-full-url");

        final Optional<ProcedureRequest> procedureRequest = procedureRequestMapper.mapToProcedureRequest(
            message, null, mock(Practitioner.class), null, null, null);

        assertThat(procedureRequest)
            .isNotEmpty()
            .hasValueSatisfying(procedure ->
                assertThat(procedure.getRequester().getAgent().getReference())
                    .isEqualTo("requesting-practitioner-full-url"));
    }

    @Test
    void testMapToProcedureRequestPerformerReferenceWhenPerformingOrganisationIsPresented() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S10+10",
            "CIN+UN",
            "FTX+CID+++COELIAC"
        ));
        when(fullUrlGenerator.generate(nullable(Organization.class)))
            .thenReturn("performing-organisation-full-url");

        final Optional<ProcedureRequest> procedureRequest = procedureRequestMapper.mapToProcedureRequest(
            message, null, null, null, mock(Practitioner.class), mock(Organization.class));

        assertThat(procedureRequest)
            .isNotEmpty()
            .hasValueSatisfying(procedure ->
                assertThat(procedure.getPerformer().getReference())
                    .isEqualTo("performing-organisation-full-url"));
    }

    @Test
    void testMapToProcedureRequestPerformerReferenceWhenOnlyPerformingPractitionerIsPresented() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S10+10",
            "CIN+UN",
            "FTX+CID+++COELIAC"
        ));
        when(fullUrlGenerator.generate(nullable(Practitioner.class)))
            .thenReturn("performing-practitioner-full-url");

        final Optional<ProcedureRequest> procedureRequest = procedureRequestMapper.mapToProcedureRequest(
            message, null, null, null, mock(Practitioner.class), null);

        assertThat(procedureRequest)
            .isNotEmpty()
            .hasValueSatisfying(procedure ->
                assertThat(procedure.getPerformer().getReference())
                    .isEqualTo("performing-practitioner-full-url"));
    }
}
