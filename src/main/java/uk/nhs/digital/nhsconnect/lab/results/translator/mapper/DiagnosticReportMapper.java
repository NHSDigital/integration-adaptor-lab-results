package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Specimen;
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
    private static final Map<ReportStatusCode, DiagnosticReport.DiagnosticReportStatus> STATUS_MAP = Map.of(
        ReportStatusCode.UNSPECIFIED, DiagnosticReport.DiagnosticReportStatus.UNKNOWN);
    private static final String CODE_SYSTEM = "http://snomed.info/sct";
    private static final String CODE_DISPLAY = "Diagnostic studies report";
    private static final String CODE_NUMBER = "721981007";

    private final UUIDGenerator uuidGenerator;
    private final ResourceFullUrlGenerator fullUrlGenerator;

    public DiagnosticReport map(final Message message,
                                Patient patient,
                                List<Specimen> specimens) {
        DiagnosticReport fhir = new DiagnosticReport();
        fhir.setId(uuidGenerator.generateUUID());

        ServiceReportDetails serviceReportDetails = message.getServiceReportDetails();

        // fhir.issued = SG2.DTM
        mapIssued(serviceReportDetails.getDateIssued(), fhir);
        // fhir.status
        fhir.setStatus(STATUS_MAP.get(serviceReportDetails.getStatus().getEvent()));
        // fhir.identifier
        mapIdentifier(serviceReportDetails.getReference(), fhir);
        // fhir.code
        mapCode(fhir);

        // fhir.subject
        fhir.getSubject().setReference(fullUrlGenerator.generate(patient));
        // fhir.specimens
        mapSpecimens(specimens, fhir);

        /*
            TODO: Add the following
                - BasedOn - ProcedureReport
                - Performer - Practitioner/Organization
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

    private void mapSpecimens(final List<Specimen> specimens, final DiagnosticReport fhir) {
        specimens.forEach(specimen -> fhir.addSpecimen().setReference(fullUrlGenerator.generate(specimen)));
    }

    private void mapCode(final DiagnosticReport fhir) {
        fhir.getCode().addCoding()
            .setDisplay(CODE_DISPLAY)
            .setCode(CODE_NUMBER)
            .setSystem(CODE_SYSTEM);
    }
}
