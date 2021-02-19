package uk.nhs.digital.nhsconnect.lab.results.inbound.fhir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.nhs.digital.nhsconnect.lab.results.fixtures.FhirFixtures.generateBundle;
import static uk.nhs.digital.nhsconnect.lab.results.fixtures.FhirFixtures.generateRequester;
import static uk.nhs.digital.nhsconnect.lab.results.fixtures.PathologyRecordFixtures.generatePathologyRecord;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.fhir.PathologyRecord;
import uk.nhs.digital.nhsconnect.lab.results.translator.mapper.BundleMapper;
import uk.nhs.digital.nhsconnect.lab.results.translator.mapper.PathologyRecordMapper;

@ExtendWith(MockitoExtension.class)
class EdifactToFhirServiceTest {

    @Mock
    private PathologyRecordMapper pathologyRecordMapper;

    @Mock
    private BundleMapper bundleMapper;

    @Mock
    private Message message;

    @InjectMocks
    private EdifactToFhirService service;

    @Test
    void testConvertEdifactToFhirRequesterMapperReturnsSomething() {
        Practitioner requester = generateRequester("Dr Bob Hope", Enumerations.AdministrativeGender.MALE);
        PathologyRecord pathologyRecord = generatePathologyRecord(requester);

        when(pathologyRecordMapper.mapToPathologyRecord(message)).thenReturn(pathologyRecord);
        when(bundleMapper.mapToBundle(pathologyRecord)).thenReturn(generateBundle(requester));

        final Bundle bundle = service.convertToFhir(message);

        assertThat(bundle).isNotNull();
        assertThat(bundle.getEntry())
            .hasSize(1)
            .first()
            .extracting(Bundle.BundleEntryComponent::getResource)
            .isNotNull();
    }
}
