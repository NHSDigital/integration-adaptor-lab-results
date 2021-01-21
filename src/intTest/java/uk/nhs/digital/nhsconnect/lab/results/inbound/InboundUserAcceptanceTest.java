package uk.nhs.digital.nhsconnect.lab.results.inbound;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.IntegrationBaseTest;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.OutboundMeshMessage;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The test EDIFACT message is sent to the MESH mailbox where the adaptor receives
 * inbound transactions. The test waits for the transaction to be processed and compares
 * the message from the inbound queue to be the same to the message which has been sent.
 */

@Slf4j
public class InboundUserAcceptanceTest extends IntegrationBaseTest {

    private static final String RECIPIENT = "XX11";
    private static final OutboundMeshMessage OUTBOUND_MESH_MESSAGE = OutboundMeshMessage.create(RECIPIENT,
            WorkflowId.REGISTRATION, getSampleEdifactContent(), null, null);

    @BeforeEach
    void beforeEach() {
        clearMeshMailboxes();
        clearGpOutboundQueue();
        System.setProperty("LAB_RESULTS_SCHEDULER_ENABLED", "true"); //enable scheduling
    }

    @AfterEach
    void tearDown() {
        System.setProperty("LAB_RESULTS_SCHEDULER_ENABLED", "false");
    }

    @Test
    void testSendEdifactIsProcessedAndPushedToGpOutboundQueue() throws JMSException {

        labResultsMeshClient.sendEdifactMessage(OUTBOUND_MESH_MESSAGE);

        final Message gpOutboundQueueMessage = getGpOutboundQueueMessage();
        assertThat(gpOutboundQueueMessage).isNotNull();
        assertThat(parseTextMessage(gpOutboundQueueMessage)).isEqualTo("{\n  \"resourceType\": \"Parameters\"\n}");
    }

    private static String getSampleEdifactContent() {

        final String edifactHeader = "UNB+UNOA:2+TES5+XX11+020114:1619+00000003";
        final String edifactTrailer = "UNZ+1+00000003";

        final List<String> edifactContent = List.of(
                edifactHeader + "'",
                "UNH+00000004+FHSREG:0:1:FH:FHS001'",
                "BGM+++507'",
                "NAD+FHS+XX1:954'",
                "DTM+137:199201141619:203'",
                "RFF+950:F4'",
                "RFF+TN:18'",
                "S01+1'",
                "NAD+GP+2750922,295:900'",
                "NAD+RIC+RT:956'",
                "QTY+951:6'",
                "QTY+952:3'",
                "HEA+ACD+A:ZZZ'",
                "HEA+ATP+2:ZZZ'",
                "HEA+BM+S:ZZZ'",
                "HEA+DM+Y:ZZZ'",
                "DTM+956:19920114:102'",
                "LOC+950+GLASGOW'",
                "FTX+RGI+++BABY AT THE REYNOLDS-THORPE CENTRE'",
                "S02+2'",
                "PNA+PAT+NHS123:OPI+++SU:KENNEDY+FO:SARAH+TI:MISS+MI:ANGELA'",
                "DTM+329:19911209:102'",
                "PDI+2'",
                "NAD+PAT++??:26 FARMSIDE CLOSE:ST PAULS CRAY:ORPINGTON:KENT+++++BR6  7ET'",
                "UNT+24+00000004'",
                edifactTrailer + "'");

        return String.join("\n", edifactContent);
    }
}
