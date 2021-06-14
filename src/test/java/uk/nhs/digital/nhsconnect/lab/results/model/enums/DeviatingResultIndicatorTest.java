package uk.nhs.digital.nhsconnect.lab.results.model.enums;

import org.hl7.fhir.dstu3.model.Coding;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DeviatingResultIndicatorTest {

    @ParameterizedTest
    @ArgumentsSource(DeviatingResultIndicatorToCodingArgumentsProvider.class)
    void when_convertingToCoding_expect_properObjectIsCreated(
            DeviatingResultIndicator deviatingResultIndicator, Coding expectedCoding) {

        assertThat(deviatingResultIndicator.toCoding())
            .usingRecursiveComparison()
            .isEqualTo(expectedCoding);
    }

    public static class DeviatingResultIndicatorToCodingArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of(DeviatingResultIndicator.ABOVE_HIGH_REFERENCE_LIMIT, new Coding()
                    .setCode("H")
                    .setSystem("http://hl7.org/fhir/v2/0078")
                    .setDisplay("High")
                ),
                Arguments.of(DeviatingResultIndicator.BELOW_LOW_REFERENCE_LIMIT, new Coding()
                    .setCode("L")
                    .setSystem("http://hl7.org/fhir/v2/0078")
                    .setDisplay("Low")
                ),
                Arguments.of(DeviatingResultIndicator.OUTSIDE_REFERENCE_LIMIT, new Coding()
                    .setCode("OR")
                    .setDisplay("Outside reference range")
                ),
                Arguments.of(DeviatingResultIndicator.POTENTIALLY_ABNORMAL, new Coding()
                    .setCode("PA")
                    .setDisplay("Potentially abnormal")
                )
            );
        }
    }
}
