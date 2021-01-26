package uk.nhs.digital.nhsconnect.lab.results.sequence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("checkstyle:magicnumber")
class SequenceRepositoryTest {
    private static final String MAX_KEY = "max-key";
    private static final String NEW_KEY = "new-key";
    private static final OutboundSequenceId SEQUENCE_ID = new OutboundSequenceId(NEW_KEY, 1L);

    @InjectMocks
    private SequenceRepository sequenceRepository;

    @Mock
    private MongoOperations mongoOperations;

    @Test
    void when_getNextKey_expect_correctValue() {
        when(mongoOperations.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class),
                eq(OutboundSequenceId.class))).thenReturn(SEQUENCE_ID);

        assertThat(sequenceRepository.getNext(NEW_KEY)).isEqualTo(1L);
    }

    @Test
    void when_getMaxNextKey_expect_valueReset() {
        when(mongoOperations.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class),
                eq(OutboundSequenceId.class)))
                .thenReturn(new OutboundSequenceId(MAX_KEY, 100000000L))
                .thenReturn(new OutboundSequenceId(MAX_KEY, 100000001L));

        assertThat(sequenceRepository.getNext(MAX_KEY)).isEqualTo(1L);
    }

}