package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import uk.nhs.digital.nhsconnect.lab.results.utils.TimestampService;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
public class RecepBeginningOfMessage extends Segment {
    private static final String DATE_TIME_FORMAT = "yyyyMMddHHmm";
    private static final String BGM_PREFIX = "+600+243:";
    private static final String BGM_SUFFIX = ":306+64";

    @NonNull
    private Instant timestamp;

    @Override
    public String getKey() {
        return "BGM";
    }

    @Override
    public String getValue() {
        return BGM_PREFIX
            .concat(getDateTimeFormat().format(timestamp))
            .concat(BGM_SUFFIX);
    }

    @Override
    protected void validateStateful() {
        // Do nothing
    }

    @Override
    public void preValidate() {
        // Do nothing
    }

    private DateTimeFormatter getDateTimeFormat() {
        return DateTimeFormatter.ofPattern(DATE_TIME_FORMAT).withZone(TimestampService.UK_ZONE);
    }
}
