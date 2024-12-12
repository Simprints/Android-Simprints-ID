package com.simprints.infra.events.session

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.events.sampledata.createEventWithSessionId
import com.simprints.infra.events.sampledata.createSessionScope
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class SessionEventRepositoryImplTest {
    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var sessionDataCache: SessionDataCache

    private lateinit var sessionEventRepository: SessionEventRepositoryImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        sessionDataCache = SessionDataCache()

        sessionEventRepository = SessionEventRepositoryImpl(
            eventRepository,
            sessionDataCache,
        )
    }

    @Test
    fun `create session cache the new session`() = runTest {
        coEvery { eventRepository.createEventScope(any()) } returns mockk()

        sessionEventRepository.createSession()

        coVerify { eventRepository.createEventScope(EventScopeType.SESSION) }
        assertThat(sessionDataCache.eventScope).isNotNull()
    }

    @Test
    fun `when create session is called, then the event cache is cleared`() = runTest {
        coEvery { eventRepository.createEventScope(any()) } returns mockk()
        sessionDataCache.eventCache["test"] = mockk()

        sessionEventRepository.createSession()

        coVerify { eventRepository.createEventScope(EventScopeType.SESSION) }
        assertThat(sessionDataCache.eventCache).isEmpty()
    }

    @Test
    fun `closes existing sessions when creating new session`() = runTest {
        coEvery { eventRepository.createEventScope(any()) } returns createSessionScope("mockId")

        sessionEventRepository.createSession()

        coVerify { eventRepository.closeAllOpenScopes(EventScopeType.SESSION, any()) }
    }

    @Test
    fun `updates cache when saving current session scope`() = runTest {
        val mockEventScope = createSessionScope("mockId")
        val updatedScope = mockEventScope.copy(endedAt = Timestamp(42))
        sessionDataCache.eventScope = mockEventScope

        sessionEventRepository.saveSessionScope(updatedScope)

        coVerify { eventRepository.saveEventScope(updatedScope) }
        assertThat(sessionDataCache.eventScope?.endedAt).isEqualTo(Timestamp(42))
    }

    @Test
    fun `does not update cache when saving different session scope`() = runTest {
        val mockEventScope = createSessionScope("mockId")
        sessionDataCache.eventScope = createSessionScope("differentId")

        sessionEventRepository.saveSessionScope(mockEventScope)

        coVerify { eventRepository.saveEventScope(mockEventScope) }
        assertThat(sessionDataCache.eventScope?.id).isEqualTo("differentId")
    }

    @Test
    fun `returns true if there is open session in cache`() = runTest {
        sessionDataCache.eventScope = createSessionScope("mockId")
        assertThat(sessionEventRepository.hasOpenSession()).isTrue()
    }

    @Test
    fun `returns true if there is open session in local store`() = runTest {
        coEvery { eventRepository.getOpenEventScopes(any()) } returns listOf(mockk())

        assertThat(sessionEventRepository.hasOpenSession()).isTrue()
    }

    @Test
    fun `returns false if there is no session in cache or local store`() = runTest {
        coEvery { eventRepository.getOpenEventScopes(any()) } returns emptyList()

        assertThat(sessionEventRepository.hasOpenSession()).isFalse()
    }

    @Test
    fun `return current scope from cache if present`() = runTest {
        sessionDataCache.eventScope = createSessionScope("mockId")

        val loadedSession = sessionEventRepository.getCurrentSessionScope()

        assertThat(loadedSession.id).isEqualTo("mockId")
        coVerify(exactly = 0) { eventRepository.getOpenEventScopes(any()) }
    }

    @Test
    fun `return current scope from db if no cache`() = runTest {
        coEvery { eventRepository.getOpenEventScopes(any()) } returns listOf(createSessionScope("mockId"))
        sessionDataCache.eventCache["test"] = mockk()

        val loadedSession = sessionEventRepository.getCurrentSessionScope()

        assertThat(loadedSession.id).isEqualTo("mockId")
        assertThat(sessionDataCache.eventCache).isEmpty()
    }

    @Test
    fun `create current scope if no cache or db`() = runTest {
        coEvery { eventRepository.getOpenEventScopes(any()) } returns emptyList()
        coEvery { eventRepository.createEventScope(any()) } returns createSessionScope("mockId")

        val loadedSession = sessionEventRepository.getCurrentSessionScope()

        assertThat(loadedSession.id).isEqualTo("mockId")
    }

    @Test
    fun `updates location info in scope when removing`() = runTest {
        sessionDataCache.eventScope = createSessionScope("mockId")

        sessionEventRepository.removeLocationDataFromCurrentSession()

        coVerify {
            eventRepository.saveEventScope(
                withArg {
                    assertThat(it.payload.location).isNull()
                },
            )
        }
    }

    @Test
    fun `does not change scope if no location info`() = runTest {
        val scope = createSessionScope("mockId")
        sessionDataCache.eventScope = scope.copy(payload = scope.payload.copy(location = null))

        sessionEventRepository.removeLocationDataFromCurrentSession()

        coVerify(exactly = 0) { eventRepository.saveEventScope(any()) }
    }

    @Test
    fun `loads session events into cache on first call`() = runTest {
        sessionDataCache.eventScope = createSessionScope("mockId")

        val event = createEventWithSessionId("eventId", "mockId")
        coEvery { eventRepository.getEventsFromScope(any()) } returns listOf(event)

        sessionEventRepository.getEventsInCurrentSession()

        coVerify { eventRepository.getEventsFromScope(any()) }
        assertThat(sessionDataCache.eventCache["eventId"]).isEqualTo(event)
    }

    @Test
    fun `does not load events into cache if present`() = runTest {
        sessionDataCache.eventScope = createSessionScope("mockId")
        sessionDataCache.eventCache["eventId"] = createEventWithSessionId("eventId", "mockId")

        sessionEventRepository.getEventsInCurrentSession()

        coVerify(exactly = 0) { eventRepository.getEventsFromScope(any()) }
    }

    @Test
    fun `updates cached event on add or update`() = runTest {
        val event = createEventWithSessionId("eventId", "mockId")
        sessionDataCache.eventScope = mockk { every { id } returns "updatedEventScopeId" }
        sessionDataCache.eventCache[event.id] = event
        coEvery { eventRepository.addOrUpdateEvent(any(), any(), any()) } returns event.apply {
            this.scopeId = "updatedEventScopeId"
        }

        sessionEventRepository.addOrUpdateEvent(event)

        assertThat(sessionDataCache.eventCache["eventId"]?.scopeId).isEqualTo("updatedEventScopeId")
    }

    @Test
    fun `clears cache when closing session`() = runTest {
        sessionDataCache.eventScope = createSessionScope("mockId")
        sessionDataCache.eventCache["eventId"] = createEventWithSessionId("eventId", "mockId")

        sessionEventRepository.closeCurrentSession(null)

        coVerify { eventRepository.closeEventScope(any<EventScope>(), null) }
        assertThat(sessionDataCache.eventScope).isNull()
        assertThat(sessionDataCache.eventCache).isEmpty()
    }
}
