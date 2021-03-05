package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Patient;
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

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ProcedureRequestMapper {
    private final UUIDGenerator uuidGenerator;
    private final ResourceFullUrlGenerator fullUrlGenerator;
    private static final Map<ReportStatusCode, ProcedureRequest.ProcedureRequestStatus> STATUS_CODE_MAPPING = Map.of(
        ReportStatusCode.UNSPECIFIED, ProcedureRequest.ProcedureRequestStatus.UNKNOWN
    );

    public Optional<ProcedureRequest> mapToProcedureRequest(final Message message, Patient patient) {
        return message.getServiceReportDetails().getSubject().getClinicalInfo()
            .map(clinicalInfo -> mapPatientClinicalInfo(clinicalInfo, patient));
    }

    private ProcedureRequest mapPatientClinicalInfo(final PatientClinicalInfo patientClinicalInfo, Patient patient) {
        final ProcedureRequest procedureRequest = new ProcedureRequest();

        mapFreeText(patientClinicalInfo, procedureRequest);
        mapStatus(patientClinicalInfo, procedureRequest);
        procedureRequest.setIntent(ProcedureRequest.ProcedureRequestIntent.NULL);
        procedureRequest.setCode(new CodeableConcept().setText("unknown"));
        procedureRequest.setId(uuidGenerator.generateUUID());
        procedureRequest.getSubject().setReference(fullUrlGenerator.generate(patient));

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
        Optional.ofNullable(patientClinicalInfo)
            .ifPresent(n -> procedureRequest.setStatus(STATUS_CODE_MAPPING.get(
                ReportStatusCode.fromCode(patientClinicalInfo.getCode().getCode()))));
    }
}
