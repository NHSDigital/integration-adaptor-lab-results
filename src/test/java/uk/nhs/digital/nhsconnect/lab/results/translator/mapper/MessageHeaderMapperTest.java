package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Organization;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.mesh.RecipientMailboxIdMappings;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Interchange;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.InterchangeHeader;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.utils.ResourceFullUrlGenerator;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageHeaderMapperTest {

    private static final String MESSAGE_HEADER_ID = "8d8ac732-f057-4fe0-bacf-ed3700ab21da";
    private static final Instant INTERCHANGE_TRANSLATION_TIME = Instant.ofEpochSecond(123);
    private static final String INTERCHANGE_SENDER = "some_sender";
    private static final String DIAGNOSTIC_REPORT_FULL_URL = "diag_report_url";
    private static final String PERFORMING_ORGANIZATION_FULL_URL = "perf_org_url";
    private static final String REQUESTING_ORGANIZATION_FULL_URL = "req_org_url";
    private static final String SENDER_MAILBOX = "sender_mailbox";

    @Mock
    private UUIDGenerator uuidGenerator;
    @Mock
    private ResourceFullUrlGenerator fullUrlGenerator;
    @Mock
    private RecipientMailboxIdMappings recipientMailboxIdMappings;

    @InjectMocks
    private MessageHeaderMapper messageHeaderMapper;

    @Test
    void when_mappingMessageToMessageHeader_expect_computedFieldsAreMapped() {
        var message = mock(Message.class);
        var diagnosticReport = mock(DiagnosticReport.class);
        var requestingOrganization = mock(Organization.class);
        var performingOrganization = mock(Organization.class);

        var interchange = mock(Interchange.class);
        var interchangeHeader = mock(InterchangeHeader.class);

        when(message.getInterchange()).thenReturn(interchange);
        when(interchange.getInterchangeHeader()).thenReturn(interchangeHeader);
        when(interchangeHeader.getTranslationTime()).thenReturn(INTERCHANGE_TRANSLATION_TIME);
        when(interchangeHeader.getSender()).thenReturn(INTERCHANGE_SENDER);

        when(fullUrlGenerator.generate(diagnosticReport)).thenReturn(DIAGNOSTIC_REPORT_FULL_URL);
        when(fullUrlGenerator.generate(performingOrganization)).thenReturn(PERFORMING_ORGANIZATION_FULL_URL);
        when(fullUrlGenerator.generate(requestingOrganization)).thenReturn(REQUESTING_ORGANIZATION_FULL_URL);

        when(uuidGenerator.generateUUID()).thenReturn(MESSAGE_HEADER_ID);

        when(recipientMailboxIdMappings.getRecipientMailboxId(INTERCHANGE_SENDER)).thenReturn(SENDER_MAILBOX);

        var messageHeader = messageHeaderMapper.mapToMessageHeader(
            message, diagnosticReport, performingOrganization, requestingOrganization);

        assertAll(
            () -> assertThat(messageHeader.getId()).isEqualTo(MESSAGE_HEADER_ID),
            () -> assertThat(messageHeader.getTimestamp()).isEqualTo("1970-01-01T01:02:03.000"),
            () -> assertThat(messageHeader.getResponsible().getReference()).isEqualTo(PERFORMING_ORGANIZATION_FULL_URL),
            () -> assertThat(messageHeader.getSender().getReference()).isEqualTo(PERFORMING_ORGANIZATION_FULL_URL),
            () -> assertThat(messageHeader.getReceiver().getReference()).isEqualTo(REQUESTING_ORGANIZATION_FULL_URL)
        );
    }
}
