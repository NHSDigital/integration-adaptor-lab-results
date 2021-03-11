package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Specimen;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MissingSegmentException;
import uk.nhs.digital.nhsconnect.lab.results.utils.ResourceFullUrlGenerator;
import uk.nhs.digital.nhsconnect.lab.results.utils.TimestampService;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("checkstyle:MagicNumber")
@ExtendWith(MockitoExtension.class)
class DiagnosticReportMapperTest {

    @Mock
    private UUIDGenerator uuidGenerator;

    @Mock
    private ResourceFullUrlGenerator resourceFullUrlGenerator;

    @InjectMocks
    private DiagnosticReportMapper mapper;

    @Test
    void testMapToDiagnosticReportMissingRequiredSegments() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06" // InvestigationSubject
        ));

        assertThatThrownBy(() -> mapper.mapToDiagnosticReport(message, null, Collections.emptyList(),
            Collections.emptyList(), null, null, null))
            .isExactlyInstanceOf(MissingSegmentException.class)
            .hasMessageStartingWith("EDIFACT section is missing segment");
    }

    @Test
    void testMapToDiagnosticReportWithMessageWithoutResourceReferences() {
        final Message message = new Message(List.of(
            "S02+02",
            "GIS+N",
            "RFF+SRI:15/CH000042P/200010191704",
            "STS++UN",
            "DTM+ISR:201002251541:203"
        ));

        when(uuidGenerator.generateUUID()).thenReturn("resource-id");

        final var result = mapper.mapToDiagnosticReport(message, null, Collections.emptyList(),
            Collections.emptyList(), null, null, null);
        ZonedDateTime dateIssued = LocalDateTime.of(2010, 02, 25, 15, 41).atZone(TimestampService.UK_ZONE);

        assertAll(
            () -> assertThat(result.getId()).isEqualTo("resource-id"),
            () -> assertThat(result.getIdentifier())
                .first()
                .satisfies(identifier -> assertAll(
                    () -> assertThat(identifier.getValue()).isEqualTo("15/CH000042P/200010191704")
                )),
            () -> assertThat(result.getCode().getCoding())
                .hasSize(1)
                .first()
                .satisfies(coding -> assertAll(
                    () -> assertThat(coding.getCode()).isEqualTo("721981007"),
                    () -> assertThat(coding.getDisplay()).isEqualTo("Diagnostic studies report"),
                    () -> assertThat(coding.getSystem()).isEqualTo("http://snomed.info/sct")
                )),
            () -> assertThat(result.getStatus()).isEqualTo(DiagnosticReport.DiagnosticReportStatus.UNKNOWN),
            () -> assertThat(result.getIssued()).isEqualTo(Date.from(dateIssued.toInstant()))
        );
    }

    @Test
    void testMapToDiagnosticReportWithSubjectResource() {
        final Message message = new Message(List.of(
            "S02+02",
            "GIS+N",
            "RFF+SRI:15/CH000042P/200010191704",
            "STS++UN",
            "DTM+ISR:201002251541:203"
        ));

        final var patient = mock(Patient.class);
        when(resourceFullUrlGenerator.generate(any(Patient.class))).thenReturn("patient-full-url");

        final var result = mapper.mapToDiagnosticReport(message, patient, Collections.emptyList(),
            Collections.emptyList(), null, null, null);

        assertThat(result.getSubject())
            .extracting(Reference::getReference)
            .isEqualTo("patient-full-url");
    }

    @Test
    void testMapToDiagnosticReportWithSpecimenResources() {
        final Message message = new Message(List.of(
            "S02+02",
            "GIS+N",
            "RFF+SRI:15/CH000042P/200010191704",
            "STS++UN",
            "DTM+ISR:201002251541:203"
        ));

        final var specimen1 = mock(Specimen.class);
        final var specimen2 = mock(Specimen.class);

        when(resourceFullUrlGenerator.generate(nullable(Patient.class))).thenReturn("patient-id");
        when(resourceFullUrlGenerator.generate(specimen1)).thenReturn("specimen-1-id");
        when(resourceFullUrlGenerator.generate(specimen2)).thenReturn("specimen-2-id");

        final var result = mapper.mapToDiagnosticReport(message, null, List.of(specimen1, specimen2),
            Collections.emptyList(), null, null, null);

        assertThat(result.getSpecimen())
            .hasSize(2)
            .extracting(Reference::getReference)
            .containsExactly("specimen-1-id", "specimen-2-id");
    }

    @Test
    void testMapToDiagnosticReportWithObservationResources() {
        final Message message = new Message(List.of(
            "S02+02",
            "GIS+N",
            "RFF+SRI:15/CH000042P/200010191704",
            "STS++UN",
            "DTM+ISR:201002251541:203"
        ));

        final var observation1 = mock(Observation.class);
        final var observation2 = mock(Observation.class);

        when(resourceFullUrlGenerator.generate(nullable(Patient.class))).thenReturn("patient-id");
        when(resourceFullUrlGenerator.generate(observation1)).thenReturn("observation-1-id");
        when(resourceFullUrlGenerator.generate(observation2)).thenReturn("observation-2-id");

        final var result = mapper.mapToDiagnosticReport(message, null, Collections.emptyList(),
            List.of(observation1, observation2), null, null, null);

        assertThat(result.getResult())
            .hasSize(2)
            .extracting(Reference::getReference)
            .containsExactly("observation-1-id", "observation-2-id");
    }

    @Test
    void testMapToDiagnosticReportWithPerformingPractitioner() {
        final Message message = new Message(List.of(
            "S02+02",
            "GIS+N",
            "RFF+SRI:15/CH000042P/200010191704",
            "STS++UN",
            "DTM+ISR:201002251541:203"
        ));

        final var practitioner = mock(Practitioner.class);

        when(resourceFullUrlGenerator.generate(nullable(Practitioner.class))).thenReturn("practitioner-id");
        final var result = mapper.mapToDiagnosticReport(message, null, Collections.emptyList(),
            Collections.emptyList(), practitioner, null, null);

        assertAll(
            () -> assertThat(result.getPerformer()).hasSize(1),
            () -> assertThat(result.getPerformer().get(0).getActor())
                .extracting(Reference::getReference)
                .isEqualTo("practitioner-id")
        );
    }

    @Test
    void testMapToDiagnosticReportWithPerformingOrganization() {
        final Message message = new Message(List.of(
            "S02+02",
            "GIS+N",
            "RFF+SRI:15/CH000042P/200010191704",
            "STS++UN",
            "DTM+ISR:201002251541:203"
        ));

        final var organization = mock(Organization.class);

        when(resourceFullUrlGenerator.generate(nullable(Organization.class))).thenReturn("organization-id");
        final var result = mapper.mapToDiagnosticReport(message, null, Collections.emptyList(),
            Collections.emptyList(), null, organization, null);

        assertAll(
            () -> assertThat(result.getPerformer()).hasSize(1),
            () -> assertThat(result.getPerformer().get(0).getActor())
                .extracting(Reference::getReference)
                .isEqualTo("organization-id")
        );
    }

    @Test
    void testMapToDiagnosticReportWithProcedureRequest() {
        final Message message = new Message(List.of(
            "S02+02",
            "GIS+N",
            "RFF+SRI:15/CH000042P/200010191704",
            "STS++UN",
            "DTM+ISR:201002251541:203"
        ));

        final var procedureRequest = mock(ProcedureRequest.class);

        when(resourceFullUrlGenerator.generate(nullable(ProcedureRequest.class))).thenReturn("procedure-request-id");
        final var result = mapper.mapToDiagnosticReport(message, null, Collections.emptyList(),
            Collections.emptyList(), null, null, procedureRequest);

        assertThat(result.getBasedOn())
            .hasSize(1)
            .extracting(Reference::getReference)
            .containsExactly("procedure-request-id");
    }
}
