package com.simprints.infra.events.session

import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.SessionEventRepository
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.events.event.domain.models.scope.EventScopeEndCause
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.logging.Simber
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

    override suspend fun createSession(): EventScope = withLockedContext {
        createSessionInternal()
    }

    override suspend fun saveSessionScope(eventScope: EventScope) = withLockedContext {
        saveSessionScopeInternal(eventScope)
    }

    override suspend fun hasOpenSession(): Boolean = withLockedContext {
        val session = sessionDataCache.eventScope ?: localSessionScope()
        session != null
    }

    override suspend fun getCurrentSessionScope(): EventScope = withLockedContext {
        getCurrentScopeInternal()
    }

    override suspend fun removeLocationDataFromCurrentSession() = withLockedContext {
        val sessionScope = getCurrentScopeInternal()
        if (sessionScope.payload.location != null) {
            val updatedSessionScope = sessionScope.copy(
                payload = sessionScope.payload.copy(location = null)
            )
            saveSessionScopeInternal(updatedSessionScope)
        }
    }

    override suspend fun getEventsInCurrentSession(): List<Event> = withLockedContext {
        val sessionId = getCurrentScopeInternal().id

        if (sessionDataCache.eventCache.isEmpty()) {
            loadEventsIntoCache(sessionId)
        }
        sessionDataCache.eventCache.values.toList()
    }

    override suspend fun addOrUpdateEvent(event: Event) = withLockedContext {
        val currentEvents = sessionDataCache.eventCache.values.toList()
        val savedEvent = eventRepository.addOrUpdateEvent(
            getCurrentScopeInternal(),
            event,
            currentEvents,
        )
        sessionDataCache.eventCache[savedEvent.id] = savedEvent
    }

    override suspend fun closeCurrentSession(reason: EventScopeEndCause?) = withLockedContext {
        eventRepository.closeEventScope(getCurrentScopeInternal(), reason)
        sessionDataCache.eventCache.clear()
        sessionDataCache.eventScope = null
    }

    /**
     * Scope for internal extension functions that must be called within locked context.
     */
    private object LockedContext

    private suspend inline fun <T> withLockedContext(
        crossinline block: suspend LockedContext.() -> T,
    ) = eventsLock.withLock { LockedContext.block() }


    private suspend fun LockedContext.createSessionInternal(): EventScope {
        closeAllSessions(EventScopeEndCause.NEW_SESSION)
        return eventRepository.createEventScope(EventScopeType.SESSION).also { sessionScope ->
            sessionDataCache.eventScope = sessionScope
            sessionDataCache.eventCache.clear()
        }
    }

    private suspend fun LockedContext.saveSessionScopeInternal(eventScope: EventScope) {
        if (eventScope.id == sessionDataCache.eventScope?.id) {
            sessionDataCache.eventScope = eventScope
        }
        eventRepository.saveEventScope(eventScope)
    }

    private suspend fun LockedContext.getCurrentScopeInternal(): EventScope {
        val cachedScope = sessionDataCache.eventScope
        if (cachedScope != null) return cachedScope

        val restoredScope = localSessionScope()
        if (restoredScope != null) {
            Simber.w("Restored session from DB")
            return restoredScope
        }

        Simber.w("Creating new session DB")
        return createSessionInternal()
    }

    private suspend fun LockedContext.localSessionScope() = eventRepository
        .getOpenEventScopes(EventScopeType.SESSION)
        .firstOrNull()
        ?.also { session ->
            sessionDataCache.eventScope = session
            loadEventsIntoCache(session.id)
        }

    private suspend fun LockedContext.loadEventsIntoCache(sessionId: String) {
        sessionDataCache.eventCache.clear()
        eventRepository.getEventsFromScope(sessionId).forEach { sessionDataCache.eventCache[it.id] = it }
    }

    /**
     * If the session is closing for normal reasons (i.e. came to a normal end), then it should be `null`.
     */
    private suspend fun LockedContext.closeAllSessions(reason: EventScopeEndCause?) {
        eventRepository.closeAllOpenScopes(EventScopeType.SESSION, reason)
    }
}
