package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

public interface TransactionType {

    static TransactionType fromCode(String code) {
        return Stream.of(
                Arrays.stream(Inbound.values()),
                Arrays.stream(Outbound.values()))
                .flatMap(Function.identity())
                .map(TransactionType.class::cast)
                .filter(transactionType -> transactionType.getCode().equals(code))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    String getCode();

    default String name() {
        return ((Enum) this).name();
    }
}
