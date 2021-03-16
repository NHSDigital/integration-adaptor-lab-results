package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.DiagnosticReport.DiagnosticReportStatus;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Observation.ObservationRelationshipType;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.Specimen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
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
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static uk.nhs.digital.nhsconnect.lab.results.model.Constants.SNOMED_CODING_SYSTEM;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DiagnosticReportMapper {
    private static final Map<ReportStatusCode, DiagnosticReportStatus> STATUS_MAP = Map.of(
        ReportStatusCode.UNSPECIFIED, DiagnosticReportStatus.UNKNOWN);
    private static final String CODE_DISPLAY = "Diagnostic studies report";
    private static final String CODE_NUMBER = "721981007";

    private final UUIDGenerator uuidGenerator;
    private final ResourceFullUrlGenerator fullUrlGenerator;

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
        private DiagnosticReport fhir;
        private ServiceReportDetails serviceReportDetails;

        public DiagnosticReport map() {
            this.fhir = new DiagnosticReport();
            this.serviceReportDetails = message.getServiceReportDetails();

            fhir.setId(uuidGenerator.generateUUID());
            // fhir.issued = SG2.DTM
            mapIssued();
            // fhir.basedOn
            mapBasedOn();
            // fhir.status
            fhir.setStatus(STATUS_MAP.get(serviceReportDetails.getStatus().getEvent()));
            // fhir.identifier
            mapIdentifier();
            // fhir.code
            mapCode();
            // fhir.subject
            fhir.getSubject().setReference(fullUrlGenerator.generate(patient));
            // fhir.specimens
            mapSpecimens();
            // fhir.results
            mapObservations();
            // fhir.performer
            mapPerformer();

            return fhir;
        }

        private void mapBasedOn() {
            Optional.ofNullable(procedureRequest).ifPresent(request ->
                fhir.addBasedOn().setReference(fullUrlGenerator.generate(request)));
        }

        private void mapIssued() {
            LocalDateTime dateIssued = serviceReportDetails.getDateIssued().getDateIssued();
            ZonedDateTime zonedDateTime = dateIssued.atZone(TimestampService.UK_ZONE);
            fhir.setIssued(Date.from(zonedDateTime.toInstant()));
        }

        private void mapIdentifier() {
            final Identifier identifier = new Identifier();
            identifier.setValue(serviceReportDetails.getReference().getNumber());
            fhir.addIdentifier(identifier);
        }

        private void mapSpecimens() {
            specimens.stream()
                .map(fullUrlGenerator::generate)
                .forEach(reference -> fhir.addSpecimen().setReference(reference));
        }

        private void mapCode() {
            fhir.getCode().addCoding()
                .setDisplay(CODE_DISPLAY)
                .setCode(CODE_NUMBER)
                .setSystem(SNOMED_CODING_SYSTEM);
        }

        private void mapPerformer() {
            Stream.of(performingOrganization, performingPractitioner)
                .filter(Objects::nonNull)
                .map(fullUrlGenerator::generate)
                .forEach(performerUrl -> fhir.addPerformer().getActor().setReference(performerUrl));
        }

        private void mapObservations() {
            final Predicate<Observation> isTestGroup =  ob -> ob.getRelated().stream()
                .anyMatch(relation -> relation.getType() == ObservationRelationshipType.HASMEMBER);
            final Predicate<Observation> isUngroupedResult = ob -> ob.getRelated().isEmpty();

            observations.stream()
                .filter(isTestGroup.or(isUngroupedResult))
                .map(fullUrlGenerator::generate)
                .forEach(reference -> fhir.addResult().setReference(reference));
        }
    }
}
