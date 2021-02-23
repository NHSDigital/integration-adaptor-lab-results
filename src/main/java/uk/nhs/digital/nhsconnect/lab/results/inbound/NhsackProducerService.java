package uk.nhs.digital.nhsconnect.lab.results.inbound;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.sequence.SequenceService;
import uk.nhs.digital.nhsconnect.lab.results.utils.TimestampService;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class NhsackProducerService {

    private final SequenceService sequenceService;
    private final TimestampService timestampService;

    public String produceNhsack() {
        //TODO NIAD-1063
        return "";
    }
}
