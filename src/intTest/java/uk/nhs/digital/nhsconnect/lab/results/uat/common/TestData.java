package uk.nhs.digital.nhsconnect.lab.results.uat.common;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class TestData {
    private final String edifact;
    private final List<String> jsonList;
}
