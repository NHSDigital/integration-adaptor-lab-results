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
        assertThat(new RangeDetail(null, null).getKey()).isEqualTo("RND");
    }

    @Test
    void testIncorrectHeader() {
        assertThatThrownBy(() -> RangeDetail.fromString("WRONG+U+0+10"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't create RangeDetail from WRONG+U+0+10");
    }

    @Test
    void testMissingBothLimits() {
        var result = RangeDetail.fromString("RND+U++");
        assertAll(
            () -> assertThat(result.getValue()).isEqualTo("U++"),
            () -> assertThat(result.getLowerLimit()).isNull(),
            () -> assertThat(result.getUpperLimit()).isNull(),
            () -> assertThatThrownBy(result::preValidate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("RND: At least one of lower reference limit and upper reference limit is required"),
            () -> assertThatNoException().isThrownBy(result::validateStateful)
        );
    }

    @Test
    void testOnlyLowerLimit() {
        var result = RangeDetail.fromString("RND+U+0+");
        assertAll(
            () -> assertThat(result.getValue()).isEqualTo("U+0+"),
            () -> assertThat(result.getLowerLimit()).isEqualTo(BigDecimal.ZERO),
            () -> assertThat(result.getUpperLimit()).isNull(),
            () -> assertThatNoException().isThrownBy(result::preValidate),
            () -> assertThatNoException().isThrownBy(result::validateStateful)
        );
    }

    @Test
    void testOnlyUpperLimit() {
        var result = RangeDetail.fromString("RND+U++1");
        assertAll(
            () -> assertThat(result.getValue()).isEqualTo("U++1"),
            () -> assertThat(result.getLowerLimit()).isNull(),
            () -> assertThat(result.getUpperLimit()).isEqualTo(BigDecimal.ONE),
            () -> assertThatNoException().isThrownBy(result::preValidate),
            () -> assertThatNoException().isThrownBy(result::validateStateful)
        );
    }

    @Test
    void testBothLimits() {
        var result = RangeDetail.fromString("RND+U+-1+1");
        assertAll(
            () -> assertThat(result.getValue()).isEqualTo("U+-1+1"),
            () -> assertThat(result.getLowerLimit()).isEqualTo(BigDecimal.ONE.negate()),
            () -> assertThat(result.getUpperLimit()).isEqualTo(BigDecimal.ONE),
            () -> assertThatNoException().isThrownBy(result::preValidate),
            () -> assertThatNoException().isThrownBy(result::validateStateful)
        );
    }

    @Test
    @SuppressWarnings("BigDecimalMethodWithoutRoundingCalled")
    void testRetainsPrecision() {
        var result = RangeDetail.fromString("RND+U+-1.0+1.00");
        assertAll(
            () -> assertThat(result.getValue()).isEqualTo("U+-1.0+1.00"),
            () -> assertThat(result.getLowerLimit()).isEqualTo(BigDecimal.ONE.setScale(1).negate()),
            () -> assertThat(result.getUpperLimit()).isEqualTo(BigDecimal.ONE.setScale(2)),
            () -> assertThatNoException().isThrownBy(result::preValidate),
            () -> assertThatNoException().isThrownBy(result::validateStateful)
        );
    }
}