package uk.nhs.digital.nhsconnect.lab.results.sequence;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Objects;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Slf4j
@Repository
public class SequenceRepository {
    private static final String KEY = "key";
    private static final String SEQUENCE_NUMBER = "sequenceNumber";
    protected static final long MAX_SEQUENCE_NUMBER = 100_000_000L;

    @Autowired
    private MongoOperations mongoOperations;

    public Long getNext(final String key) {
        Long seqNumber = increment(key, MAX_SEQUENCE_NUMBER);
        if (seqNumber == 0) {
            seqNumber = increment(key, MAX_SEQUENCE_NUMBER);
        }
        return seqNumber;
    }

    private Long increment(final String key, final Long maxSequenceNumber) {
        return Objects.requireNonNull(mongoOperations.findAndModify(
            query(where(KEY).is(key)),
            new Update().inc(SEQUENCE_NUMBER, 1),
            options().returnNew(true).upsert(true),
            OutboundSequenceId.class
        )).getSequenceNumber() % maxSequenceNumber;
    }
}
