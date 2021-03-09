package uk.nhs.digital.nhsconnect.lab.results.uat.common;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FailureArgumentsProvider extends AbstractArgumentsProvider {

    @Override
    public String getFolder() {
        return "failure_uat_data";
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws IOException {
        var resources = getResources();

        var grouped = groupedResources(resources).entrySet()
            .stream()
            .peek(entry -> {
                if (entry.getValue().size() != 1) {
                    throw new IllegalStateException(String.format(
                        "There should be 1 test data file: '<any>%s': %s",
                        EDIFACT_FILE_ENDING, entry.getKey()));
                }
            })
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> TestData.builder()
                    .edifact(readEdifactResource(entry.getValue()))
                    .build()));

        return grouped.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> Arguments.of(entry.getKey(), entry.getValue()));
    }

}