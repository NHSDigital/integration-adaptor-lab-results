package uk.nhs.digital.nhsconnect.lab.results.inbound.fhir.mapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.TransactionType;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class TransactionMapperConfig {
    @Bean
    public Map<TransactionType, FhirTransactionMapper> getTransactionMappers(Set<FhirTransactionMapper> fhirTransactionMappers) {
        return fhirTransactionMappers.stream()
            .collect(Collectors.toMap(FhirTransactionMapper::getTransactionType, Function.identity()));
    }
}
