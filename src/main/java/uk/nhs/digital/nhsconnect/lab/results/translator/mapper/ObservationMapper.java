package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Observation.ObservationReferenceRangeComponent;
import org.hl7.fhir.dstu3.model.Observation.ObservationStatus;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Quantity.QuantityComparator;
import org.hl7.fhir.dstu3.model.SimpleQuantity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.FreeTextSegment;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.MeasurementValueComparator;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.TestStatus;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.TestStatusCode;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.InvestigationSubject;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.LabResult;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.ResultReferenceRange;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor(onConstructor_ = @Autowired)
public class ObservationMapper {
    private final UUIDGenerator uuidGenerator;

    public List<Observation> mapToTestGroupsAndResults(final Message message) {
        // test groups are GIS+N blocks with SEQ segments
        // they should have RFF+ASL:X where X is the SEQ value of the S16

        // test results are GIS+N blocks without SEQ segments
        // they should have RFF+ARL:Y where Y is the SEQ value of the test group

        final InvestigationSubject subject = message.getServiceReportDetails().getSubject();
        final List<LabResult> labResults = subject.getLabResults();

        // start by assuming everything is a test result
        return labResults.stream().map(labResult -> {
            final var result = new Observation();

            result.setId(uuidGenerator.generateUUID());

            mapStatus(labResult, result);
            mapValueQuantity(labResult, result);
            mapCode(labResult, result);
            mapReferenceRange(labResult, result);
            mapComment(labResult, result);

            return result;
        })
            .collect(Collectors.toList());
    }

    private void mapStatus(final LabResult labResult, final Observation observation) {
        // Observation.status = SG18.STS.C555.9011
        final var status = labResult.getTestStatus()
            .map(TestStatus::getTestStatusCode)
            .map(TestStatusCode::getDescription)
            .map(String::toLowerCase)
            .map(ObservationStatus::fromCode)
            .orElse(ObservationStatus.UNKNOWN);

        observation.setStatus(status);
    }

    private void mapValueQuantity(final LabResult labResult, final Observation observation) {
        // Observation.value.valueQuantity.*
        labResult.getInvestigationResult().ifPresent(investigationResult -> {
            final var quantity = new Quantity();

            // Observation.value.valueQuantity.value = SG18.RSL.C830(1).6314
            quantity.setValue(investigationResult.getMeasurementValue());

            // Observation.value.valueQuantity.unit = SG18.RSL.C848.6410
            quantity.setUnit(investigationResult.getMeasurementUnit());

            // Observation.value.valueQuantity.comparator = SG18.RSL.C830(1).6321
            investigationResult.getMeasurementValueComparator()
                .map(MeasurementValueComparator::getDescription)
                .map(QuantityComparator::fromCode)
                .ifPresent(quantity::setComparator);

            observation.setValue(quantity);
        });
    }

    private void mapCode(final LabResult labResult, final Observation observation) {
        // Observation.code = SG18.INV.C847.9930 and SG18.INV.C847.9931
        observation.getCode().setText(labResult.getInvestigation().getInvestigationDescription());
        observation.getCode().addCoding().setCode(labResult.getInvestigation().getInvestigationCode());
    }

    private void mapReferenceRange(final LabResult labResult, final Observation observation) {
        // Observation.referenceRange.*
        labResult.getResultReferenceRanges().stream()
            .map(ResultReferenceRange::getDetails)
            .map(rangeDetail -> {
                final var range = new ObservationReferenceRangeComponent();

                // Observation.referenceRange.low = SG20.RND.6162
                rangeDetail.getLowerLimit()
                    .map(this::toSimpleQuantity)
                    .ifPresent(range::setLow);

                // Observation.referenceRange.high = SG20.RND.6152
                rangeDetail.getUpperLimit()
                    .map(this::toSimpleQuantity)
                    .ifPresent(range::setHigh);

                return range;
            })
            .forEach(observation::addReferenceRange);
    }

    private void mapComment(final LabResult labResult, final Observation observation) {
        // Observation.comment = SG18.FTX.C108.4440(1-5)
        Optional.of(labResult.getFreeTexts().stream()
            .map(FreeTextSegment::getTexts)
            .map(texts -> String.join(" ", texts))
            .collect(Collectors.joining("\n")))
            .filter(StringUtils::isNotBlank)
            .ifPresent(observation::setComment);
    }

    private SimpleQuantity toSimpleQuantity(final BigDecimal value) {
        final var simpleQuantity = new SimpleQuantity();
        simpleQuantity.setValue(value);
        return simpleQuantity;
    }
}
