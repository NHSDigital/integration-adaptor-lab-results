package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.Specimen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.model.fhir.PathologyRecord;
import uk.nhs.digital.nhsconnect.lab.results.model.fhir.PathologyRecord.PathologyRecordBuilder;
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

    private PathologyRecordBuilder pathologyRecordBuilder;

    @BeforeEach
    void setUp() {
        when(uuidGenerator.generateUUID()).thenReturn(SOME_UUID);
        when(fullUrlGenerator.generate(any(Resource.class))).thenReturn(FULL_URL);
        // add members that are required:
        final var mockRequester = mock(Practitioner.class);
        lenient().when(mockRequester.getId()).thenReturn(SOME_UUID);
        pathologyRecordBuilder = PathologyRecord.builder()
            .requestingPractitioner(mockRequester)
            .patient(mock(Patient.class));
    }

    @Test
    void testMapPathologyRecordToBundleWithPatient() {
        final var mockPatient = mock(Patient.class);
        pathologyRecordBuilder.patient(mockPatient);

        final Bundle bundle = bundleMapper.mapToBundle(pathologyRecordBuilder.build());

        final var patientBundleEntries = bundle.getEntry().stream()
            .filter(entry -> entry.getResource() instanceof Patient)
            .collect(Collectors.toList());
        final var patients = patientBundleEntries.stream()
            .map(BundleEntryComponent::getResource)
            .map(Patient.class::cast)
            .collect(Collectors.toList());

        assertAll(
            () -> verifyBundle(bundle),
            () -> assertThat(patients).hasSize(1).contains(mockPatient),
            () -> assertThat(patientBundleEntries).first()
                .extracting(BundleEntryComponent::getFullUrl)
                .isEqualTo(FULL_URL)
        );
    }

    @Test
    void testMapPathologyRecordToBundleWithRequestingPractitioner() {
        final var mockRequestingPractitioner = mock(Practitioner.class);
        pathologyRecordBuilder.requestingPractitioner(mockRequestingPractitioner);

        final var bundle = bundleMapper.mapToBundle(pathologyRecordBuilder.build());

        final var requesterBundleEntries = bundle.getEntry().stream()
            .filter(entry -> entry.getResource() instanceof Practitioner)
            .collect(Collectors.toList());
        final var requestingPractitioners = requesterBundleEntries.stream()
            .map(BundleEntryComponent::getResource)
            .map(Practitioner.class::cast)
            .collect(Collectors.toList());

        assertAll(
            () -> verifyBundle(bundle),
            () -> assertThat(requestingPractitioners).hasSize(1).contains(mockRequestingPractitioner),
            () -> assertThat(requesterBundleEntries).first()
                .extracting(BundleEntryComponent::getFullUrl)
                .isEqualTo(FULL_URL)
        );
    }

    @Test
    void testMapPathologyRecordToBundleWithRequestingOrganization() {
        final var mockRequestingOrganization = mock(Organization.class);
        pathologyRecordBuilder.requestingOrganization(mockRequestingOrganization);

        final var bundle = bundleMapper.mapToBundle(pathologyRecordBuilder.build());

        final var requestingOrganizationBundleEntries = bundle.getEntry().stream()
            .filter(entry -> entry.getResource() instanceof Organization)
            .collect(Collectors.toList());
        final var requestingOrganizations = requestingOrganizationBundleEntries.stream()
            .map(BundleEntryComponent::getResource)
            .map(Organization.class::cast)
            .collect(Collectors.toList());

        assertAll(
            () -> verifyBundle(bundle),
            () -> assertThat(requestingOrganizations).hasSize(1).contains(mockRequestingOrganization),
            () -> assertThat(requestingOrganizationBundleEntries).first()
                .extracting(BundleEntryComponent::getFullUrl)
                .isEqualTo(FULL_URL)
        );
    }

    @Test
    void testMapPathologyRecordToBundleWithPerformingPractitioner() {
        final var mockPerformingPractitioner = mock(Practitioner.class);
        pathologyRecordBuilder.performingPractitioner(mockPerformingPractitioner);

        final var bundle = bundleMapper.mapToBundle(pathologyRecordBuilder.build());

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
    void testMapPathologyRecordToBundleWithPerformingOrganization() {
        final var mockPerformingOrganization = mock(Organization.class);
        pathologyRecordBuilder.performingOrganization(mockPerformingOrganization);

        final var bundle = bundleMapper.mapToBundle(pathologyRecordBuilder.build());

        final var performingOrganizationBundleEntries = bundle.getEntry().stream()
            .filter(entry -> entry.getResource() instanceof Organization)
            .collect(Collectors.toList());
        final var performingOrganizations = performingOrganizationBundleEntries.stream()
            .map(BundleEntryComponent::getResource)
            .map(Organization.class::cast)
            .collect(Collectors.toList());

        assertAll(
            () -> verifyBundle(bundle),
            () -> assertThat(performingOrganizations).hasSize(1).contains(mockPerformingOrganization),
            () -> assertThat(performingOrganizationBundleEntries).first()
                .extracting(BundleEntryComponent::getFullUrl)
                .isEqualTo(FULL_URL)
        );
    }

    @Test
    void testMapPathologyRecordToBundleWithSpecimens() {
        final var mockSpecimen1 = mock(Specimen.class);
        final var mockSpecimen2 = mock(Specimen.class);
        pathologyRecordBuilder.specimens(List.of(mockSpecimen1, mockSpecimen2));

        final var bundle = bundleMapper.mapToBundle(pathologyRecordBuilder.build());

        final var specimenBundleEntries = bundle.getEntry().stream()
            .filter(entry -> entry.getResource() instanceof Specimen)
            .collect(Collectors.toList());
        final var specimens = specimenBundleEntries.stream()
            .map(BundleEntryComponent::getResource)
            .map(Specimen.class::cast)
            .collect(Collectors.toList());

        assertAll(
            () -> verifyBundle(bundle),
            () -> assertThat(specimens).hasSize(2)
                .contains(mockSpecimen1, mockSpecimen2),
            () -> assertThat(specimenBundleEntries)
                .extracting(BundleEntryComponent::getFullUrl)
                .allMatch(FULL_URL::equals)
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
