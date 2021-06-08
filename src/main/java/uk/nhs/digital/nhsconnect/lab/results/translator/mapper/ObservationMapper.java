package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Observation.ObservationReferenceRangeComponent;
import org.hl7.fhir.dstu3.model.Observation.ObservationRelatedComponent;
import org.hl7.fhir.dstu3.model.Observation.ObservationRelationshipType;
import org.hl7.fhir.dstu3.model.Observation.ObservationStatus;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Quantity.QuantityComparator;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.SimpleQuantity;
import org.hl7.fhir.dstu3.model.Specimen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.FhirProfiles;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.FreeTextSegment;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.LaboratoryInvestigationResult;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.RangeDetail;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.TestStatus;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.InvestigationSubject;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.LabResult;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.ResultReferenceRange;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.CodingType;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.DeviatingResultIndicator;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.LaboratoryInvestigationResultType;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.MeasurementValueComparator;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.TestStatusCode;
import uk.nhs.digital.nhsconnect.lab.results.utils.ResourceFullUrlGenerator;
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
import static uk.nhs.digital.nhsconnect.lab.results.model.Constants.READ_CODING_SYSTEM;
import static uk.nhs.digital.nhsconnect.lab.results.model.Constants.SNOMED_CODING_SYSTEM;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ObservationMapper {

    private static final Map<CodingType, String> CODING_TYPE_SYSTEMS = Map.of(
        CodingType.SNOMED_CT_CODE, SNOMED_CODING_SYSTEM,
        CodingType.READ_CODE, READ_CODING_SYSTEM
    );

    private static final Function<Boolean, List<LabResult>> EMPTY_LIST = $ -> Collections.emptyList();

    private final UUIDGenerator uuidGenerator;
    private final ResourceFullUrlGenerator fullUrlGenerator;

    public List<Observation> mapToObservations(final Message message, final Patient patient,
                                               final Map<String, Specimen> specimenIds,
                                               final Organization organization,
                                               final Practitioner practitioner) {
        return new InternalMapper(message, patient, specimenIds, organization, practitioner).map();
    }

    @RequiredArgsConstructor
    private class InternalMapper {

        private final Map<String, String> edifactToFhirIdMap = new HashMap<>();
        private final Map<String, Observation> testGroupsById = new HashMap<>();

        private final Message message;
        private final Patient patient;
        private final Map<String, Specimen> specimenIds;
        private final Organization organization;
        private final Practitioner practitioner;

        List<Observation> map() {
            final InvestigationSubject subject = message.getServiceReportDetails().getSubject();

            final Map<Boolean, List<LabResult>> testGroupsAndResults = subject.getLabResults().stream()
                .collect(groupingBy(result -> result.getSequenceDetails().isPresent()));

            // test groups are GIS+N blocks with SEQ segments
            // they should have RFF+ASL:X where X is the SEQ value of the S16
            final List<LabResult> testGroups = testGroupsAndResults.computeIfAbsent(true, EMPTY_LIST);
            testGroups.forEach(this::addTestGroup);

            // test results are GIS+N blocks without SEQ segments
            // they should have RFF+ARL:Y where Y is the SEQ value of the test group
            final List<LabResult> testResults = testGroupsAndResults.computeIfAbsent(false, EMPTY_LIST);
            final List<Observation> mappedResults = testResults.stream()
                .map(this::mapTestResult)
                .collect(Collectors.toList());

            return Stream.concat(testGroupsById.values().stream(), mappedResults.stream())
                .collect(Collectors.toList());
        }

        private void addTestGroup(final LabResult labResult) {
            final var observation = buildBareObservation();
            final var observationId = observation.getId();

            //noinspection OptionalGetWithoutIsPresent Previous logic guarantees its presence
            final var edifactId = labResult.getSequenceDetails().get().getNumber();
            edifactToFhirIdMap.put(edifactId, observationId);

            mapContents(labResult, observation);

            testGroupsById.put(observationId, observation);
        }

        private Observation mapTestResult(final LabResult labResult) {
            final var observation = buildBareObservation();

            mapContents(labResult, observation);

            final var edifactId = labResult.getSequenceReference().getNumber();
            Optional.ofNullable(edifactToFhirIdMap.get(edifactId))
                .ifPresent(fhirId -> linkWithGroup(fhirId, observation));
            return observation;
        }

        private Observation buildBareObservation() {
            final var observation = new Observation();
            observation.getMeta().addProfile(FhirProfiles.OBSERVATION);
            observation.setId(uuidGenerator.generateUUID());
            return observation;
        }

        private void linkWithGroup(String fhirId, Observation result) {
            Optional.ofNullable(testGroupsById.get(fhirId))
                .map(Observation::addRelated)
                .ifPresent(resultComponent -> linkWithResult(resultComponent, result));
        }

        private void linkWithResult(ObservationRelatedComponent resultComponent, Observation result) {
            resultComponent.getTarget().setReference(fullUrlGenerator.generate(result.getId()));
            resultComponent.setType(ObservationRelationshipType.HASMEMBER);
        }

        private void mapContents(final LabResult labResult, final Observation observation) {
            mapStatus(labResult, observation);
            mapLaboratoryInvestigationResult(labResult, observation);
            mapCode(labResult, observation);
            mapReferenceRange(labResult, observation);
            mapInterpretation(labResult, observation);
            mapComment(labResult, observation);
            mapPatient(observation);
            mapPerformer(observation);
            mapSpecimen(labResult, observation);
        }

        private void mapSpecimen(final LabResult labResult, final Observation observation) {
            Optional.ofNullable(specimenIds.get(labResult.getSequenceReference().getNumber()))
                .map(Specimen::getId)
                .map(fullUrlGenerator::generate)
                .map(Reference::new)
                .ifPresent(observation::setSpecimen);
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

        private void mapInterpretation(final LabResult labResult, final Observation observation) {
            // Observation.interpretation = SG18.RSL.7857
            var interpretation = observation.getInterpretation();
            labResult.getInvestigationResult()
                .map(LaboratoryInvestigationResult::getDeviatingResultIndicator)
                .map(DeviatingResultIndicator::toCoding)
                .ifPresent(interpretation::addCoding);
        }

        private void mapLaboratoryInvestigationResult(final LabResult labResult, final Observation observation) {
            // Observation.value.valueQuantity.*
            labResult.getInvestigationResult().ifPresent(investigationResult -> {
                if (investigationResult.getResultType() == LaboratoryInvestigationResultType.NUMERICAL_VALUE) {
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
                } else if (investigationResult.getResultType() == LaboratoryInvestigationResultType.CODED_VALUE) {
                    final Coding coding = new Coding()
                        .setCode(investigationResult.getCode())
                        .setDisplay(investigationResult.getDescription());
                    investigationResult.getCodingType()
                        .map(this::getSystemValue)
                        .ifPresent(coding::setSystem);

                    CodeableConcept result = new CodeableConcept().addCoding(coding);

                    observation.setValue(result);
                }
            });
        }

        private void mapCode(final LabResult labResult, final Observation observation) {
            // Observation.code = SG18.INV.C847.9930 and SG18.INV.C847.9931
            final var coding = observation.getCode().addCoding();
            labResult.getInvestigation().getCode().ifPresent(coding::setCode);
            labResult.getInvestigation().getDescription().ifPresent(coding::setDisplay);
            labResult.getInvestigation().getCodingType()
                .map(this::getSystemValue)
                .ifPresent(coding::setSystem);
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
                        .map(quantity -> addUnit(quantity, rangeDetail))
                        .ifPresent(range::setLow);

                    // Observation.referenceRange.high = SG20.RND.6152
                    rangeDetail.getUpperLimit()
                        .map(this::toSimpleQuantity)
                        .map(quantity -> addUnit(quantity, rangeDetail))
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
                .map(MappingUtils::unescape)
                .filter(StringUtils::isNotBlank)
                .ifPresent(observation::setComment);
        }

        private void mapPatient(final Observation observation) {
            observation.getSubject().setReference(fullUrlGenerator.generate(patient));
        }

        private void mapPerformer(final Observation observation) {
            Stream.of(organization, practitioner)
                .map(fullUrlGenerator::generate)
                .forEach(performerUrl -> observation.addPerformer().setReference(performerUrl));
        }

        private SimpleQuantity toSimpleQuantity(final BigDecimal value) {
            final var simpleQuantity = new SimpleQuantity();
            simpleQuantity.setValue(value);
            return simpleQuantity;
        }

        private SimpleQuantity addUnit(final SimpleQuantity quantity, final RangeDetail rangeDetail) {
            rangeDetail.getUnits().ifPresent(quantity::setUnit);
            return quantity;
        }

        private String getSystemValue(final CodingType codingType) {
            return CODING_TYPE_SYSTEMS.get(codingType);
        }
    }
}
