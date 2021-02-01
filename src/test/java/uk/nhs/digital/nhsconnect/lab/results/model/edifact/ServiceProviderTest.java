package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiceProviderTest {

    private final ServiceProvider serviceProvider = new ServiceProvider(
            ServiceProviderCode.ORGANISATION
    );

    @Test
    void when_edifactStringDoesNotStartWithCorrectKey_expect_illegalArgumentExceptionIsThrown() {
        assertThrows(IllegalArgumentException.class, () -> ServiceProvider.fromString("wrong value"));
    }

    @Test
    void when_edifactStringIsPassed_expect_returnAServiceProviderObject() {
        assertThat(serviceProvider)
                .usingRecursiveComparison()
                .isEqualTo(ServiceProvider.fromString("SPR+ORG"));
    }

    @Test
    void when_mappingSegmentObjectToEdifactString_expect_returnCorrectEdifactString() {
        String expectedEdifactString = "SPR+ORG'";

        ServiceProvider serviceProvider = ServiceProvider.builder()
                .serviceProviderCode(ServiceProviderCode.ORGANISATION)
                .build();

        assertEquals(expectedEdifactString, serviceProvider.toEdifact());
    }

    @Test
    void when_buildingSegmentObjectWithoutMandatoryField_expect_nullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> ServiceProvider.builder().build());
    }

    @Test
    void testGetKey() {
        assertEquals(serviceProvider.getKey(), "SPR");
    }

    @Test
    void testGetValue() {
        assertEquals(serviceProvider.getValue(), "ORG");
    }
}
