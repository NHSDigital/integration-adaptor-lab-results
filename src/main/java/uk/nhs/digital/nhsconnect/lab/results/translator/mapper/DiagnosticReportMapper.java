package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.DiagnosticReport.DiagnosticReportStatus;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Observation.ObservationRelationshipType;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.Specimen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.FhirProfiles;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.ServiceReportDetails;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.ReportStatusCode;
import uk.nhs.digital.nhsconnect.lab.results.utils.ResourceFullUrlGenerator;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static uk.nhs.digital.nhsconnect.lab.results.model.Constants.SNOMED_CODING_SYSTEM;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DiagnosticReportMapper {
    private static final Map<ReportStatusCode, DiagnosticReportStatus> STATUS_MAP = Map.of(
        ReportStatusCode.UNSPECIFIED, DiagnosticReportStatus.UNKNOWN,
        ReportStatusCode.PRELIMINARY, DiagnosticReportStatus.PRELIMINARY,
        ReportStatusCode.SUPPLEMENTARY, DiagnosticReportStatus.APPENDED
    );
    private static final String CODE_DISPLAY = "Diagnostic studies report";
    private static final String CODE_NUMBER = "721981007";

    private final UUIDGenerator uuidGenerator;
    private final ResourceFullUrlGenerator fullUrlGenerator;
    private final DateFormatMapper dateFormatMapper;

    public DiagnosticReport mapToDiagnosticReport(final Message message, Patient patient, List<Specimen> specimens,
                                                  List<Observation> observations, Practitioner performingPractitioner,
                                                  Organization performingOrganization,
                                                  ProcedureRequest procedureRequest) {

        return new InternalMapper(message, patient, specimens, observations, performingPractitioner,
            performingOrganization, procedureRequest).map();
    }

    @RequiredArgsConstructor
    private class InternalMapper {
        private final Message message;
        private final Patient patient;
        private final List<Specimen> specimens;
        private final List<Observation> observations;
        private final Practitioner performingPractitioner;
        private final Organization performingOrganization;
        private final ProcedureRequest procedureRequest;
        private DiagnosticReport diagnosticReport;
        private ServiceReportDetails serviceReportDetails;

        public DiagnosticReport map() {
            this.diagnosticReport = new DiagnosticReport();
            this.diagnosticReport.getMeta().addProfile(FhirProfiles.DIAGNOSTIC_REPORT);
            this.serviceReportDetails = message.getServiceReportDetails();

            diagnosticReport.setId(uuidGenerator.generateUUID());
            // diagnosticReport.issued = SG2.DTM
            mapIssued();
            // diagnosticReport.basedOn
            mapBasedOn();
            // diagnosticReport.status
            diagnosticReport.setStatus(STATUS_MAP.get(serviceReportDetails.getStatus().getEvent()));
            // diagnosticReport.identifier
            mapIdentifier();
            // diagnosticReport.code
            mapCode();
            // diagnosticReport.subject
            diagnosticReport.getSubject().setReference(fullUrlGenerator.generate(patient));
            // diagnosticReport.specimens
            mapSpecimens();
            // diagnosticReport.results
            mapObservations();
            // diagnosticReport.performer
            mapPerformer();

            return diagnosticReport;
        }

        private void mapBasedOn() {
            Optional.ofNullable(procedureRequest).ifPresent(request ->
                diagnosticReport.addBasedOn().setReference(fullUrlGenerator.generate(request)));
        }

        private void mapIssued() {
            Date dateIssued = dateFormatMapper.mapToDate(
                serviceReportDetails.getDateIssued().getDateFormat(),
                serviceReportDetails.getDateIssued().getDateIssued()
            );

            diagnosticReport.setIssued(dateIssued);
        }

        private void mapIdentifier() {
            diagnosticReport.addIdentifier()
                .setValue(serviceReportDetails.getReference().getNumber())
                .setSystem("https://tools.ietf.org/html/rfc4122");
        }

        private void mapSpecimens() {
            specimens.stream()
                .map(fullUrlGenerator::generate)
                .forEach(reference -> diagnosticReport.addSpecimen().setReference(reference));
        }

        private void mapCode() {
            diagnosticReport.getCode().addCoding()
                .setDisplay(CODE_DISPLAY)
                .setCode(CODE_NUMBER)
                .setSystem(SNOMED_CODING_SYSTEM);
        }

        private void mapPerformer() {
            Stream.of(performingOrganization, performingPractitioner)
                .map(fullUrlGenerator::generate)
                .forEach(performerUrl -> diagnosticReport.addPerformer().getActor().setReference(performerUrl));
        }

        private void mapObservations() {
            final Predicate<Observation> isTestGroup = ob -> ob.getRelated().stream()
                .anyMatch(relation -> relation.getType() == ObservationRelationshipType.HASMEMBER);
            final Predicate<Observation> isUngroupedResult = ob -> ob.getRelated().isEmpty();

            observations.stream()
                .filter(isTestGroup.or(isUngroupedResult))
                .map(fullUrlGenerator::generate)
                .forEach(reference -> diagnosticReport.addResult().setReference(reference));
        }
    }
}
