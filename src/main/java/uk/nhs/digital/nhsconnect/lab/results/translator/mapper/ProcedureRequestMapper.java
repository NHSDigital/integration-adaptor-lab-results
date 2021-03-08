package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.FreeTextSegment;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ReportStatusCode;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.PatientClinicalInfo;
import uk.nhs.digital.nhsconnect.lab.results.utils.ResourceFullUrlGenerator;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.ProcedureRequest.ProcedureRequestStatus;
import org.hl7.fhir.dstu3.model.ProcedureRequest.ProcedureRequestIntent;

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
        return message.getServiceReportDetails().getSubject().getClinicalInfo()
            .map(clinicalInfo -> mapPatientClinicalInfo(clinicalInfo, patient, requestingPractitioner,
                requestingOrganization, performingPractitioner, performingOrganization));
    }

    private ProcedureRequest mapPatientClinicalInfo(
        final PatientClinicalInfo patientClinicalInfo,
        Patient patient, Practitioner requestingPractitioner,
        Organization requestingOrganization,
        Practitioner performingPractitioner,
        Organization performingOrganization
    ) {
        final ProcedureRequest procedureRequest = new ProcedureRequest();

        mapFreeText(patientClinicalInfo, procedureRequest);
        mapStatus(patientClinicalInfo, procedureRequest);
        procedureRequest.setIntent(ProcedureRequestIntent.NULL);
        procedureRequest.setCode(new CodeableConcept().setText("unknown"));
        procedureRequest.setId(uuidGenerator.generateUUID());
        procedureRequest.getSubject().setReference(fullUrlGenerator.generate(patient));
        setRequesterReference(requestingPractitioner, requestingOrganization, procedureRequest);
        setPerformerReference(performingPractitioner, performingOrganization, procedureRequest);

        return procedureRequest;
    }

    private void mapFreeText(final PatientClinicalInfo patientClinicalInfo, final ProcedureRequest procedureRequest) {
        final List<Annotation> annotations = patientClinicalInfo.getFreeTexts().stream()
            .map(FreeTextSegment::getTexts)
            .map(texts -> String.join(" ", texts))
            .map(MappingUtils::unescape)
            .map(text -> new Annotation().setText(text))
            .collect(Collectors.toList());

        if (annotations.isEmpty()) {
            throw new FhirValidationException("Unable to map message. "
                + "The FreeText segment is mandatory in Clinical Information");
        }

        procedureRequest.setNote(annotations);
    }

    private void mapStatus(final PatientClinicalInfo patientClinicalInfo, final ProcedureRequest procedureRequest) {
        procedureRequest.setStatus(STATUS_CODE_MAPPING.get(
            ReportStatusCode.fromCode(patientClinicalInfo.getCode().getCode())));
    }

    private void setRequesterReference(
        Practitioner requestingPractitioner,
        Organization requestingOrganization,
        final ProcedureRequest procedureRequest
    ) {
        if (requestingOrganization != null) {
            procedureRequest.getRequester().getAgent()
                .setReference(fullUrlGenerator.generate(requestingOrganization));
        } else if (requestingPractitioner != null) {
            procedureRequest.getRequester().getAgent()
                .setReference(fullUrlGenerator.generate(requestingPractitioner));
        }
    }

    private void setPerformerReference(
        Practitioner performingPractitioner,
        Organization performingOrganization,
        final ProcedureRequest procedureRequest
    ) {
        if (performingOrganization != null) {
            procedureRequest.getPerformer().setReference(fullUrlGenerator.generate(performingOrganization));
        } else if (performingPractitioner != null) {
            procedureRequest.getPerformer().setReference(fullUrlGenerator.generate(performingPractitioner));
        }
    }
}
