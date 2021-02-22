package uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.DateFormat;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.FreeTextSegment;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Reference;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ReferenceType;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.SequenceDetails;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.SpecimenCharacteristicFastingStatus;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.SpecimenCharacteristicType;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.SpecimenCollectionDateTime;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.SpecimenCollectionReceiptDateTime;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.SpecimenQuantity;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MissingSegmentException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class SpecimenTest {
    private static final int EXPECTED_SPECIMEN_QUANTITY = 1750;

    @Test
    void testIndicator() {
        assertThat(Specimen.INDICATOR).isEqualTo("S16");
    }

    @Test
    void testGetSequenceDetails() {
        final var specimen = new Specimen(List.of(
            "ignore me",
            "SEQ++123ABC",
            "ignore me"
        ));
        assertThat(specimen.getSequenceDetails())
            .isPresent()
            .map(SequenceDetails::getNumber)
            .contains("123ABC");
    }

    @Test
    void testGetSpecimenCharacteristicType() {
        final var specimen = new Specimen(List.of(
            "ignore me",
            "SPC+TSP+:::BLOOD & URINE",
            "ignore me"
        ));
        assertThat(specimen.getCharacteristicType())
            .isNotNull()
            .extracting(SpecimenCharacteristicType::getTypeOfSpecimen)
            .isEqualTo("BLOOD & URINE");
    }

    @Test
    void testGetSpecimenCharacteristicFastingStatus() {
        final var specimen = new Specimen(List.of(
            "ignore me",
            "SPC+FS+F",
            "ignore me"
        ));
        assertThat(specimen.getCharacteristicFastingStatus())
            .isPresent()
            .map(SpecimenCharacteristicFastingStatus::getFastingStatus)
            .hasValue("F");
    }

    @Test
    void testGetSpecimenReferenceByServiceRequester() {
        final var specimen = new Specimen(List.of(
            "ignore me",
            "RFF+RTI:CH000064LX",
            "ignore me"
        ));
        var serviceRequesterReference = assertThat(specimen.getServiceRequesterReference())
            .isPresent();

        serviceRequesterReference
            .map(Reference::getNumber)
            .isEqualTo(Optional.of("CH000064LX"));
        serviceRequesterReference
            .map(Reference::getTarget)
            .map(ReferenceType::getQualifier)
            .hasValue("RTI");
    }

    @Test
    void testGetSpecimenReferenceByServiceProvider() {
        final var specimen = new Specimen(List.of(
            "ignore me",
            "RFF+STI:CH000064LX",
            "ignore me"
        ));
        var serviceProviderReference = assertThat(specimen.getServiceProviderReference())
            .isPresent();

        serviceProviderReference
            .map(Reference::getNumber)
            .hasValue("CH000064LX");
        serviceProviderReference
            .map(Reference::getTarget)
            .map(ReferenceType::getQualifier)
            .hasValue("STI");
    }

    @Test
    void testGetSpecimenQuantity() {
        final var specimen = new Specimen(List.of(
            "ignore me",
            "QTY+SVO:1750+:::mL",
            "ignore me"
        ));
        var specimenQuantity = assertThat(specimen.getQuantity()).isPresent();

        specimenQuantity
            .map(SpecimenQuantity::getQuantity)
            .hasValue(EXPECTED_SPECIMEN_QUANTITY);
        specimenQuantity
            .map(SpecimenQuantity::getQuantityUnitOfMeasure)
            .hasValue("mL");
    }

    @Test
    void testGetSpecimenCollectionDateTime() {
        final var specimen = new Specimen(List.of(
            "ignore me",
            "DTM+SCO:20100223:102",
            "ignore me"
        ));
        var specimenCollectionDateTime = assertThat(specimen.getCollectionDateTime()).isPresent();

        specimenCollectionDateTime
            .map(SpecimenCollectionDateTime::getCollectionDateTime)
            .hasValue("2010-02-23");
        specimenCollectionDateTime
            .map(SpecimenCollectionDateTime::getDateFormat)
            .hasValue(DateFormat.CCYYMMDD);
    }

    @Test
    void testGetSpecimenCollectionReceiptDateTime() {
        final var specimen = new Specimen(List.of(
            "ignore me",
            "DTM+SRI:201002241541:203",
            "ignore me"
        ));

        var specimenCollectionReceiptDateTime = assertThat(specimen.getCollectionReceiptDateTime()).isPresent();

        specimenCollectionReceiptDateTime
            .map(SpecimenCollectionReceiptDateTime::getCollectionReceiptDateTime)
            .hasValue("2010-02-24T15:41+00:00");
        specimenCollectionReceiptDateTime
            .map(SpecimenCollectionReceiptDateTime::getDateFormat)
            .hasValue(DateFormat.CCYYMMDDHHMM);
    }

    @Test
    void testGetServiceProviderCommentFreeTexts() {
        final var specimen = new Specimen(List.of(
            "ignore me",
            "FTX+SPC+++red blood cell seen",
            "ignore me",
            "FTX+SPC+++Note low platelets",
            "ignore me"
        ));

        var specimenFreeTexts = assertThat(specimen.getFreeTexts()).hasSize(2);

        specimenFreeTexts
            .map(FreeTextSegment::getTexts)
            .map(freeTexts -> freeTexts[0])
            .contains("red blood cell seen");
        specimenFreeTexts
            .map(FreeTextSegment::getTexts)
            .map(freeTexts -> freeTexts[0])
            .contains("Note low platelets");
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void testLazyGettersWhenMissing() {
        final var specimen = new Specimen(List.of());
        assertAll(
            () -> assertThat(specimen.getSequenceDetails()).isEmpty(),
            () -> assertThatThrownBy(specimen::getCharacteristicType)
                .isExactlyInstanceOf(MissingSegmentException.class)
                .hasMessage("EDIFACT section is missing segment SPC+TSP"),
            () -> assertThat(specimen.getCharacteristicFastingStatus()).isEmpty(),
            () -> assertThat(specimen.getServiceRequesterReference()).isEmpty(),
            () -> assertThat(specimen.getServiceProviderReference()).isEmpty(),
            () -> assertThat(specimen.getQuantity()).isEmpty(),
            () -> assertThat(specimen.getCollectionDateTime()).isEmpty(),
            () -> assertThat(specimen.getCollectionReceiptDateTime()).isEmpty(),
            () -> assertThat(specimen.getFreeTexts()).isEmpty()
        );
    }
}
