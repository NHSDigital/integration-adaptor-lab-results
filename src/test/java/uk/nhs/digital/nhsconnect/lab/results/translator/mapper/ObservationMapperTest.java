package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Observation.ObservationReferenceRangeComponent;
import org.hl7.fhir.dstu3.model.Observation.ObservationStatus;
import org.hl7.fhir.dstu3.model.Quantity.QuantityComparator;
import org.hl7.fhir.dstu3.model.SimpleQuantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MissingSegmentException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SuppressWarnings("checkstyle:MagicNumber")
class ObservationMapperTest {
    private ObservationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObservationMapper();
    }

    @Test
    void testMapToObservationsNonePresent() {
        final Message message = new Message(Collections.emptyList());

        final var observations = mapper.mapToTestGroupsAndResults(message);

        assertThat(observations).isEmpty();
    }

    @Test
    void testMapToObservationsMissingRequiredSegments() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "GIS+N" // LabResult
        ));

        assertThatThrownBy(() -> mapper.mapToTestGroupsAndResults(message))
            .isExactlyInstanceOf(MissingSegmentException.class)
            .hasMessageStartingWith("EDIFACT section is missing segment");
    }

    @Test
    void testMapToObservationsOnlyRequiredSegmentLaboratoryInvestigation() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "GIS+N", // LabResult
            "INV+MQ+code:911::description", // LaboratoryInvestigation
            "GIS+N", // LabResult
            "INV+MQ+code:911::description" // LaboratoryInvestigation
        ));

        final var observations = mapper.mapToTestGroupsAndResults(message);

        assertThat(observations).hasSize(2)
            .allSatisfy(observation -> assertThat(observation.getCode()).satisfies(codeConcept -> assertAll(
                () -> assertThat(codeConcept.getText()).isEqualTo("description"),
                () -> assertThat(codeConcept.getCoding()).hasSize(1).first()
                    .extracting(Coding::getCode)
                    .isEqualTo("code")
            )));
    }

    @Test
    void testMapToTestGroupsAndResultsLaboratoryInvestigationResult() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "GIS+N", // LabResult
            "INV+MQ+c:911::d", // LaboratoryInvestigation

            "RSL+NV+1.23:7++:::units" // LaboratoryInvestigationResult
        ));

        final var observations = mapper.mapToTestGroupsAndResults(message);

        assertThat(observations).hasSize(1)
            .first()
            .extracting(Observation::getValueQuantity)
            .isNotNull()
            .satisfies(quantity -> assertAll(
                () -> assertThat(quantity.getValue()).isEqualTo("1.23"),
                () -> assertThat(quantity.getComparator()).isEqualTo(QuantityComparator.LESS_THAN),
                () -> assertThat(quantity.getUnit()).isEqualTo("units")
            ));
    }

    @Test
    void testMapToTestGroupsAndResultsTestStatusCode() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "GIS+N", // LabResult
            "INV+MQ+c:911::d", // LaboratoryInvestigation

            "STS++PR" // TestStatus
        ));

        final var observations = mapper.mapToTestGroupsAndResults(message);

        assertThat(observations).hasSize(1)
            .first()
            .extracting(Observation::getStatus)
            .isEqualTo(ObservationStatus.PRELIMINARY);
    }

    @Test
    void testMapToTestGroupsAndResultsFreeTexts() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "GIS+N", // LabResult
            "INV+MQ+c:911::d", // LaboratoryInvestigation

            "FTX+RIT+++multi:line", // FreeTextSegment
            "FTX+RIT+++comment"
        ));

        final var observations = mapper.mapToTestGroupsAndResults(message);

        assertThat(observations).hasSize(1)
            .first()
            .extracting(Observation::getComment)
            .isEqualTo("multi line\ncomment");
    }

    @Test
    void testMapToTestGroupsAndResultsRange() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "GIS+N", // LabResult
            "INV+MQ+c:911::d", // LaboratoryInvestigation

            "S20+20", // ResultReferenceRange
            "RND+U+-1.0+1.0" // RangeDetail
        ));

        final var observations = mapper.mapToTestGroupsAndResults(message);

        final var assertReferenceRange = assertThat(observations).hasSize(1)
            .first()
            .extracting(Observation::getReferenceRangeFirstRep);
        assertAll(
            () -> assertReferenceRange.extracting(ObservationReferenceRangeComponent::getLow)
                .extracting(SimpleQuantity::getValue)
                .extracting(BigDecimal::intValue)
                .isEqualTo(-1),
            () -> assertReferenceRange.extracting(ObservationReferenceRangeComponent::getHigh)
                .extracting(SimpleQuantity::getValue)
                .extracting(BigDecimal::intValue)
                .isEqualTo(1)
        );
    }

    @Test
    void testCanMapMultipleTestResults() {
        final Message message = new Message(List.of(
            "S02+02",
            "S06+06",
            "GIS+N",
            "INV+MQ+c:911::d",
            "GIS+N",
            "INV+MQ+c:911::d",
            "GIS+N",
            "INV+MQ+c:911::d"
        ));

        final List<Observation> actual = mapper.mapToTestGroupsAndResults(message);

        assertThat(actual).hasSize(3);
    }

    @Test
    @Disabled
    void testCanMapTestGroupWithOneResult() {
        final Message message = new Message(List.of(
            "S02+02",
            "S06+06",
            "GIS+N",
            "INV+MQ+44O..:911::Test group",
            "SEQ++1",
            "FTX+SPC+++This is a test group",
            "RFF+ASL:1",
            "GIS+N",
            "INV+MQ+:::Test result",
            "FTX+RIT+++This is a test result:belonging to the test group",
            "RFF+ARL:1"
        ));

        final List<Observation> actual = mapper.mapToTestGroupsAndResults(message);

        final var assertObservations = assertThat(actual).hasSize(2);
        assertAll(
            () -> assertObservations.first()
        );
    }
}
