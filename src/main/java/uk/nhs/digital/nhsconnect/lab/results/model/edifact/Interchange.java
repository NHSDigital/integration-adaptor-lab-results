package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import lombok.Getter;
import lombok.Setter;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.InterchangeCriticalException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.InterchangeParsingException;

import java.util.List;

public class Interchange extends Section {

    @Getter(lazy = true)
    private final InterchangeHeader interchangeHeader =
        InterchangeHeader.fromString(extractSegment(InterchangeHeader.KEY));

    @Getter(lazy = true)
    private final InterchangeTrailer interchangeTrailer =
        InterchangeTrailer.fromString(extractSegment(InterchangeTrailer.KEY));

    @Getter
    @Setter
    private List<Message> messages;

    public Interchange(final List<String> edifactSegments) {
        super(edifactSegments);
    }

    @Override
    public String toString() {
        return String.format("Interchange{SIS: %s}", getInterchangeHeader().getSequenceNumber());
    }

    public void validate() throws InterchangeCriticalException, InterchangeParsingException {
        try {
            getInterchangeHeader().validate();
        } catch (Exception ex) {
            throw new InterchangeCriticalException("Critical error while parsing interchange", ex);
        }

        try {
            getInterchangeTrailer().validate();
            if (!getInterchangeHeader().getSequenceNumber().equals(getInterchangeTrailer().getSequenceNumber())) {
                throw new EdifactValidationException(
                    "Interchange header sequence number does not match trailer sequence number");
            }
        } catch (Exception ex) {
            throw new InterchangeParsingException(
                "Error while parsing interchange",
                getInterchangeHeader().getSender(),
                getInterchangeHeader().getRecipient(),
                getInterchangeHeader().getSequenceNumber(),
                getInterchangeHeader().isNhsAckRequested(),
                ex);
        }
    }
}