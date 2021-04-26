package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.MessageHeader;
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
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.model.MedicalReport;
import uk.nhs.digital.nhsconnect.lab.results.utils.ResourceFullUrlGenerator;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BundleMapperTest {
    private static final String MESSAGE_HEADER_ID = randomUUID().toString();
    private static final String PATIENT_ID = randomUUID().toString();
    private static final String PERFORMING_ORGANIZATION_ID = randomUUID().toString();
    private static final String PERFORMING_PRACTITIONER_ID = randomUUID().toString();
    private static final String REQUESTING_ORGANIZATION_ID = randomUUID().toString();
    private static final String REQUESTING_PRACTITIONER_ID = randomUUID().toString();
    private static final String SPECIMEN_1_ID = randomUUID().toString();
    private static final String SPECIMEN_2_ID = randomUUID().toString();
    private static final String TEST_REPORT_ID = randomUUID().toString();
    private static final String TEST_REQUEST_SUMMARY_ID = randomUUID().toString();
    private static final String TEST_RESULTS_1_ID = randomUUID().toString();
    private static final String TEST_RESULTS_2_ID = randomUUID().toString();
    private static final String BUNDLE_IDENTIFIER = randomUUID().toString();

    @Mock
    private UUIDGenerator uuidGenerator;

    @Mock
    private ResourceFullUrlGenerator fullUrlGenerator;

    @Mock
    private MessageHeader messageHeader;
    @Mock
    private Patient patient;
    @Mock
    private Organization performingOrganization;
    @Mock
    private Practitioner performingPractitioner;
    @Mock
    private Organization requestingOrganization;
    @Mock
    private Practitioner requestingPractitioner;
    @Mock
    private Specimen specimen1;
    @Mock
    private Specimen specimen2;
    @Mock
    private DiagnosticReport testReport;
    @Mock
    private ProcedureRequest testRequestSummary;
    @Mock
    private Observation testResult1;
    @Mock
    private Observation testResult2;

    @InjectMocks
    private BundleMapper bundleMapper;

    private MedicalReport medicalReport;

    private Map<Resource, String> allResources;

    @BeforeEach
    void setUp() {
        allResources = Map.ofEntries(
            new AbstractMap.SimpleEntry<>(messageHeader, MESSAGE_HEADER_ID),
            new AbstractMap.SimpleEntry<>(patient, PATIENT_ID),
            new AbstractMap.SimpleEntry<>(performingOrganization, PERFORMING_ORGANIZATION_ID),
            new AbstractMap.SimpleEntry<>(performingPractitioner, PERFORMING_PRACTITIONER_ID),
            new AbstractMap.SimpleEntry<>(requestingOrganization, REQUESTING_ORGANIZATION_ID),
            new AbstractMap.SimpleEntry<>(requestingPractitioner, REQUESTING_PRACTITIONER_ID),
            new AbstractMap.SimpleEntry<>(specimen1, SPECIMEN_1_ID),
            new AbstractMap.SimpleEntry<>(specimen2, SPECIMEN_2_ID),
            new AbstractMap.SimpleEntry<>(testReport, TEST_REPORT_ID),
            new AbstractMap.SimpleEntry<>(testRequestSummary, TEST_REQUEST_SUMMARY_ID),
            new AbstractMap.SimpleEntry<>(testResult1, TEST_RESULTS_1_ID),
            new AbstractMap.SimpleEntry<>(testResult2, TEST_RESULTS_2_ID)
        );

        allResources.forEach((key, value) -> when(fullUrlGenerator.generate(key)).thenReturn(value));

        when(uuidGenerator.generateUUID()).thenReturn(BUNDLE_IDENTIFIER);

        medicalReport = MedicalReport.builder()
            .messageHeader(messageHeader)
            .patient(patient)
            .performingOrganization(performingOrganization)
            .performingPractitioner(performingPractitioner)
            .requestingOrganization(requestingOrganization)
            .requestingPractitioner(requestingPractitioner)
            .specimens(List.of(specimen1, specimen2))
            .testReport(testReport)
            .testRequestSummary(testRequestSummary)
            .testResults(List.of(testResult1, testResult2))
            .build();
    }

    @Test
    void testMapMedicalReportToBundleWithPatient() {
        final Bundle bundle = bundleMapper.mapToBundle(medicalReport);

        assertAll(Stream.of(
            Stream.<Executable>of(
                () -> verifyBundle(bundle),
                () -> assertThat(bundle.getEntry()).hasSize(allResources.size())
            ),
            bundle.getEntry().stream()
                .map(bundleEntryComponent -> (Executable) () ->
                    assertThat(bundleEntryComponent.getFullUrl())
                        .isEqualTo(allResources.get(bundleEntryComponent.getResource())))
        )
            .flatMap(stream -> stream)
            .toArray(Executable[]::new));
    }

    private void verifyBundle(Bundle bundle) {
        assertAll(
            () -> assertThat(bundle.getMeta().getLastUpdated()).isNotNull(),
            () -> assertThat(bundle.getMeta().getProfile().get(0).asStringValue())
                .isEqualTo("https://fhir.nhs.uk/STU3/StructureDefinition/ITK-Message-Bundle-1"),
            () -> assertThat(bundle.getIdentifier().getSystem()).isEqualTo("https://tools.ietf.org/html/rfc4122"),
            () -> assertThat(bundle.getIdentifier().getValue()).isEqualTo(BUNDLE_IDENTIFIER),
            () -> assertThat(bundle.getType()).isEqualTo(Bundle.BundleType.MESSAGE)
        );
    }
}
