package uk.nhs.digital.nhsconnect.lab.results.utils;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UUIDGenerator {

    public String generateUUID() {
        return UUID.randomUUID().toString();
    }
}
