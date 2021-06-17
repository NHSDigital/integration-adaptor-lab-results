package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.utils.ResourceFullUrlGenerator;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
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
    void when_freeTextIsMissing_expect_exception() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S10+10",
            "CIN+UN"
        ));

        assertThatThrownBy(() -> procedureRequestMapper.mapToProcedureRequest(
            message, mock(Patient.class), mock(Practitioner.class), mock(Organization.class), mock(Practitioner.class)))
            .isInstanceOf(FhirValidationException.class)
            .hasMessage("Unable to map message. "
                + "The FreeText segment is mandatory in Clinical Information");
    }

    @Test
    void when_oneFreeTextFieldIsPresent_expect_oneAnnotation() {
        when(uuidGenerator.generateUUID()).thenReturn("test-uuid");

        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S10+10",
            "CIN+UN",
            "FTX+CID+++COELIAC"
        ));

        ProcedureRequest procedureRequest = procedureRequestMapper.mapToProcedureRequest(
            message, mock(Patient.class), mock(Practitioner.class), mock(Organization.class), mock(Practitioner.class));

        assertAll(
            () -> assertThat(procedureRequest.getNote())
                .hasSize(1)
                .first()
                .extracting(Annotation::getText)
                .isEqualTo("COELIAC"),
            () -> assertThat(procedureRequest.getId()).isEqualTo("test-uuid")
        );
    }

    @Test
    void when_multipleFreeTextFieldsArePresent_expect_multipleAnnotations() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S10+10",
            "CIN+UN",
            "FTX+CID+++COELIAC",
            "FTX+CID+++JAUNDICE  ??OBSTRUCTIVE",
            "FTX+CID+++GASTRIC ULCER DECLINE"
        ));

        ProcedureRequest procedureRequest = procedureRequestMapper.mapToProcedureRequest(
            message, mock(Patient.class), mock(Practitioner.class), mock(Organization.class), mock(Practitioner.class));

        assertAll(
            () -> assertThat(procedureRequest.getNote())
                .hasSize(3)
                .extracting(Annotation::getText)
                .containsExactly("COELIAC", "JAUNDICE  ?OBSTRUCTIVE", "GASTRIC ULCER DECLINE")
        );
    }

    @Test
    void when_mappingEdifact_expect_otherResourcesAreReferenced() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S10+10",
            "CIN+UN",
            "FTX+CID+++COELIAC"
        ));

        var patient = mock(Patient.class);
        var requestingOrganization = mock(Organization.class);
        var requestingPractitioner = mock(Practitioner.class);
        var performingPractitioner = mock(Practitioner.class);

        when(fullUrlGenerator.generate(patient)).thenReturn("patient-full-url");
        when(fullUrlGenerator.generate(requestingOrganization)).thenReturn("requesting-organisation-full-url");
        when(fullUrlGenerator.generate(requestingPractitioner)).thenReturn("requesting-practitioner-full-url");
        when(fullUrlGenerator.generate(performingPractitioner)).thenReturn("performing-practitioner-full-url");

        ProcedureRequest procedureRequest = procedureRequestMapper.mapToProcedureRequest(
            message, patient, requestingPractitioner, requestingOrganization, performingPractitioner);

        assertAll(
            () -> assertThat(procedureRequest.getSubject().getReference())
                .isEqualTo("patient-full-url"),
            () -> assertThat(procedureRequest.getRequester().getAgent().getReference())
                .isEqualTo("requesting-practitioner-full-url"),
            () -> assertThat(procedureRequest.getRequester().getOnBehalfOf().getReference())
                .isEqualTo("requesting-organisation-full-url"),
            () -> assertThat(procedureRequest.getPerformer().getReference())
                .isEqualTo("performing-practitioner-full-url")
        );
    }
}
