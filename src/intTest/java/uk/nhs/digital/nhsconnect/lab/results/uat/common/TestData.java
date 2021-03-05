package uk.nhs.digital.nhsconnect.lab.results.uat.common;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TestData {
    private final String edifact;
    private final String json;
}
