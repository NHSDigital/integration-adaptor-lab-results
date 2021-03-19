package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class RangeDetailTest {
    @Test
    void testGetKey() {
        assertThat(new RangeDetail(null, null, null).getKey()).isEqualTo("RND");
    }

    @Test
    void testIncorrectHeader() {
        assertThatThrownBy(() -> RangeDetail.fromString("WRONG+U+0+10"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't create RangeDetail from WRONG+U+0+10");
    }

    @Test
    void testAllFieldsGiven() {
        var result = RangeDetail.fromString("RND+U+1.1+2.2+unit");
        assertAll(
            () -> assertThat(result.getLowerLimit()).contains(new BigDecimal("1.1")),
            () -> assertThat(result.getUpperLimit()).contains(new BigDecimal("2.2")),
            () -> assertThat(result.getUnits()).contains("unit")
        );
    }

    @Test
    void testMissingAllFields() {
        var result = RangeDetail.fromString("RND+U++");
        assertAll(
            () -> assertThat(result.getLowerLimit()).isEmpty(),
            () -> assertThat(result.getUpperLimit()).isEmpty(),
            () -> assertThatThrownBy(result::validate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("RND: At least one of lower reference limit and upper reference limit is required")
        );
    }

    @Test
    void testOnlyLowerLimit() {
        var result = RangeDetail.fromString("RND+U+0+");
        assertAll(
            () -> assertThat(result.getLowerLimit()).contains(BigDecimal.ZERO),
            () -> assertThat(result.getUpperLimit()).isEmpty(),
            () -> assertThatNoException().isThrownBy(result::validate)
        );
    }

    @Test
    void testOnlyUpperLimit() {
        var result = RangeDetail.fromString("RND+U++1");
        assertAll(
            () -> assertThat(result.getLowerLimit()).isEmpty(),
            () -> assertThat(result.getUpperLimit()).contains(BigDecimal.ONE),
            () -> assertThatNoException().isThrownBy(result::validate)
        );
    }

    @Test
    void testBothLimits() {
        var result = RangeDetail.fromString("RND+U+-1+1");
        assertAll(
            () -> assertThat(result.getLowerLimit()).contains(BigDecimal.ONE.negate()),
            () -> assertThat(result.getUpperLimit()).contains(BigDecimal.ONE),
            () -> assertThatNoException().isThrownBy(result::validate)
        );
    }

    @Test
    @SuppressWarnings("BigDecimalMethodWithoutRoundingCalled")
    void testRetainsPrecision() {
        var result = RangeDetail.fromString("RND+U+-1.0+1.00");
        assertAll(
            () -> assertThat(result.getLowerLimit()).contains(BigDecimal.ONE.setScale(1).negate()),
            () -> assertThat(result.getUpperLimit()).contains(BigDecimal.ONE.setScale(2)),
            () -> assertThatNoException().isThrownBy(result::validate)
        );
    }
}
