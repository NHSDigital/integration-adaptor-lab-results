package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.HumanName;
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
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.nhs.digital.nhsconnect.lab.results.fixtures.FhirFixtures.generatePatient;
import static uk.nhs.digital.nhsconnect.lab.results.fixtures.FhirFixtures.generateRequester;
import static uk.nhs.digital.nhsconnect.lab.results.fixtures.PathologyRecordFixtures.generatePathologyRecord;

@ExtendWith(MockitoExtension.class)
class BundleMapperTest {
    private static final Enumerations.AdministrativeGender GENDER = Enumerations.AdministrativeGender.MALE;
    private static final String NAME_TEXT = "Dr Bob Hope";
    private static final String BIRTH_DATE = "2001-01-12";
    private static final String VALUE_UUID = "some-value-uuid";
    private static final String ENTRY_UUID = "some-entry-uuid";
    private static final int BUNDLE_ENTRY_SIZE = 2;
    private static final Predicate<BundleEntryComponent> URL_IS_SOME_UUID =
        component -> "urn:uuid:some-uuid".equals(component.getFullUrl());

    @Mock
    private UUIDGenerator uuidGenerator;

    @InjectMocks
    private BundleMapper bundleMapper;

    @BeforeEach
    void setUp() {
        when(uuidGenerator.generateUUID()).thenReturn("some-uuid");
    }

    @Test
    void testMapPathologyRecordToBundleWithPractitioner() {
        final var mockRequester = mock(Practitioner.class);
        final var pathologyRecord = PathologyRecord.builder()
            .requester(mockRequester)
            .build();
        final var bundle = bundleMapper.mapToBundle(pathologyRecord);

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
                .matches(URL_IS_SOME_UUID)
        );
    }

    @Test
    void testMapPathologyRecordToBundleWithPatient() {
        when(uuidGenerator.generateUUID()).thenReturn(VALUE_UUID).thenReturn(ENTRY_UUID);

        Practitioner generatedRequester = generateRequester(NAME_TEXT, GENDER);
        Patient generatedPatient = generatePatient(NAME_TEXT, GENDER, BIRTH_DATE);

        final Bundle bundle = bundleMapper.mapToBundle(generatePathologyRecord(generatedRequester, generatedPatient));

        assertAll("bundle",
            () -> assertNotNull(bundle.getMeta().getLastUpdated()),
            () -> assertEquals(
                "https://fhir.nhs.uk/STU3/StructureDefinition/ITK-Message-Bundle-1",
                bundle.getMeta().getProfile().get(0).asStringValue()
            ),
            () -> assertEquals("https://tools.ietf.org/html/rfc4122", bundle.getIdentifier().getSystem()),
            () -> assertEquals(VALUE_UUID, bundle.getIdentifier().getValue()),
            () -> assertEquals(Bundle.BundleType.MESSAGE, bundle.getType()),
            () -> assertEquals(BUNDLE_ENTRY_SIZE, bundle.getEntry().size())
        );

        assertRequesterEntry(bundle);

        assertPatientEntry(bundle);
    }

    private void assertRequesterEntry(Bundle bundle) {
        Bundle.BundleEntryComponent bundleEntryComponentForRequesterResource = bundle.getEntry().get(0);
        assertAll("bundle.entry[0]",
            () -> assertNotNull(bundleEntryComponentForRequesterResource),
            () -> assertEquals("urn:uuid:" + ENTRY_UUID, bundleEntryComponentForRequesterResource.getFullUrl()));

        Practitioner requester = (Practitioner) bundleEntryComponentForRequesterResource.getResource();

        assertAll("requester",
            () -> assertThat(requester.getName())
                .hasSize(1)
                .first()
                .extracting(HumanName::getText)
                .isEqualTo(NAME_TEXT),
            () -> assertThat(requester.getGender().toCode())
                .isEqualTo("male")
        );
    }

    @Test
    void testMapPathologyRecordToBundleWithSpecimens() {
        final var mockSpecimen1 = mock(Specimen.class);
        final var mockSpecimen2 = mock(Specimen.class);
        final var pathologyRecord = PathologyRecord.builder()
            .specimens(List.of(mockSpecimen1, mockSpecimen2))
            .build();
        final var bundle = bundleMapper.mapToBundle(pathologyRecord);

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
                .allMatch(URL_IS_SOME_UUID)
        );
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private void assertPatientEntry(Bundle bundle) {

        final Bundle.BundleEntryComponent bundleEntryComponentForPatientResource = bundle.getEntry().get(1);
        assertAll("bundle.entry[1]",
            () -> assertNotNull(bundleEntryComponentForPatientResource),
            () -> assertEquals("urn:uuid:" + ENTRY_UUID, bundleEntryComponentForPatientResource.getFullUrl()));

        final Patient patient = (Patient) bundleEntryComponentForPatientResource.getResource();

        assertAll("patient",
            () -> assertNotNull(patient),
            () -> assertThat(patient.getId()).isEqualTo(ENTRY_UUID),
            () -> assertThat(patient.getName())
                .hasSize(1)
                .first()
                .extracting(HumanName::getText)
                .isEqualTo(NAME_TEXT),
            () -> assertThat(patient.getGender()).isEqualTo(GENDER),
            () -> assertThat(patient.getBirthDate())
                .isEqualTo(Date.from(LocalDate.of(2001, 1, 12).atStartOfDay()
                    .atZone(ZoneId.systemDefault()).toInstant())));
    }

    private void verifyBundle(Bundle bundle) {
        assertAll(
            () -> assertNotNull(bundle.getMeta().getLastUpdated()),
            () -> assertEquals(
                "https://fhir.nhs.uk/STU3/StructureDefinition/ITK-Message-Bundle-1",
                bundle.getMeta().getProfile().get(0).asStringValue()
            ),
            () -> assertEquals("https://tools.ietf.org/html/rfc4122", bundle.getIdentifier().getSystem()),
            () -> assertEquals("some-uuid", bundle.getIdentifier().getValue()),
            () -> assertEquals(Bundle.BundleType.MESSAGE, bundle.getType())
        );
    }
}
