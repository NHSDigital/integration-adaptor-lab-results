package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.DateFormat;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MissingSegmentException;
import uk.nhs.digital.nhsconnect.lab.results.utils.ResourceFullUrlGenerator;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpecimenMapperTest {
    @Mock
    private UUIDGenerator uuidGenerator;

    @Mock
    private DateFormatMapper dateFormatMapper;

    @Mock
    private ResourceFullUrlGenerator fullUrlGenerator;

    @InjectMocks
    private SpecimenMapper specimenMapper;

    @Test
    void testMapToSpecimensNonePresent() {
        final Message message = new Message(Collections.emptyList());

        final var specimens = specimenMapper.mapToSpecimens(message, null);

        assertThat(specimens).isEmpty();
    }

    @Test
    void testMapToSpecimensMissingRequiredSegments() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S16+16"  // SpecimenDetails
        ));

        assertThatThrownBy(() -> specimenMapper.mapToSpecimens(message, null))
            .isExactlyInstanceOf(MissingSegmentException.class)
            .hasMessageStartingWith("EDIFACT section is missing segment");
    }

    @Test
    void testMapToSpecimensOnlyRequiredSegmentCharacteristicType() {
        when(uuidGenerator.generateUUID()).thenReturn("test-uuid");
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S16+16", // SpecimenDetails
            "SPC+TSP+:::Specimen type", // SpecimenCharacteristicType
            "S16+16", // SpecimenDetails

            "SPC+TSP+:::Specimen type" // SpecimenCharacteristicType
        ));

        final var specimens = specimenMapper.mapToSpecimens(message, null);

        assertThat(specimens).hasSize(2)
            .allSatisfy(specimen -> assertThat(specimen.getType().getCoding())
                .hasSize(1)
                .map(Coding::getDisplay)
                .allMatch("Specimen type"::equals))
            .allSatisfy(specimen -> assertThat(specimen.getId()).isEqualTo("test-uuid"));
    }

    @Test
    void testMapToSpecimensServiceRequesterReference() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S16+16", // SpecimenDetails
            "SPC+TSP+:::Required", // SpecimenCharacteristicType

            "RFF+RTI:Requester" // Reference - SPECIMEN_BY_REQUESTER
        ));

        final var specimens = specimenMapper.mapToSpecimens(message, null);

        assertThat(specimens).hasSize(1)
            .first()
            .satisfies(specimen -> assertThat(specimen.getIdentifier()).hasSize(1)
                .first()
                .extracting(Identifier::getValue)
                .isEqualTo("Requester"));
    }

    @Test
    void testMapToSpecimensServiceProviderReference() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S16+16", // SpecimenDetails
            "SPC+TSP+:::Required", // SpecimenCharacteristicType

            "RFF+STI:Provider" // Reference - SPECIMEN_BY_PROVIDER
        ));

        final var specimens = specimenMapper.mapToSpecimens(message, null);

        assertThat(specimens).hasSize(1)
            .first()
            .satisfies(specimen -> assertThat(specimen.getAccessionIdentifier())
                .extracting(Identifier::getValue)
                .isEqualTo("Provider"));
    }

    @Test
    void testMapToSpecimensCollectionReceiptDateTime() {
        final var expectedDate = new Date();
        when(dateFormatMapper.mapToDate(any(DateFormat.class), anyString()))
            .thenReturn(expectedDate);

        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S16+16", // SpecimenDetails
            "SPC+TSP+:::Required", // SpecimenCharacteristicType

            "DTM+SRI:date:203" // SpecimenCollectionReceiptDateTime
        ));

        final var specimens = specimenMapper.mapToSpecimens(message, null);

        assertThat(specimens).hasSize(1)
            .first()
            .satisfies(specimen -> assertThat(specimen.getReceivedTime()).isEqualTo(expectedDate));
    }

    @Test
    void testMapToSpecimensCollectionDateTime() {
        final var expectedDate = mock(DateTimeType.class);
        when(dateFormatMapper.mapToDateTimeType(any(DateFormat.class), anyString()))
            .thenReturn(expectedDate);

        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S16+16", // SpecimenDetails
            "SPC+TSP+:::Required", // SpecimenCharacteristicType

            "DTM+SCO:date:203" // SpecimenCollectionDateTime
        ));

        final var specimens = specimenMapper.mapToSpecimens(message, null);

        assertThat(specimens).hasSize(1)
            .first()
            .satisfies(specimen -> assertThat(specimen.getCollection().getCollected()).isEqualTo(expectedDate));
    }

    @Test
    void testMapToSpecimensQuantityAndUnitOfMeasure() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S16+16", // SpecimenDetails
            "SPC+TSP+:::Required", // SpecimenCharacteristicType

            "QTY+SVO:1+:::unit" // SpecimenQuantity
        ));

        final var specimens = specimenMapper.mapToSpecimens(message, null);

        assertThat(specimens).hasSize(1)
            .first()
            .satisfies(specimen -> assertAll(
                () -> assertThat(specimen.getCollection().getQuantity().getValue()).isEqualTo(BigDecimal.ONE),
                () -> assertThat(specimen.getCollection().getQuantity().getUnit()).isEqualTo("unit")
            ));
    }

    @Test
    void testMapToSpecimensFreeTexts() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S16+16", // SpecimenDetails
            "SPC+TSP+:::Required", // SpecimenCharacteristicType

            "FTX+SPC+++item 1 part 1:item 1 part 2", // FreeTextSegment
            "FTX+SPC+++item 2 part 1:item 2 part 2"  // FreeTextSegment
        ));

        final var specimens = specimenMapper.mapToSpecimens(message, null);

        assertThat(specimens).hasSize(1)
            .first()
            .satisfies(specimen -> assertThat(specimen.getNote())
                .extracting(Annotation::getText)
                .contains("item 1 part 1", "item 1 part 2", "item 2 part 1", "item 2 part 2"));
    }

    @Test
    void testMapToSpecimensPatientReference() {
        final Message message = new Message(List.of(
            "S02+02", // ServiceReportDetails
            "S06+06", // InvestigationSubject
            "S16+16", // SpecimenDetails
            "SPC+TSP+:::Required" // SpecimenCharacteristicType
        ));
        when(fullUrlGenerator.generateFullUrl(any(Patient.class))).thenReturn("patient-full-url");

        final var specimens = specimenMapper.mapToSpecimens(message, mock(Patient.class));

        assertThat(specimens).hasSize(1)
            .first()
            .satisfies(specimen -> assertThat(specimen.getSubject())
                .extracting(Reference::getReference)
                .isEqualTo("patient-full-url"));
    }
}
