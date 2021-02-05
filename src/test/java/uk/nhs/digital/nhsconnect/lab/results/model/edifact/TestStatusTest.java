package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
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
        assertThat(testStatus)
                .usingRecursiveComparison()
                .isEqualTo(TestStatus.fromString("STS++CO"));
    }

    @Test
    void when_mappingSegmentObjectToEdifactString_expect_returnCorrectEdifactString() {
        String expectedEdifactString = "STS+CO'";

        TestStatus testStatus = TestStatus.builder()
                .testStatusCode(TestStatusCode.CORRECTED)
                .build();

        assertEquals(expectedEdifactString, testStatus.toEdifact());
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
        assertEquals(testStatus.getValue(), "CO");
    }
}
