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

    public String produceRecep(Interchange receivedInterchangeFromHa) throws EdifactValidationException {
        var segments = mapEdifactToRecep(receivedInterchangeFromHa);
        return toEdifact(segments);
    }

    private String toEdifact(List<Segment> segments) {
        return segments.stream()
            .map(Segment::toEdifact)
            .collect(Collectors.joining("\n"));
    }

    private List<Segment> mapEdifactToRecep(Interchange receivedInterchangeFromHa) throws EdifactValidationException {
        List<Segment> segments = new ArrayList<>();

        var interchangeHeader = mapToInterchangeHeader(receivedInterchangeFromHa);
        var messageHeader = new RecepMessageHeader();
        segments.add(interchangeHeader);
        segments.add(messageHeader);
        var beginningOfMessage = new RecepBeginningOfMessage();
        segments.add(beginningOfMessage);
        segments.add(mapToNameAndAddress(receivedInterchangeFromHa));
        var translationDateTime = emptyRecepTimestamp();
        segments.add(translationDateTime);
        segments.addAll(mapToReferenceMessageReceps(receivedInterchangeFromHa));
        segments.add(mapToReferenceInterchange(receivedInterchangeFromHa));
        var messageTrailer = new MessageTrailer(0);
        var interchangeTrailer = mapToInterchangeTrailer(receivedInterchangeFromHa);
        segments.add(messageTrailer);
        segments.add(interchangeTrailer);

        segments.forEach(Segment::preValidate);

        setSequenceNumbers(segments, interchangeHeader, messageHeader, messageTrailer, interchangeTrailer);
        setTimestamps(interchangeHeader, beginningOfMessage, translationDateTime);

        segments.forEach(Segment::validate);

        return segments;
    }

    private List<ReferenceMessageRecep> mapToReferenceMessageReceps(Interchange receivedInterchangeFromHa) {
        return receivedInterchangeFromHa.getMessages().stream()
            .map(this::mapToReferenceMessage)
            .collect(Collectors.toList());
    }

    private void setTimestamps(
            RecepHeader recepInterchangeHeader,
            RecepBeginningOfMessage recepBeginningOfMessage,
            RecepMessageDateTime recepTranslationDateTime
    ) {
        var currentTimestamp = timestampService.getCurrentTimestamp();
        recepInterchangeHeader.setTranslationTime(currentTimestamp);
        recepBeginningOfMessage.setTimestamp(currentTimestamp);
        recepTranslationDateTime.setTimestamp(currentTimestamp);
    }

    private void setSequenceNumbers(
            List<Segment> recepMessageSegments,
            RecepHeader recepInterchangeHeader,
            RecepMessageHeader recepMessageHeader,
            MessageTrailer recepMessageTrailer,
            InterchangeTrailer recepInterchangeTrailer
    ) {
        var recepInterchangeSequence = sequenceService.generateInterchangeSequence(
            recepInterchangeHeader.getSender(),
            recepInterchangeHeader.getRecipient());
        var recepMessageSequence = sequenceService.generateMessageSequence(
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

    private InterchangeTrailer mapToInterchangeTrailer(Interchange receivedInterchangeFromHa) {
        return new InterchangeTrailer(receivedInterchangeFromHa.getInterchangeTrailer().getNumberOfMessages());
    }

    private RecepMessageDateTime emptyRecepTimestamp() {
        return new RecepMessageDateTime();
    }

    private RecepHeader mapToInterchangeHeader(Interchange interchange) {
        var recepSender = interchange.getInterchangeHeader().getRecipient();
        var recepRecipient = interchange.getInterchangeHeader().getSender();

        return new RecepHeader(recepSender, recepRecipient, interchange.getInterchangeHeader().getTranslationTime());
    }

    private RecepNationalHealthBody mapToNameAndAddress(Interchange interchange) {
        var message = interchange.getMessages().get(0);
        var cypher = message.getHealthAuthorityNameAndAddress().getIdentifier();
        var gpCode = message.findFirstGpCode();
        return new RecepNationalHealthBody(cypher, gpCode);
    }

    private ReferenceMessageRecep mapToReferenceMessage(Message message) {
        var messageHeader = message.getMessageHeader();
        return new ReferenceMessageRecep(messageHeader.getSequenceNumber(), ReferenceMessageRecep.RecepCode.SUCCESS);
    }

    private ReferenceInterchangeRecep mapToReferenceInterchange(Interchange interchange) {
        return new ReferenceInterchangeRecep(
            interchange.getInterchangeHeader().getSequenceNumber(),
            ReferenceInterchangeRecep.RecepCode.RECEIVED,
            interchange.getInterchangeTrailer().getNumberOfMessages());
    }

}