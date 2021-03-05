package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import lombok.RequiredArgsConstructor;
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
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ReferenceType;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.TestStatus;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.TestStatusCode;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.InvestigationSubject;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.LabResult;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.ResultReferenceRange;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ObservationMapper {
    private static final String CODING_SYSTEM = "http://loinc.org";

    private static final Function<Boolean, List<LabResult>> EMPTY_LIST = $ -> Collections.emptyList();

    private final UUIDGenerator uuidGenerator;

    public List<Observation> mapToObservations(final Message message) {
        final InvestigationSubject subject = message.getServiceReportDetails().getSubject();

        final Map<Boolean, List<LabResult>> testGroupsAndResults = subject.getLabResults().stream()
            .collect(groupingBy(result -> result.getSequenceDetails().isPresent()));

        // test groups are GIS+N blocks with SEQ segments
        // they should have RFF+ASL:X where X is the SEQ value of the S16
        final List<LabResult> testGroups = testGroupsAndResults.computeIfAbsent(true, EMPTY_LIST);
        final var edifactToFhirIdMap = new HashMap<String, String>(testGroups.size());
        final List<Observation> mappedGroups = testGroups.stream()
            .map(labResult -> mapTestGroup(edifactToFhirIdMap, labResult))
            .collect(Collectors.toList());

        // test results are GIS+N blocks without SEQ segments
        // they should have RFF+ARL:Y where Y is the SEQ value of the test group
        final List<LabResult> testResults = testGroupsAndResults.computeIfAbsent(false, EMPTY_LIST);
        final List<Observation> mappedResults = testResults.stream()
            .map(labResult -> mepTestResult(edifactToFhirIdMap, labResult))
            .collect(Collectors.toList());

        return Stream.concat(mappedGroups.stream(), mappedResults.stream())
            .collect(Collectors.toList());
    }

    private Observation mepTestResult(Map<?, ? extends String> edifactToFhirIdMap, LabResult labResult) {
        final var result = new Observation();

        result.setId(uuidGenerator.generateUUID());

        mapContents(labResult, result);

        if (labResult.getSequenceReference().getTarget() == ReferenceType.INVESTIGATION) {
            final var edifactId = labResult.getSequenceReference().getNumber();
            final var fhirId = edifactToFhirIdMap.get(edifactId);
            result.addRelated().getTarget().setReference(fhirId);
        }

        return result;
    }

    private Observation mapTestGroup(Map<? super String, ? super String> edifactToFhirIdMap, LabResult labResult) {
        final var result = new Observation();

        final var fhirId = uuidGenerator.generateUUID();
        result.setId(fhirId);

        //noinspection OptionalGetWithoutIsPresent Previous logic guarantees its presence
        final var edifactId = labResult.getSequenceDetails().get().getNumber();
        edifactToFhirIdMap.put(edifactId, fhirId);

        mapContents(labResult, result);

        return result;
    }

    private void mapContents(final LabResult labResult, final Observation observation) {
        mapStatus(labResult, observation);
        mapValueQuantity(labResult, observation);
        mapCode(labResult, observation);
        mapReferenceRange(labResult, observation);
        mapComment(labResult, observation);
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
        final var coding = observation.getCode().addCoding();
        labResult.getInvestigation().getInvestigationCode().ifPresent(coding::setCode);
        coding.setDisplay(labResult.getInvestigation().getInvestigationDescription());
        coding.setSystem(CODING_SYSTEM);
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
