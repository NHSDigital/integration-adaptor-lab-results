package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import lombok.NonNull;

final class MappingUtils {
    static String unescape(@NonNull final String rawEdifact) {
        return rawEdifact.replaceAll("\\?(.)", "$1");
    }

    private MappingUtils() {
        // utility class
    }
}
