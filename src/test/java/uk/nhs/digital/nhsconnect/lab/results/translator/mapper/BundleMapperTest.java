package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Specimen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.model.fhir.PathologyRecord;
import uk.nhs.digital.nhsconnect.lab.results.model.fhir.PathologyRecord.PathologyRecordBuilder;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BundleMapperTest {
    @Mock
    private UUIDGenerator uuidGenerator;

    @InjectMocks
    private BundleMapper bundleMapper;

    private PathologyRecordBuilder pathologyRecordBuilder;

    @BeforeEach
    void setUp() {
        when(uuidGenerator.generateUUID()).thenReturn("some-uuid");
        // add members that are required:
        pathologyRecordBuilder = PathologyRecord.builder()
            .patient(mock(Patient.class));
    }

    @Test
    void testMapPathologyRecordToBundleWithPractitioner() {
        final var mockRequester = mock(Practitioner.class);
        pathologyRecordBuilder.requester(mockRequester);

        final var bundle = bundleMapper.mapToBundle(pathologyRecordBuilder.build());

        final var practitionerBundleEntries = bundle.getEntry().stream()
            .filter(entry -> entry.getResource() instanceof Practitioner)
            .collect(Collectors.toList());
        final var practitioners = practitionerBundleEntries.stream()
            .map(BundleEntryComponent::getResource)
            .map(Practitioner.class::cast)
            .collect(Collectors.toList());

        assertAll(
            () -> verifyBundle(bundle),
            () -> assertThat(practitioners).hasSize(1).contains(mockRequester),
            () -> assertThat(practitionerBundleEntries).first()
                .extracting(BundleEntryComponent::getFullUrl)
                .isEqualTo("urn:uuid:some-uuid")
        );
    }

    @Test
    void testMapPathologyRecordToBundleWithPatient() {
        final var mockPatient = mock(Patient.class);
        when(mockPatient.getId()).thenReturn("some-uuid");
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
                .isEqualTo("urn:uuid:some-uuid")
        );
    }

    @Test
    void testMapPathologyRecordToBundleWithSpecimens() {
        final var mockSpecimen1 = mock(Specimen.class);
        when(mockSpecimen1.getId()).thenReturn("some-uuid");
        final var mockSpecimen2 = mock(Specimen.class);
        when(mockSpecimen2.getId()).thenReturn("some-uuid");
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
                .allMatch("urn:uuid:some-uuid"::equals)
        );
    }

    private void verifyBundle(Bundle bundle) {
        assertAll(
            () -> assertThat(bundle.getMeta().getLastUpdated()).isNotNull(),
            () -> assertThat(bundle.getMeta().getProfile().get(0).asStringValue())
                .isEqualTo("https://fhir.nhs.uk/STU3/StructureDefinition/ITK-Message-Bundle-1"),
            () -> assertThat(bundle.getIdentifier().getSystem()).isEqualTo("https://tools.ietf.org/html/rfc4122"),
            () -> assertThat(bundle.getIdentifier().getValue()).isEqualTo("some-uuid"),
            () -> assertThat(bundle.getType()).isEqualTo(Bundle.BundleType.MESSAGE)
        );
    }
}
