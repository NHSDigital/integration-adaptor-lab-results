package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

@Getter
@Setter
@RequiredArgsConstructor
public class RecepNationalHealthBody extends Segment {
    private static final String KEY = "NHS";

    @NonNull
    private String cipher;

    @NonNull
    private String gpCode;

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        return cipher + ":819:201+" + gpCode + ":814:202";
    }

    @Override
    protected void validateStateful() {
        // Do nothing
    }

    @Override
    public void preValidate() throws EdifactValidationException {
        // Do nothing
    }
}
