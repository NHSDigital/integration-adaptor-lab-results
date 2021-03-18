package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.ProcedureRequest.ProcedureRequestIntent;
import org.hl7.fhir.dstu3.model.ProcedureRequest.ProcedureRequestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.FreeTextSegment;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.ReportStatusCode;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.PatientClinicalInfo;
import uk.nhs.digital.nhsconnect.lab.results.utils.ResourceFullUrlGenerator;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ProcedureRequestMapper {
    private final UUIDGenerator uuidGenerator;
    private final ResourceFullUrlGenerator fullUrlGenerator;
    private static final Map<ReportStatusCode, ProcedureRequestStatus> STATUS_CODE_MAPPING = Map.of(
        ReportStatusCode.UNSPECIFIED, ProcedureRequestStatus.UNKNOWN
    );

    public Optional<ProcedureRequest> mapToProcedureRequest(
        final Message message,
        Patient patient,
        Practitioner requestingPractitioner,
        Organization requestingOrganization,
        Practitioner performingPractitioner,
        Organization performingOrganization
    ) {
        return new InternalMapper(message, patient, requestingPractitioner,
            requestingOrganization, performingPractitioner, performingOrganization).map();
    }

    @RequiredArgsConstructor
    private class InternalMapper {
        private final Message message;
        private final Patient patient;
        private final Practitioner requestingPractitioner;
        private final Organization requestingOrganization;
        private final Practitioner performingPractitioner;
        private final Organization performingOrganization;
        private PatientClinicalInfo patientClinicalInfo;
        private ProcedureRequest procedureRequest;

        public Optional<ProcedureRequest> map() {
            return message.getServiceReportDetails().getSubject().getClinicalInfo()
                .map(this::mapPatientClinicalInfo);
        }

        private ProcedureRequest mapPatientClinicalInfo(PatientClinicalInfo patientClinicalInfo) {
            this.patientClinicalInfo = patientClinicalInfo;
            this.procedureRequest = new ProcedureRequest();
            mapFreeText();
            mapStatus();
            procedureRequest.setIntent(ProcedureRequestIntent.ORDER);
            procedureRequest.setCode(new CodeableConcept().setText("unknown"));
            procedureRequest.setId(uuidGenerator.generateUUID());
            procedureRequest.getSubject().setReference(fullUrlGenerator.generate(patient));
            setRequesterReference();
            setPerformerReference();

            return procedureRequest;
        }

        private void mapFreeText() {
            List<FreeTextSegment> patientClinicalInfoFreeTexts = patientClinicalInfo.getFreeTexts();

            if (patientClinicalInfoFreeTexts.isEmpty()) {
                throw new FhirValidationException("Unable to map message. "
                    + "The FreeText segment is mandatory in Clinical Information");
            }

            final List<Annotation> annotations = patientClinicalInfoFreeTexts.stream()
                .map(FreeTextSegment::getTexts)
                .map(texts -> String.join(" ", texts))
                .map(MappingUtils::unescape)
                .map(text -> new Annotation().setText(text))
                .collect(Collectors.toList());

            procedureRequest.setNote(annotations);
        }

        private void mapStatus() {
            procedureRequest.setStatus(STATUS_CODE_MAPPING.get(
                ReportStatusCode.fromCode(patientClinicalInfo.getCode().getCode())));
        }

        private void setRequesterReference() {
            if (requestingOrganization != null) {
                procedureRequest.getRequester().getAgent()
                    .setReference(fullUrlGenerator.generate(requestingOrganization));
            } else if (requestingPractitioner != null) {
                procedureRequest.getRequester().getAgent()
                    .setReference(fullUrlGenerator.generate(requestingPractitioner));
            }
        }

        private void setPerformerReference() {
            if (performingOrganization != null) {
                procedureRequest.getPerformer().setReference(fullUrlGenerator.generate(performingOrganization));
            } else if (performingPractitioner != null) {
                procedureRequest.getPerformer().setReference(fullUrlGenerator.generate(performingPractitioner));
            }
        }
    }
}
