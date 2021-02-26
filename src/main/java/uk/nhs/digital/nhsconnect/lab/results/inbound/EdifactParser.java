package uk.nhs.digital.nhsconnect.lab.results.inbound;

import com.google.common.collect.Comparators;
import com.google.common.collect.Streams;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Interchange;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.MessageHeader;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.MessageTrailer;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.InterchangeCriticalException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.InterchangeFactory;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.InterchangeParsingException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MessageParsingException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.Split;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.ToEdifactParsingException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
public class EdifactParser {

    private static final String MESSAGE_END_SEGMENT = MessageTrailer.KEY;

    private final InterchangeFactory interchangeFactory;

    @Autowired
    public EdifactParser(InterchangeFactory interchangeFactory) {
        this.interchangeFactory = interchangeFactory;
    }

    public Interchange parse(String edifact)
            throws InterchangeParsingException, MessageParsingException, InterchangeCriticalException {

        var allEdifactSegments = Arrays.asList(Split.bySegmentTerminator(edifact.replaceAll("\\n", "").strip()));

        return buildInterchange(allEdifactSegments);
    }

    private Interchange buildInterchange(List<String> allEdifactSegments)
            throws InterchangeParsingException, InterchangeCriticalException, MessageParsingException {

        final Interchange interchange = parseInterchange(allEdifactSegments);
        parseMessages(allEdifactSegments, interchange);
        return interchange;
    }

    private Interchange parseInterchange(List<String> allEdifactSegments)
            throws InterchangeCriticalException, InterchangeParsingException {

        final Interchange interchange;
        try {
            interchange = interchangeFactory.createInterchange(extractInterchangeEdifactSegments(allEdifactSegments));
            interchange.validate();
        } catch (InterchangeCriticalException | InterchangeParsingException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InterchangeCriticalException(ex);
        }
        return interchange;
    }

    private void parseMessages(List<String> allEdifactSegments, Interchange interchange)
            throws MessageParsingException, InterchangeParsingException {

        final List<Message> messages;
        try {
            messages = parseAllMessages(allEdifactSegments);
            messages.forEach(message -> {
                final var messageHeader = message.getMessageHeader();
                final var messageTrailer = message.getMessageTrailer();
                messageHeader.validate();
                messageTrailer.validate();
                if (!messageHeader.getSequenceNumber().equals(messageTrailer.getSequenceNumber())) {
                    throw new EdifactValidationException(
                        "Message header sequence number does not match trailer sequence number");
                }
                message.setInterchange(interchange);
            });
            interchange.setMessages(messages);
        } catch (Exception ex) {
            var interchangeHeader = interchange.getInterchangeHeader();
            throw new MessageParsingException(
                "Error parsing messages",
                interchangeHeader.getSender(),
                interchangeHeader.getRecipient(),
                interchangeHeader.getSequenceNumber(),
                interchangeHeader.isNhsAckRequested(),
                ex);
        }

        if (interchange.getInterchangeTrailer().getNumberOfMessages() != messages.size()) {
            var interchangeHeader = interchange.getInterchangeHeader();
            throw new InterchangeParsingException(
                "Interchange trailer message count does not equal actual message count",
                interchangeHeader.getSender(),
                interchangeHeader.getRecipient(),
                interchangeHeader.getSequenceNumber(),
                interchangeHeader.isNhsAckRequested());
        }
    }

    private List<Message> parseAllMessages(List<String> allEdifactSegments) {
        var allMessageHeaderSegmentIndexes =
            findAllIndexesOfSegment(allEdifactSegments, MessageHeader.KEY);
        var allMessageTrailerSegmentIndexes =
            findAllIndexesOfSegment(allEdifactSegments, MessageTrailer.KEY);

        var messageHeaderTrailerIndexPairs =
            zipIndexes(allMessageHeaderSegmentIndexes, allMessageTrailerSegmentIndexes);

        return messageHeaderTrailerIndexPairs.stream()
            .map(messageStartEndIndexPair -> allEdifactSegments.subList(
                messageStartEndIndexPair.getLeft(), messageStartEndIndexPair.getRight() + 1))
            .map(this::parseMessage)
            .collect(Collectors.toList());
    }

    private Message parseMessage(List<String> singleMessageEdifactSegments) {
        var messageTrailerIndex = singleMessageEdifactSegments.size() - 1;
        var firstMessageEndIndex = findAllIndexesOfSegment(singleMessageEdifactSegments, MESSAGE_END_SEGMENT).stream()
            .findFirst()
            .orElse(messageTrailerIndex);

        // first lines until end of message
        var onlyMessageLines = new ArrayList<>(singleMessageEdifactSegments.subList(0, firstMessageEndIndex));
        onlyMessageLines.add(singleMessageEdifactSegments.get(messageTrailerIndex));

        return new Message(onlyMessageLines);
    }

    private List<Pair<Integer, Integer>> zipIndexes(List<Integer> startIndexes, List<Integer> endIndexes) {
        if (startIndexes.size() != endIndexes.size()) {
            throw new ToEdifactParsingException(
                "Message header-trailer count mismatch: " + startIndexes.size() + "-" + endIndexes.size());
        }

        var indexPairs = Streams.zip(startIndexes.stream(), endIndexes.stream(), Pair::of)
            .collect(Collectors.toList());

        if (!areIndexesInOrder(indexPairs)) {
            throw new ToEdifactParsingException("Message trailer before message header");
        }

        return indexPairs;
    }

    private boolean areIndexesInOrder(List<Pair<Integer, Integer>> messageIndexPairs) {
        // trailer must go after header
        // next header must go after previous trailer
        return Comparators.isInOrder(
            messageIndexPairs.stream()
                .map(pair -> List.of(pair.getLeft(), pair.getRight()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList()),
            Comparator.naturalOrder());
    }

    private List<String> extractInterchangeEdifactSegments(List<String> allEdifactSegments) {
        var firstMessageHeaderIndex = findAllIndexesOfSegment(allEdifactSegments, MessageHeader.KEY).get(0);
        var allMessageTrailerIndexes = findAllIndexesOfSegment(allEdifactSegments, MessageTrailer.KEY);
        var lastMessageTrailerIndex = allMessageTrailerIndexes.get(allMessageTrailerIndexes.size() - 1);

        var segmentsBeforeFirstMessageHeader = allEdifactSegments.subList(0, firstMessageHeaderIndex);
        var segmentsAfterLastMessageTrailer = allEdifactSegments.subList(
            lastMessageTrailerIndex + 1, allEdifactSegments.size());

        return Stream.of(segmentsBeforeFirstMessageHeader, segmentsAfterLastMessageTrailer)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    private List<Integer> findAllIndexesOfSegment(List<String> list, String key) {
        return IntStream.range(0, list.size())
            .filter(i -> list.get(i).startsWith(key))
            .boxed()
            .collect(Collectors.toList());
    }
}
