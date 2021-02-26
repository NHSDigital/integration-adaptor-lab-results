//package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;
//
//import org.hl7.fhir.dstu3.model.Organization;
//import org.junit.jupiter.api.Test;
//import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
//
//import java.util.ArrayList;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class OrganizationMapperTest {
//
//    @Test
//    void testMapToPerformingOrganization() {
//        final Message message = new Message(new ArrayList<>());
//
//        assertThat(new OrganizationMapper().mapToPerformingOrganization(message))
//            .isExactlyInstanceOf(Organization.class);
//    }
//
//    @Test
//    void testMapToRequestingOrganization() {
//        final Message message = new Message(new ArrayList<>());
//
//        assertThat(new OrganizationMapper().mapToRequestingOrganization(message))
//            .isExactlyInstanceOf(Organization.class);
//    }
//}
