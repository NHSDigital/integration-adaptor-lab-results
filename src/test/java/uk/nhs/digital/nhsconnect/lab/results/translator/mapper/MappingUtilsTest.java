package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MappingUtilsTest {
    @Test
    void testUnescape() {
        assertThat(MappingUtils.unescape("Unescape?+greengrocer?'s??"))
                .isEqualTo("Unescape+greengrocer's?");
    }

    @Test
    void testUnescapeNull() {
        assertThatThrownBy(() -> MappingUtils.unescape(null))
                .isExactlyInstanceOf(NullPointerException.class);
    }
}
