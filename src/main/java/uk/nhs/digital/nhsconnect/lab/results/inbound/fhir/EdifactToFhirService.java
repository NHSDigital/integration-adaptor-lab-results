package uk.nhs.digital.nhsconnect.lab.results.inbound.fhir;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.MedicalReport;
import uk.nhs.digital.nhsconnect.lab.results.translator.mapper.BundleMapper;
import uk.nhs.digital.nhsconnect.lab.results.translator.mapper.MedicalReportMapper;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EdifactToFhirService {

    private final MedicalReportMapper medicalReportMapper;
    private final BundleMapper bundleMapper;

    public Bundle convertToFhir(final Message message) {
        MedicalReport medicalReport = medicalReportMapper.mapToMedicalReport(message);

        return bundleMapper.mapToBundle(medicalReport);
    }
}
