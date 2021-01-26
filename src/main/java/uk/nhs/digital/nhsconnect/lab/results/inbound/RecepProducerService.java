package uk.nhs.digital.nhsconnect.lab.results.inbound;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Interchange;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.InterchangeTrailer;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.MessageTrailer;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.RecepBeginningOfMessage;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.RecepHeader;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.RecepMessageDateTime;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.RecepMessageHeader;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.RecepNationalHealthBody;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ReferenceInterchangeRecep;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ReferenceMessageRecep;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Segment;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.sequence.SequenceService;
import uk.nhs.digital.nhsconnect.lab.results.utils.TimestampService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RecepProducerService {

    private final SequenceService sequenceService;
    private final TimestampService timestampService;

    public String produceRecep(final Interchange receivedInterchangeFromHa) throws EdifactValidationException {
        final var segments = mapEdifactToRecep(receivedInterchangeFromHa);
        return toEdifact(segments);
    }

    private String toEdifact(final List<Segment> segments) {
        return segments.stream()
            .map(Segment::toEdifact)
            .collect(Collectors.joining("\n"));
    }

    private List<Segment> mapEdifactToRecep(final Interchange receivedInterchange) throws EdifactValidationException {
        final List<Segment> segments = new ArrayList<>();

        final var interchangeHeader = mapToInterchangeHeader(receivedInterchange);
        final var messageHeader = new RecepMessageHeader();
        segments.add(interchangeHeader);
        segments.add(messageHeader);
        final var beginningOfMessage = new RecepBeginningOfMessage();
        segments.add(beginningOfMessage);
        segments.add(mapToNameAndAddress(receivedInterchange));
        final var translationDateTime = emptyRecepTimestamp();
        segments.add(translationDateTime);
        segments.addAll(mapToReferenceMessageReceps(receivedInterchange));
        segments.add(mapToReferenceInterchange(receivedInterchange));
        final var messageTrailer = new MessageTrailer(0);
        final var interchangeTrailer = mapToInterchangeTrailer(receivedInterchange);
        segments.add(messageTrailer);
        segments.add(interchangeTrailer);

        segments.forEach(Segment::preValidate);

        setSequenceNumbers(segments, interchangeHeader, messageHeader, messageTrailer, interchangeTrailer);
        setTimestamps(interchangeHeader, beginningOfMessage, translationDateTime);

        segments.forEach(Segment::validate);

        return segments;
    }

    private List<ReferenceMessageRecep> mapToReferenceMessageReceps(final Interchange receivedInterchange) {
        return receivedInterchange.getMessages().stream()
            .map(this::mapToReferenceMessage)
            .collect(Collectors.toList());
    }

    private void setTimestamps(
        final RecepHeader recepInterchangeHeader,
        final RecepBeginningOfMessage recepBeginningOfMessage,
        final RecepMessageDateTime recepTranslationDateTime
    ) {
        final var currentTimestamp = timestampService.getCurrentTimestamp();
        recepInterchangeHeader.setTranslationTime(currentTimestamp);
        recepBeginningOfMessage.setTimestamp(currentTimestamp);
        recepTranslationDateTime.setTimestamp(currentTimestamp);
    }

    private void setSequenceNumbers(
        final List<Segment> recepMessageSegments,
        final RecepHeader recepInterchangeHeader,
        final RecepMessageHeader recepMessageHeader,
        final MessageTrailer recepMessageTrailer,
        final InterchangeTrailer recepInterchangeTrailer
    ) {
        final var recepInterchangeSequence = sequenceService.generateInterchangeSequence(
            recepInterchangeHeader.getSender(),
            recepInterchangeHeader.getRecipient());
        final var recepMessageSequence = sequenceService.generateMessageSequence(
            recepInterchangeHeader.getSender(),
            recepInterchangeHeader.getRecipient());

        recepInterchangeHeader.setSequenceNumber(recepInterchangeSequence);
        recepInterchangeTrailer
            .setSequenceNumber(recepInterchangeSequence)
            .setNumberOfMessages(1); // we always build recep with single message inside

        recepMessageHeader.setSequenceNumber(recepMessageSequence);
        recepMessageTrailer
            .setSequenceNumber(recepMessageSequence)
            .setNumberOfSegments(recepMessageSegments.size() - 2); // excluding UNB and UNZ
    }

    private InterchangeTrailer mapToInterchangeTrailer(final Interchange receivedInterchange) {
        return new InterchangeTrailer(receivedInterchange.getInterchangeTrailer().getNumberOfMessages());
    }

    private RecepMessageDateTime emptyRecepTimestamp() {
        return new RecepMessageDateTime();
    }

    private RecepHeader mapToInterchangeHeader(final Interchange interchange) {
        final var recepSender = interchange.getInterchangeHeader().getRecipient();
        final var recepRecipient = interchange.getInterchangeHeader().getSender();

        return new RecepHeader(recepSender, recepRecipient, interchange.getInterchangeHeader().getTranslationTime());
    }

    private RecepNationalHealthBody mapToNameAndAddress(final Interchange interchange) {
        final var message = interchange.getMessages().get(0);
        final var cypher = message.getHealthAuthorityNameAndAddress().getIdentifier();
        final var gpCode = message.findFirstGpCode();
        return new RecepNationalHealthBody(cypher, gpCode);
    }

    private ReferenceMessageRecep mapToReferenceMessage(final Message message) {
        final var messageHeader = message.getMessageHeader();
        return new ReferenceMessageRecep(messageHeader.getSequenceNumber(), ReferenceMessageRecep.RecepCode.SUCCESS);
    }

    private ReferenceInterchangeRecep mapToReferenceInterchange(final Interchange interchange) {
        return new ReferenceInterchangeRecep(
            interchange.getInterchangeHeader().getSequenceNumber(),
            ReferenceInterchangeRecep.RecepCode.RECEIVED,
            interchange.getInterchangeTrailer().getNumberOfMessages());
    }

}
