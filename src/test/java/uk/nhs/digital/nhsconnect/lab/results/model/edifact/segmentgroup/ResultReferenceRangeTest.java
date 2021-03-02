package uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.FreeTextSegment;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.RangeDetail;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MissingSegmentException;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class ResultReferenceRangeTest {

    public static final int EXPECTED_RANGE_LOWER_LIMIT = 170;
    public static final int EXPECTED_RANGE_UPPER_LIMIT = 1100;

    @Test
    void testIndicator() {
        assertThat(ResultReferenceRange.INDICATOR).isEqualTo("S20");
    }

    @Test
    void testGetRangeDetail() {
        final var range = new ResultReferenceRange(List.of(
                "ignore me",
                "RND+U+170+1100",
                "ignore me"
        ));
        var rangeDetails = range.getDetails();
        assertThat(rangeDetails)
                .isNotNull()
                .extracting(RangeDetail::getLowerLimit)
                .isEqualTo(BigDecimal.valueOf(EXPECTED_RANGE_LOWER_LIMIT));
        assertThat(rangeDetails)
                .isNotNull()
                .extracting(RangeDetail::getUpperLimit)
                .isEqualTo(BigDecimal.valueOf(EXPECTED_RANGE_UPPER_LIMIT));
    }

    @Test
    void testGetReferencePopulationDefinitionFreeText() {
        final var range = new ResultReferenceRange(List.of(
                "ignore me",
                "FTX+RPD+++Equivocal",
                "ignore me"
        ));
        assertThat(range.getFreeTexts())
                .isPresent()
                .map(FreeTextSegment::getTexts)
                .map(texts -> texts[0])
                .contains("Equivocal");
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void testLazyGettersWhenMissing() {
        final var range = new ResultReferenceRange(List.of());
        assertAll(
                () -> assertThatThrownBy(range::getDetails)
                        .isExactlyInstanceOf(MissingSegmentException.class)
                        .hasMessage("EDIFACT section is missing segment RND+U"),
                () -> assertThat(range.getFreeTexts()).isEmpty()
        );
    }
}
