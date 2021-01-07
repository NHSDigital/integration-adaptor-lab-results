package uk.nhs.digital.nhsconnect.lab.results.mesh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.OutboundMeshMessage;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class RecipientMailboxIdMappings {
    @Value("${labresults.mesh.recipientToMailboxIdMappings}")
    private final String recipientToMailboxIdMappings;

    @Autowired
    public RecipientMailboxIdMappings(@Value("${labresults.mesh.recipientToMailboxIdMappings}") String recipientToMailboxIdMappings) {
        this.recipientToMailboxIdMappings = recipientToMailboxIdMappings;
    }

    public String getRecipientMailboxId(OutboundMeshMessage outboundMeshMessage) {
        Map<String, String> mappings = createMappings();
        String recipient = outboundMeshMessage.getHaTradingPartnerCode();
        if (!mappings.containsKey(recipient)) {
            throw new MeshRecipientUnknownException("Couldn't decode recipient: " + recipient);
        }

        return mappings.get(recipient);
    }

    private Map<String, String> createMappings() {
        return Stream.of(recipientToMailboxIdMappings.replaceAll(" ", "\n").split("\n"))
            .map(row -> row.split("="))
            .peek(this::validateMappings)
            .collect(Collectors.toMap(row -> row[0].strip(), row -> row[1].strip()));
    }

    private void validateMappings(String[] rows) {
        if (rows.length < 2) {
            throw new MeshRecipientUnknownException("LAB_RESULTS_MESH_RECIPIENT_MAILBOX_ID_MAPPINGS env var doesn't contain valid recipient to mailbox mapping");
        }
    }
}
