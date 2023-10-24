package org.octri.authentication.server.security.repository;

import java.util.Optional;

import org.octri.authentication.server.security.entity.SessionEvent;
import org.octri.authentication.server.security.entity.SessionEvent.EventType;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * {@link JpaRepository} for manipulating {@link SessionEvent} entities.
 */
public interface SessionEventRepository extends JpaRepository<SessionEvent, Long> {

	Optional<SessionEvent> findFirstBySessionIdAndEvent(String sessionId, EventType eventType);

}
