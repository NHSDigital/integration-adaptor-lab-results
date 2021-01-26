package uk.nhs.digital.nhsconnect.lab.results.sequence;

import org.springframework.data.repository.CrudRepository;

public interface SequenceDao extends CrudRepository<OutboundSequenceId, String> {
}