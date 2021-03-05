package uk.nhs.digital.nhsconnect.lab.results.uat.common;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SuccessArgumentsProvider extends AbstractArgumentsProvider {

    @Override
    public String getFolder() {
        return "success_uat_data";
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws IOException {
        var resources = getResources();

        var grouped = groupedResources(resources).entrySet()
            .stream()
            .peek(entry -> {
                if (entry.getValue().size() != 2) {
                    throw new IllegalStateException(String.format(
                        "There should be 2 test data files: '<any>%s' and '<any>%s': %s",
                        EDIFACT_FILE_ENDING, FHIR_FILE_ENDING, entry.getKey()));
                }
            })
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> TestData.builder()
                    .edifact(readResource(entry.getValue(), EDIFACT_FILE_ENDING))
                    .json(readResource(entry.getValue(), FHIR_FILE_ENDING))
                    .build()));

        return grouped.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(es -> Arguments.of(es.getKey(), es.getValue()));
    }

}