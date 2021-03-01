package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.FreeTextSegment;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ReportStatusCode;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.PatientClinicalInfo;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ProcedureRequestMapper {
    private final UUIDGenerator uuidGenerator;

    public Optional<ProcedureRequest> mapToProcedureRequest(final Message message) {
        return message.getPatientClinicalInfo().stream()
            .map(patientClinicalInfo -> mapPatientClinicalInfo(patientClinicalInfo))
            .findAny();
    }

    private ProcedureRequest mapPatientClinicalInfo(final PatientClinicalInfo patientClinicalInfo) {
        final ProcedureRequest procedureRequest = new ProcedureRequest();

        mapFreeText(patientClinicalInfo, procedureRequest);
        mapStatus(patientClinicalInfo, procedureRequest);
        procedureRequest.setIntent(ProcedureRequest.ProcedureRequestIntent.NULL);
        procedureRequest.setCode(new CodeableConcept());
        procedureRequest.setId(uuidGenerator.generateUUID());

        return procedureRequest;
    }

    private void mapFreeText(final PatientClinicalInfo patientClinicalInfo, final ProcedureRequest procedureRequest) {
        final ArrayList<Annotation> annotations = new ArrayList<>();

        var textualClinicalInformationList = patientClinicalInfo.getFreeTexts()
            .stream()
            .map(FreeTextSegment::getTexts)
            .collect(Collectors.toList());

        for (var freeTextItem: textualClinicalInformationList) {
            Annotation annotation = new Annotation();
            annotation.setText(freeTextItem[0]);
            annotations.add(annotation);
        }

        if (annotations.isEmpty()) {
            throw new FhirValidationException("Unable to map message. "
                + "The FreeText segment is mandatory in Clinical Information");
        }

        procedureRequest.setNote(annotations);
    }

    private void mapStatus(final PatientClinicalInfo patientClinicalInfo, final ProcedureRequest procedureRequest) {
        Optional.ofNullable(patientClinicalInfo)
            .ifPresent(n -> procedureRequest.setStatus(
                ReportStatusCode.mapToProcedureRequestStatus(
                    ReportStatusCode.fromCode(patientClinicalInfo.getCode().getCode()))));
    }
}
