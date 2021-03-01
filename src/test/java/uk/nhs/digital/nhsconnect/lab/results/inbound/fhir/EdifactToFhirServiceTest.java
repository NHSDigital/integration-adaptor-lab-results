package uk.nhs.digital.nhsconnect.lab.results.inbound.fhir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import static uk.nhs.digital.nhsconnect.lab.results.fixtures.FhirFixtures.generateBundle;
import static uk.nhs.digital.nhsconnect.lab.results.fixtures.PathologyRecordFixtures.generatePathologyRecord;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Patient;
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

    private static final int BUNDLE_SIZE = 3;

    @Test
    void testEdifactIsMappedToFhirBundle() {
        PathologyRecord pathologyRecord = generatePathologyRecord(new Practitioner(),
            new Practitioner(), new Patient());

        Bundle generatedBundle = generateBundle(pathologyRecord);

        when(pathologyRecordMapper.mapToPathologyRecord(message)).thenReturn(pathologyRecord);
        when(bundleMapper.mapToBundle(pathologyRecord)).thenReturn(generatedBundle);

        final Bundle bundle = service.convertToFhir(message);

        assertThat(bundle).isNotNull();
        assertThat(bundle.getEntry())
            .hasSize(BUNDLE_SIZE)
            .extracting(Bundle.BundleEntryComponent::getResource)
            .containsExactly(pathologyRecord.getRequester(),
                pathologyRecord.getPerformer(),
                pathologyRecord.getPatient());
    }
}
