package com.simprints.infra.events.session

import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.SessionEventRepository
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.events.event.domain.models.scope.EventScopeEndCause
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SessionEventRepositoryImpl @Inject constructor(
    private val eventRepository: EventRepository,
    private val sessionDataCache: SessionDataCache,
) : SessionEventRepository {

    private val eventsLock = Mutex()

    override suspend fun createSession(): EventScope = eventsLock.withLock {
        closeAllSessions(EventScopeEndCause.NEW_SESSION)
        return eventRepository.createEventScope(EventScopeType.SESSION).also { sessionScope ->
            sessionDataCache.eventScope = sessionScope
            sessionDataCache.eventCache.clear()
        }
    }

    /**
     * If the session is closing for normal reasons (i.e. came to a normal end), then it should be `null`.
     */
    private suspend fun closeAllSessions(reason: EventScopeEndCause?) {
        eventRepository.closeAllOpenScopes(EventScopeType.SESSION, reason)
    }

    override suspend fun saveSessionScope(eventScope: EventScope) = eventsLock.withLock {
        if (eventScope.id == sessionDataCache.eventScope?.id) {
            sessionDataCache.eventScope = eventScope
        }
        eventRepository.saveEventScope(eventScope)
    }

    override suspend fun hasOpenSession(): Boolean {
        val session = sessionDataCache.eventScope ?: localSessionScope()
        return session != null
    }

    override suspend fun getCurrentSessionScope(): EventScope =
        sessionDataCache.eventScope
            ?: localSessionScope()
            ?: createSession()

    override suspend fun removeLocationDataFromCurrentSession() {
        val sessionScope = getCurrentSessionScope()
        if (sessionScope.payload.location != null) {
            val updatedSessionScope = sessionScope.copy(
                payload = sessionScope.payload.copy(location = null)
            )
            saveSessionScope(updatedSessionScope)
        }
    }

    override suspend fun getEventsInCurrentSession(): List<Event> {
        val sessionId = getCurrentSessionScope().id
        return eventsLock.withLock {
            if (sessionDataCache.eventCache.isEmpty()) {
                loadEventsIntoCache(sessionId)
            }
            sessionDataCache.eventCache.values.toList()
        }
    }

    override suspend fun addOrUpdateEvent(event: Event) = eventsLock.withLock {
        val currentEvents = sessionDataCache.eventCache.values.toList()
        val savedEvent = eventRepository.addOrUpdateEvent(
            getCurrentSessionScope(),
            event,
            currentEvents
        )
        sessionDataCache.eventCache[savedEvent.id] = savedEvent
    }

    private suspend fun localSessionScope() = eventRepository
        .getOpenEventScopes(EventScopeType.SESSION)
        .firstOrNull()
        ?.also { session ->
            eventsLock.withLock {
                sessionDataCache.eventScope = session
                loadEventsIntoCache(session.id)
            }
        }

    private suspend fun loadEventsIntoCache(sessionId: String) {
        sessionDataCache.eventCache.clear()
        eventRepository.getEventsFromScope(sessionId)
            .forEach { sessionDataCache.eventCache[it.id] = it }
    }

    override suspend fun closeCurrentSession(reason: EventScopeEndCause?) = eventsLock.withLock {
        val session = getCurrentSessionScope()
        eventRepository.closeEventScope(session, reason)

        sessionDataCache.eventScope = null
        sessionDataCache.eventCache.clear()
    }
}
