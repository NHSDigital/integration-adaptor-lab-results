package uk.nhs.digital.nhsconnect.lab.results.utils;

import org.hl7.fhir.dstu3.model.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResourceFullUrlGeneratorTest {
    private ResourceFullUrlGenerator fullUrlGenerator;

    @BeforeEach
    void setUp() {
        fullUrlGenerator = new ResourceFullUrlGenerator();
    }

    @Test
    void testMapToFullUrl() {
        final var resource = mock(Resource.class);
        when(resource.getId()).thenReturn("resource-id");

        String result = fullUrlGenerator.generate(resource);

        assertThat(result).isEqualTo("urn:uuid:resource-id");
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void testMapToFullUrlNullResource() {
        assertThatThrownBy(() -> fullUrlGenerator.generate(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("resource is marked non-null but is null");
    }

    @Test
    void testMapToFullUrlNullId() {
        final var resource = mock(Resource.class);
        when(resource.getId()).thenReturn(null);

        assertThatThrownBy(() -> fullUrlGenerator.generate(resource))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("resource.id must be present and non-blank");
    }

    @Test
    void testMapToFullUrlEmptyId() {
        final var resource = mock(Resource.class);
        when(resource.getId()).thenReturn("");

        assertThatThrownBy(() -> fullUrlGenerator.generate(resource))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("resource.id must be present and non-blank");
    }

    @Test
    void testMapToFullUrlBlankId() {
        final var resource = mock(Resource.class);
        when(resource.getId()).thenReturn("  \t \n ");

        assertThatThrownBy(() -> fullUrlGenerator.generate(resource))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("resource.id must be present and non-blank");
    }
}
