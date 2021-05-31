package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.ProcedureRequest.ProcedureRequestIntent;
import org.hl7.fhir.dstu3.model.ProcedureRequest.ProcedureRequestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.FhirProfiles;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.FreeTextSegment;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.PatientClinicalInfo;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.ReportStatusCode;
import uk.nhs.digital.nhsconnect.lab.results.utils.ResourceFullUrlGenerator;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.nhs.digital.nhsconnect.lab.results.model.Constants.SNOMED_CODING_SYSTEM;
import static uk.nhs.digital.nhsconnect.lab.results.model.Constants.SNOMED_LABORATORY_TEST_CODE;
import static uk.nhs.digital.nhsconnect.lab.results.model.Constants.SNOMED_LABORATORY_TEST_DISPLAY;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ProcedureRequestMapper {
    private final UUIDGenerator uuidGenerator;
    private final ResourceFullUrlGenerator fullUrlGenerator;
    private static final Map<ReportStatusCode, ProcedureRequestStatus> STATUS_CODE_MAPPING = Map.of(
        ReportStatusCode.UNSPECIFIED, ProcedureRequestStatus.UNKNOWN
    );

    public ProcedureRequest mapToProcedureRequest(
            final Message message,
            Patient patient,
            Practitioner requestingPractitioner,
            Organization requestingOrganization,
            Practitioner performingPractitioner) {
        return new InternalMapper(
            message, patient, requestingPractitioner, requestingOrganization, performingPractitioner
        ).map();
    }

    @RequiredArgsConstructor
    private class InternalMapper {
        private final Message message;
        private final Patient patient;
        private final Practitioner requestingPractitioner;
        private final Organization requestingOrganization;
        private final Practitioner performingPractitioner;
        private PatientClinicalInfo patientClinicalInfo;
        private ProcedureRequest procedureRequest;

        public ProcedureRequest map() {
            return message.getServiceReportDetails().getSubject().getClinicalInfo()
                .map(this::mapPatientClinicalInfo)
                .orElseGet(this::buildBareProcedureRequest);
        }

        private ProcedureRequest buildBareProcedureRequest() {
            var procedureRequest = new ProcedureRequest();
            procedureRequest.getMeta().addProfile(FhirProfiles.PROCEDURE_REQUEST);
            procedureRequest.setId(uuidGenerator.generateUUID());
            procedureRequest.setStatus(ProcedureRequestStatus.ACTIVE);
            procedureRequest.setIntent(ProcedureRequestIntent.ORDER);
            procedureRequest.setCode(new CodeableConcept().addCoding(
                new Coding()
                    .setCode(SNOMED_LABORATORY_TEST_CODE)
                    .setSystem(SNOMED_CODING_SYSTEM)
                    .setDisplay(SNOMED_LABORATORY_TEST_DISPLAY))
            );
            procedureRequest.getSubject().setReference(fullUrlGenerator.generate(this.patient));
            setRequesterReference(procedureRequest);
            setPerformerReference(procedureRequest);
            return procedureRequest;
        }

        private ProcedureRequest mapPatientClinicalInfo(PatientClinicalInfo patientClinicalInfo) {
            this.procedureRequest = buildBareProcedureRequest();
            this.patientClinicalInfo = patientClinicalInfo;
            mapFreeText();
            mapStatus();

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

        private void setRequesterReference(ProcedureRequest procedureRequest) {
            procedureRequest.getRequester()
                .getOnBehalfOf()
                .setReference(fullUrlGenerator.generate(this.requestingOrganization));
            procedureRequest.getRequester()
                .getAgent()
                .setReference(fullUrlGenerator.generate(this.requestingPractitioner));
        }

        private void setPerformerReference(ProcedureRequest procedureRequest) {
            procedureRequest.getPerformer()
                .setReference(fullUrlGenerator.generate(this.performingPractitioner));
        }
    }
}
