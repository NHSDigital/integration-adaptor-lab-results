package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Identifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.DiagnosticReportDateIssued;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Reference;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ReportStatusCode;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.ServiceReportDetails;
import uk.nhs.digital.nhsconnect.lab.results.utils.TimestampService;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DiagnosticReportMapper {

    private final UUIDGenerator uuidGenerator;
    private final Map<ReportStatusCode, DiagnosticReport.DiagnosticReportStatus> statusMap = Map.of(
        ReportStatusCode.UNSPECIFIED, DiagnosticReport.DiagnosticReportStatus.UNKNOWN
    );

    public DiagnosticReport map(final Message message) {
        DiagnosticReport report = new DiagnosticReport();
        ServiceReportDetails serviceReportDetails = message.getServiceReportDetails();

        report.setId(uuidGenerator.generateUUID());

        mapDateToLong(serviceReportDetails.getDateIssued(), report);
        report.setStatus(statusMap.get(serviceReportDetails.getStatus().getEvent()));
        mapIdentifier(serviceReportDetails.getReference(), report);

        Coding coding = new Coding();
        coding.setCode("721981007");
        coding.setSystem("http://snomed.info/sct");
        coding.setDisplay("Diagnostic studies report");

        report.setCode(new CodeableConcept().setCoding(List.of(coding)));

        /*
            TODO: Add the following
                - BasedOn
                - Subject
                - Performer
                - Specimen
                - Result
         */

        return report;
    }

    private void mapDateToLong(final DiagnosticReportDateIssued reportIssuedDate, final DiagnosticReport diagnosticReport) {
        LocalDateTime dateIssued = reportIssuedDate.getDateIssued();
        ZonedDateTime zonedDateTime = dateIssued.atZone(TimestampService.UK_ZONE);

        diagnosticReport.getIssued().setTime(zonedDateTime.toInstant().toEpochMilli());
    }

    private void mapIdentifier(final Reference reference, final DiagnosticReport diagnosticReport) {
        final Identifier identifier = new Identifier();

        identifier.setValue(reference.getNumber());

        diagnosticReport.addIdentifier(identifier);
    }
}
