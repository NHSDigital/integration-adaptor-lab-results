package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.MessageHeader;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.StringType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.mesh.RecipientMailboxIdMappings;
import uk.nhs.digital.nhsconnect.lab.results.model.FhirProfiles;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.utils.ResourceFullUrlGenerator;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MessageHeaderMapper {
    private final UUIDGenerator uuidGenerator;
    private final ResourceFullUrlGenerator fullUrlGenerator;
    private final RecipientMailboxIdMappings recipientMailboxIdMappings;

    public MessageHeader mapToMessageHeader(
            Message message, DiagnosticReport diagnosticReport,
            Organization performingOrganization, Organization requestingOrganization) {
        final var messageHeader = new MessageHeader();
        messageHeader.getMeta().addProfile(FhirProfiles.MESSAGE_HEADER);
        messageHeader.setId(uuidGenerator.generateUUID());

        messageHeader.addFocus(new Reference(fullUrlGenerator.generate(diagnosticReport)));

        messageHeader.setResponsible(new Reference(fullUrlGenerator.generate(performingOrganization)));

        var extension = messageHeader.addExtension();
        extension.setUrl("https://fhir.nhs.uk/STU3/StructureDefinition/Extension-ITK-MessageHandling-2");
        extension.addExtension(new Extension().setUrl("BusAckRequested").setValue(new BooleanType(true)));
        extension.addExtension(new Extension().setUrl("InfAckRequested").setValue(new BooleanType(true)));
        extension.addExtension(new Extension().setUrl("RecipientType").setValue(new Coding()
                .setSystem("https://fhir.nhs.uk/STU3/CodeSystem/ITK-RecipientType-1")
                .setCode("FA")
                .setDisplay("For Action")
            )
        );
//            it's mandatory according to MessageHeader profile but there is no value for Pathology nor Screening
//            closest one is https://fhir.nhs.uk/STU3/MessageDefinition/ITK-SendPayload-MessageDefinition-1
//            but it requires practitioner and organization that have different profiles than pathology
//        extension.addExtension(new Extension().setUrl("MessageDefinition").setValue(...))
        extension.addExtension(new Extension().setUrl("SenderReference").setValue(new StringType("None")));
        extension.addExtension(new Extension().setUrl("LocalExtension").setValue(new StringType("None")));

        messageHeader.setEvent(new Coding()
            .setSystem("https://fhir.nhs.uk/STU3/CodeSystem/ITK-MessageEvent-2")
            .setCode("ITK012M") // there is no code for NHS004 Screening
            .setDisplay("ITK National Pathology"));

        messageHeader.setSender(new Reference(fullUrlGenerator.generate(performingOrganization)));
        messageHeader.setReceiver(new Reference(fullUrlGenerator.generate(requestingOrganization)));

        messageHeader.getSource().setEndpoint(
            recipientMailboxIdMappings.getRecipientMailboxId(
                message.getInterchange().getInterchangeHeader().getSender()
            )
        );
        return messageHeader;
    }
}
