package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestStatusTest {

    private final TestStatus testStatus = new TestStatus(
            TestStatusCode.CORRECTED
    );

    @Test
    void when_edifactStringDoesNotStartWithCorrectKey_expect_illegalArgumentExceptionIsThrown() {
        assertThrows(IllegalArgumentException.class, () -> TestStatus.fromString("wrong value"));
    }

    @Test
    void when_edifactStringIsPassed_expect_returnATestStatusObject() {
        var valueMap = Map.of(
            TestStatusCode.CORRECTED, "CO",
            TestStatusCode.UNKNOWN, "UN",
            TestStatusCode.AMENDED, "AM",
            TestStatusCode.CANCELLED, "CA",
            TestStatusCode.ENTERED_IN_ERROR, "EN",
            TestStatusCode.FINAL, "FI",
            TestStatusCode.PRELIMINARY, "PR",
            TestStatusCode.REGISTERED, "RE");

        assertThat(valueMap).hasSameSizeAs(TestStatusCode.values());

        for (TestStatusCode testStatusCode : TestStatusCode.values()) {
            assertThat(new TestStatus(testStatusCode)).usingRecursiveComparison()
                .isEqualTo(TestStatus.fromString("STS++" + valueMap.get(testStatusCode)));
        }

        assertAll(
            () -> assertThatThrownBy(() -> TestStatus.fromString("STS++XX"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No test status code for 'XX'"),
            () -> assertThatThrownBy(() -> TestStatus.fromString("STS++"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No test status code for ''")
        );
    }

    @Test
    void when_buildingSegmentObjectWithoutMandatoryField_expect_nullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> TestStatus.builder().build());
    }

    @Test
    void testGetKey() {
        assertEquals(testStatus.getKey(), "STS");
    }

    @Test
    void testGetValue() {
        assertEquals(testStatus.getTestStatusCode(), TestStatusCode.CORRECTED);
    }
}
