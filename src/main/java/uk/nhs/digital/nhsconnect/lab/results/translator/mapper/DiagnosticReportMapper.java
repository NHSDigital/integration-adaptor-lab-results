package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.DiagnosticReportDateIssued;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Reference;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ReportStatusCode;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.ServiceReportDetails;
import uk.nhs.digital.nhsconnect.lab.results.utils.ResourceFullUrlGenerator;
import uk.nhs.digital.nhsconnect.lab.results.utils.TimestampService;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DiagnosticReportMapper {

    private final UUIDGenerator uuidGenerator;
    private final ResourceFullUrlGenerator fullUrlGenerator;
    private final Map<ReportStatusCode, DiagnosticReport.DiagnosticReportStatus> statusMap = Map.of(
        ReportStatusCode.UNSPECIFIED, DiagnosticReport.DiagnosticReportStatus.UNKNOWN
    );

    public DiagnosticReport map(final Message message, Patient patient) {
        DiagnosticReport fhir = new DiagnosticReport();
        fhir.setId(uuidGenerator.generateUUID());

        ServiceReportDetails serviceReportDetails = message.getServiceReportDetails();

        // fhir.issued = SG2.DTM
        mapIssued(serviceReportDetails.getDateIssued(), fhir);
        // fhir.status
        fhir.setStatus(statusMap.get(serviceReportDetails.getStatus().getEvent()));
        // fhir.identifier
        mapIdentifier(serviceReportDetails.getReference(), fhir);
        Coding coding = new Coding();
        coding.setCode("721981007");
        coding.setSystem("http://snomed.info/sct");
        coding.setDisplay("Diagnostic studies report");
        // fhir.code
        fhir.setCode(new CodeableConcept().setCoding(List.of(coding)));
        // fhir.subject
        fhir.getSubject().setReference(fullUrlGenerator.generate(patient));

        /*
            TODO: Add the following
                - BasedOn - ProcedureReport
                - Performer - Practitioner/Organization
                - Specimen - could be a list?
                - Result - Observation
         */

        return fhir;
    }

    private void mapIssued(final DiagnosticReportDateIssued reportIssuedDate, final DiagnosticReport fhir) {
        LocalDateTime dateIssued = reportIssuedDate.getDateIssued();
        ZonedDateTime zonedDateTime = dateIssued.atZone(TimestampService.UK_ZONE);
        fhir.setIssued(Date.from(zonedDateTime.toInstant()));
    }

    private void mapIdentifier(final Reference reference, final DiagnosticReport fhir) {
        final Identifier identifier = new Identifier();

        identifier.setValue(reference.getNumber());

        fhir.addIdentifier(identifier);
    }
}
