package uk.nhs.digital.nhsconnect.lab.results.inbound.fhir;

import org.hl7.fhir.dstu3.model.Bundle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.MedicalReport;
import uk.nhs.digital.nhsconnect.lab.results.translator.mapper.BundleMapper;
import uk.nhs.digital.nhsconnect.lab.results.translator.mapper.MedicalReportMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EdifactToFhirServiceTest {

    @Mock
    private MedicalReportMapper medicalReportMapper;

    @Mock
    private BundleMapper bundleMapper;

    @InjectMocks
    private EdifactToFhirService service;

    @Test
    void testEdifactIsMappedToFhirBundle() {
        final var message = mock(Message.class);
        final var medicalReport = mock(MedicalReport.class);
        final var generatedBundle = mock(Bundle.class);
        when(medicalReportMapper.mapToMedicalReport(message)).thenReturn(medicalReport);
        when(bundleMapper.mapToBundle(medicalReport)).thenReturn(generatedBundle);

        final Bundle bundle = service.convertToFhir(message);

        assertAll(
            () -> assertThat(bundle).isSameAs(generatedBundle),
            () -> verifyNoInteractions(medicalReport),
            () -> verifyNoInteractions(generatedBundle)
        );
    }
}
