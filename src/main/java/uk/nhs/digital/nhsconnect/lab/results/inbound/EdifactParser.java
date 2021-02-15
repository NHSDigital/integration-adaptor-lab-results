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
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.InterchangeFactory;
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

    public Interchange parse(String edifact) {
        var allEdifactSegments = Arrays.asList(Split.bySegmentTerminator(edifact.replaceAll("\\n", "").strip()));

        return parseInterchange(allEdifactSegments);
    }

    private Interchange parseInterchange(List<String> allEdifactSegments) {
        Interchange interchange = interchangeFactory.createInterchange(
            extractInterchangeEdifactSegments(allEdifactSegments));

        var messages = parseAllMessages(allEdifactSegments);
        messages.forEach(message -> message.setInterchange(interchange));
        interchange.setMessages(messages);

        return interchange;
    }

    private List<Message> parseAllMessages(List<String> allEdifactSegments) {
        var allMessageHeaderSegmentIndexes = findAllIndexesOfSegment(allEdifactSegments, MessageHeader.KEY);
        var allMessageTrailerSegmentIndexes = findAllIndexesOfSegment(allEdifactSegments, MessageTrailer.KEY);

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
