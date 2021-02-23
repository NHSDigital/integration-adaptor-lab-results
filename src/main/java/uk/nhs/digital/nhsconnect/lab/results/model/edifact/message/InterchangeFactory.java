package uk.nhs.digital.nhsconnect.lab.results.model.edifact.message;

import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Interchange;

import java.util.List;

@Component
public class InterchangeFactory {
    public Interchange createInterchange(List<String> edifactSegments) throws InterchangeCriticalException {
        return new Interchange(edifactSegments);
    }
}
