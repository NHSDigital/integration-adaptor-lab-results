package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.Specimen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.model.MedicalReport;
import uk.nhs.digital.nhsconnect.lab.results.model.MedicalReport.MedicalReportBuilder;
import uk.nhs.digital.nhsconnect.lab.results.utils.ResourceFullUrlGenerator;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BundleMapperTest {
    private static final String SOME_UUID = randomUUID().toString();
    private static final String FULL_URL = "urn:uuid:" + SOME_UUID;

    @Mock
    private UUIDGenerator uuidGenerator;

    @Mock
    private ResourceFullUrlGenerator fullUrlGenerator;

    @InjectMocks
    private BundleMapper bundleMapper;

    private MedicalReportBuilder medicalReportBuilder;

    @BeforeEach
    void setUp() {
        when(uuidGenerator.generateUUID()).thenReturn(SOME_UUID);
        when(fullUrlGenerator.generate(any(Resource.class))).thenReturn(FULL_URL);
        // add members that are required:
        final var mockRequester = mock(Practitioner.class);
        lenient().when(mockRequester.getId()).thenReturn(SOME_UUID);
        medicalReportBuilder = MedicalReport.builder()
            .requestingPractitioner(mockRequester)
            .patient(mock(Patient.class))
            .testReport(mock(DiagnosticReport.class));
    }

    @Test
    void testMapMedicalReportToBundleWithPatient() {
        final var mockPatient = mock(Patient.class);
        medicalReportBuilder.patient(mockPatient);

        final Bundle bundle = bundleMapper.mapToBundle(medicalReportBuilder.build());

        final var patientBundleEntries = bundle.getEntry().stream()
            .filter(entry -> entry.getResource() instanceof Patient)
            .collect(Collectors.toList());
        final var patients = patientBundleEntries.stream()
            .map(BundleEntryComponent::getResource)
            .map(Patient.class::cast)
            .collect(Collectors.toList());

        assertAll(
            () -> verifyBundle(bundle),
            () -> assertThat(patients).containsExactly(mockPatient),
            () -> assertThat(patientBundleEntries).first()
                .extracting(BundleEntryComponent::getFullUrl)
                .isEqualTo(FULL_URL)
        );
    }

    @Test
    void testMapMedicalReportToBundleWithRequestingPractitioner() {
        final var mockRequestingPractitioner = mock(Practitioner.class);
        medicalReportBuilder.requestingPractitioner(mockRequestingPractitioner);

        final var bundle = bundleMapper.mapToBundle(medicalReportBuilder.build());

        final var requesterBundleEntries = bundle.getEntry().stream()
            .filter(entry -> entry.getResource() instanceof Practitioner)
            .collect(Collectors.toList());
        final var requestingPractitioners = requesterBundleEntries.stream()
            .map(BundleEntryComponent::getResource)
            .map(Practitioner.class::cast)
            .collect(Collectors.toList());

        assertAll(
            () -> verifyBundle(bundle),
            () -> assertThat(requestingPractitioners).containsExactly(mockRequestingPractitioner),
            () -> assertThat(requesterBundleEntries).first()
                .extracting(BundleEntryComponent::getFullUrl)
                .isEqualTo(FULL_URL)
        );
    }

    @Test
    void testMapMedicalReportToBundleWithRequestingOrganization() {
        final var mockRequestingOrganization = mock(Organization.class);
        medicalReportBuilder.requestingOrganization(mockRequestingOrganization);

        final var bundle = bundleMapper.mapToBundle(medicalReportBuilder.build());

        final var requestingOrganizationBundleEntries = bundle.getEntry().stream()
            .filter(entry -> entry.getResource() instanceof Organization)
            .collect(Collectors.toList());
        final var requestingOrganizations = requestingOrganizationBundleEntries.stream()
            .map(BundleEntryComponent::getResource)
            .map(Organization.class::cast)
            .collect(Collectors.toList());

        assertAll(
            () -> verifyBundle(bundle),
            () -> assertThat(requestingOrganizations).containsExactly(mockRequestingOrganization),
            () -> assertThat(requestingOrganizationBundleEntries).first()
                .extracting(BundleEntryComponent::getFullUrl)
                .isEqualTo(FULL_URL)
        );
    }

    @Test
    void testMapMedicalReportToBundleWithPerformingPractitioner() {
        final var mockPerformingPractitioner = mock(Practitioner.class);
        medicalReportBuilder.performingPractitioner(mockPerformingPractitioner);

        final var bundle = bundleMapper.mapToBundle(medicalReportBuilder.build());

        final var performerBundleEntries = bundle.getEntry().stream()
            .filter(entry -> entry.getResource() instanceof Practitioner)
            .collect(Collectors.toList());
        final var performingPractitioners = performerBundleEntries.stream()
            .map(BundleEntryComponent::getResource)
            .map(Practitioner.class::cast)
            .collect(Collectors.toList());

        assertAll(
            () -> verifyBundle(bundle),
            () -> assertThat(performingPractitioners).hasSize(2) // includes required requester
                .contains(mockPerformingPractitioner),
            () -> assertThat(performerBundleEntries)
                .extracting(BundleEntryComponent::getFullUrl)
                .allMatch(FULL_URL::equals)
        );
    }

    @Test
    void testMapMedicalReportToBundleWithPerformingOrganization() {
        final var mockPerformingOrganization = mock(Organization.class);
        medicalReportBuilder.performingOrganization(mockPerformingOrganization);

        final var bundle = bundleMapper.mapToBundle(medicalReportBuilder.build());

        final var performingOrganizationBundleEntries = bundle.getEntry().stream()
            .filter(entry -> entry.getResource() instanceof Organization)
            .collect(Collectors.toList());
        final var performingOrganizations = performingOrganizationBundleEntries.stream()
            .map(BundleEntryComponent::getResource)
            .map(Organization.class::cast)
            .collect(Collectors.toList());

        assertAll(
            () -> verifyBundle(bundle),
            () -> assertThat(performingOrganizations).containsExactly(mockPerformingOrganization),
            () -> assertThat(performingOrganizationBundleEntries).first()
                .extracting(BundleEntryComponent::getFullUrl)
                .isEqualTo(FULL_URL)
        );
    }

    @Test
    void testMapMedicalReportToBundleWithProcedureRequest() {
        final var mockProcedureRequest = mock(ProcedureRequest.class);
        medicalReportBuilder.testRequestSummary(mockProcedureRequest);

        final var bundle = bundleMapper.mapToBundle(medicalReportBuilder.build());

        final var procedureRequestBundleEntries = bundle.getEntry().stream()
            .filter(entry -> entry.getResource() instanceof ProcedureRequest)
            .collect(Collectors.toList());
        final var procedureRequests = procedureRequestBundleEntries.stream()
            .map(BundleEntryComponent::getResource)
            .map(ProcedureRequest.class::cast)
            .collect(Collectors.toList());

        assertAll(
            () -> verifyBundle(bundle),
            () -> assertThat(procedureRequests)
                .containsExactly(mockProcedureRequest),
            () -> assertThat(procedureRequestBundleEntries)
                .extracting(BundleEntryComponent::getFullUrl)
                .allMatch(FULL_URL::equals)
        );
    }

    @Test
    void testMapMedicalReportToBundleWithSpecimens() {
        final var mockSpecimen1 = mock(Specimen.class);
        final var mockSpecimen2 = mock(Specimen.class);
        medicalReportBuilder.specimens(List.of(mockSpecimen1, mockSpecimen2));

        final var bundle = bundleMapper.mapToBundle(medicalReportBuilder.build());

        final var specimenBundleEntries = bundle.getEntry().stream()
            .filter(entry -> entry.getResource() instanceof Specimen)
            .collect(Collectors.toList());
        final var specimens = specimenBundleEntries.stream()
            .map(BundleEntryComponent::getResource)
            .map(Specimen.class::cast)
            .collect(Collectors.toList());

        assertAll(
            () -> verifyBundle(bundle),
            () -> assertThat(specimens)
                .containsOnly(mockSpecimen1, mockSpecimen2),
            () -> assertThat(specimenBundleEntries)
                .extracting(BundleEntryComponent::getFullUrl)
                .allMatch(FULL_URL::equals)
        );
    }

    @Test
    void testMapMedicalReportToBundleWithTestResults() {
        final var mockTestResult1 = mock(Observation.class);
        final var mockTestResult2 = mock(Observation.class);
        medicalReportBuilder.testResults(List.of(mockTestResult1, mockTestResult2));

        final var bundle = bundleMapper.mapToBundle(medicalReportBuilder.build());

        final var observationBundleEntries = bundle.getEntry().stream()
            .filter(entry -> entry.getResource() instanceof Observation)
            .collect(Collectors.toList());
        final var observations = observationBundleEntries.stream()
            .map(BundleEntryComponent::getResource)
            .map(Observation.class::cast)
            .collect(Collectors.toList());

        assertAll(
            () -> verifyBundle(bundle),
            () -> assertThat(observations)
                .containsExactly(mockTestResult1, mockTestResult2),
            () -> assertThat(observationBundleEntries)
                .extracting(BundleEntryComponent::getFullUrl)
                .allMatch(FULL_URL::equals)
        );
    }

    @Test
    void testMapMedicalReportToBundleWithDiagnosticReport() {
        final var mockDiagnosticReport = mock(DiagnosticReport.class);
        medicalReportBuilder.testReport(mockDiagnosticReport);

        final Bundle bundle = bundleMapper.mapToBundle(medicalReportBuilder.build());

        final var diagnosticReportBundleEntries = bundle.getEntry().stream()
            .filter(entry -> entry.getResource() instanceof DiagnosticReport)
            .collect(Collectors.toList());
        final var diagnosticReports = diagnosticReportBundleEntries.stream()
            .map(BundleEntryComponent::getResource)
            .map(DiagnosticReport.class::cast)
            .collect(Collectors.toList());

        assertAll(
            () -> verifyBundle(bundle),
            () -> assertThat(diagnosticReports).containsExactly(mockDiagnosticReport),
            () -> assertThat(diagnosticReportBundleEntries).first()
                .extracting(BundleEntryComponent::getFullUrl)
                .isEqualTo(FULL_URL)
        );
    }

    private void verifyBundle(Bundle bundle) {
        assertAll(
            () -> assertThat(bundle.getMeta().getLastUpdated()).isNotNull(),
            () -> assertThat(bundle.getMeta().getProfile().get(0).asStringValue())
                .isEqualTo("https://fhir.nhs.uk/STU3/StructureDefinition/ITK-Message-Bundle-1"),
            () -> assertThat(bundle.getIdentifier().getSystem()).isEqualTo("https://tools.ietf.org/html/rfc4122"),
            () -> assertThat(bundle.getIdentifier().getValue()).isEqualTo(SOME_UUID),
            () -> assertThat(bundle.getType()).isEqualTo(Bundle.BundleType.MESSAGE)
        );
    }
}
