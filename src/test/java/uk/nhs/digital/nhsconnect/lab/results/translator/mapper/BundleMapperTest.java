package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.nhs.digital.nhsconnect.lab.results.fixtures.FhirFixtures.generatePractitioner;
import static uk.nhs.digital.nhsconnect.lab.results.fixtures.PathologyRecordFixtures.generatePathologyRecord;

@ExtendWith(MockitoExtension.class)
class BundleMapperTest {

    @Mock
    private UUIDGenerator uuidGenerator;

    @InjectMocks
    private BundleMapper bundleMapper;

    @Test
    void testMapPathologyRecordToBundle() {
        when(uuidGenerator.generateUUID()).thenReturn("some-value-uuid").thenReturn("some-entry-uuid");

        Practitioner generatedRequester = generatePractitioner("Dr Bob Hope", Enumerations.AdministrativeGender.MALE);
        Practitioner generatedPerformer = generatePractitioner("Dr Darcy Lewis",
            Enumerations.AdministrativeGender.FEMALE);
        final Bundle bundle = bundleMapper.mapToBundle(generatePathologyRecord(generatedRequester, generatedPerformer));

        assertAll(
            () -> assertNotNull(bundle.getMeta().getLastUpdated()),
            () -> assertEquals(
                    "https://fhir.nhs.uk/STU3/StructureDefinition/ITK-Message-Bundle-1",
                    bundle.getMeta().getProfile().get(0).asStringValue()
            ),
            () -> assertEquals("https://tools.ietf.org/html/rfc4122", bundle.getIdentifier().getSystem()),
            () -> assertEquals("some-value-uuid", bundle.getIdentifier().getValue()),
            () -> assertEquals(Bundle.BundleType.MESSAGE, bundle.getType()),
            () -> assertEquals(2, bundle.getEntry().size())
        );

        Bundle.BundleEntryComponent bundleEntryComponentForRequesterResource = bundle.getEntry().get(0);
        Practitioner requester = (Practitioner) bundleEntryComponentForRequesterResource.getResource();

        assertAll(
            () -> assertEquals("urn:uuid:some-entry-uuid", bundleEntryComponentForRequesterResource.getFullUrl()),
            () -> assertThat(requester.getName())
                    .hasSize(1)
                    .first()
                    .extracting(HumanName::getText)
                    .isEqualTo("Dr Bob Hope"),
            () -> assertThat(requester.getGender().toCode())
                    .isEqualTo("male")
        );

        Bundle.BundleEntryComponent bundleEntryComponentForPerformerResource = bundle.getEntry().get(1);
        Practitioner performer = (Practitioner) bundleEntryComponentForPerformerResource.getResource();

        assertAll(
            () -> assertEquals("urn:uuid:some-entry-uuid", bundleEntryComponentForPerformerResource.getFullUrl()),
            () -> assertThat(performer.getName())
                .hasSize(1)
                .first()
                .extracting(HumanName::getText)
                .isEqualTo("Dr Darcy Lewis"),
            () -> assertThat(performer.getGender().toCode())
                .isEqualTo("female")
        );
    }
}
