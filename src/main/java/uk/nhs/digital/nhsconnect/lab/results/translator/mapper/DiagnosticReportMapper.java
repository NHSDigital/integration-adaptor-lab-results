package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;

@Component
public class DiagnosticReportMapper {
    public DiagnosticReport map(final Message message) {
        return new DiagnosticReport();
    }
}
