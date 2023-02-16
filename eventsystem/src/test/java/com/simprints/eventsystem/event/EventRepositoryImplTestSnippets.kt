package com.simprints.eventsystem.event

import android.os.Build
import android.os.Build.VERSION
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.simprints.eventsystem.event.domain.models.AlertScreenEvent
import com.simprints.eventsystem.event.domain.models.ArtificialTerminationEvent
import com.simprints.eventsystem.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload
import com.simprints.eventsystem.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventType.ARTIFICIAL_TERMINATION
import com.simprints.eventsystem.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.eventsystem.event.domain.models.session.DatabaseInfo
import com.simprints.eventsystem.event.domain.models.session.Device
import com.simprints.eventsystem.event.domain.models.session.SessionCaptureEvent
import com.simprints.eventsystem.event.remote.EventRemoteDataSource
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID2
import com.simprints.eventsystem.sampledata.createAlertScreenEvent
import com.simprints.eventsystem.sampledata.createSessionCaptureEvent
import com.simprints.infra.config.domain.models.GeneralConfiguration.Modality
import com.simprints.testtools.common.syntax.mock
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf

fun EventRepositoryImplTest.mockDbToHaveOneOpenSession(id: String = GUID1): SessionCaptureEvent {
    val oldOpenSession: SessionCaptureEvent = createSessionCaptureEvent(id).openSession()

    coEvery { eventLocalDataSource.count(SESSION_CAPTURE) } returns 1

    // Mock query for session by id
    coEvery { eventLocalDataSource.loadAllFromSession(sessionId = id) } returns listOf(
        oldOpenSession
    )

    // Mock query for open sessions
    coEvery {
        eventLocalDataSource.loadOpenedSessions()
    } returns flowOf(oldOpenSession)

    return oldOpenSession
}

fun EventRepositoryImplTest.mockDbToBeEmpty() {
    coEvery { eventLocalDataSource.count(type = SESSION_CAPTURE) } returns 0
    coEvery {
        eventLocalDataSource.loadOpenedSessions()
    } returns flowOf()
}

fun EventRepositoryImplTest.mockDbToLoadSessionWithEvents(
    sessionId: String,
    sessionIsClosed: Boolean,
    nEvents: Int
): List<Event> {
    val events = mutableListOf<Event>()
    events.add(createSessionCaptureEvent(sessionId, isClosed = sessionIsClosed))
    repeat(nEvents) {
        events.add(createAlertScreenEvent().copy(labels = EventLabels(sessionId = sessionId)))
    }

    coEvery {
        eventLocalDataSource.loadAllFromSession(sessionId = sessionId)
    } returns events

    return events
}

fun assertANewSessionCaptureWasAdded(event: Event): Boolean =
    event is SessionCaptureEvent &&
        event.payload.projectId == DEFAULT_PROJECT_ID &&
        event.payload.createdAt == EventRepositoryImplTest.NOW &&
        event.payload.modalities == listOf(Modality.FINGERPRINT, Modality.FACE) &&
        event.payload.appVersionName == EventRepositoryImplTest.APP_VERSION_NAME &&
        event.payload.language == EventRepositoryImplTest.LANGUAGE &&
        event.payload.device == Device(
        VERSION.SDK_INT.toString(),
        Build.MANUFACTURER + "_" + Build.MODEL,
        EventRepositoryImplTest.DEVICE_ID
    ) &&
        event.payload.databaseInfo == DatabaseInfo(0) &&
        event.payload.endedAt == 0L &&
        !event.payload.sessionIsClosed


fun assertThatSessionCaptureEventWasClosed(event: Event): Boolean =
    event is SessionCaptureEvent && event.payload.endedAt > 0 && event.payload.sessionIsClosed

fun assertThatArtificialTerminationEventWasAdded(event: Event, id: String): Boolean =
    event is ArtificialTerminationEvent &&
        event.labels == EventLabels(
        sessionId = id,
        deviceId = EventRepositoryImplTest.DEVICE_ID,
        projectId = DEFAULT_PROJECT_ID
    ) &&
        event.payload.reason == NEW_SESSION &&
        event.payload.createdAt == EventRepositoryImplTest.NOW

fun EventRepositoryImplTest.mockDbToLoadTwoClosedSessionsWithEvents(
    nEventsInTotal: Int,
    sessionEvent1: String = GUID1,
    sessionEvent2: String = GUID2
): List<Event> {
    val group1 = mockDbToLoadSessionWithEvents(sessionEvent1, true, nEventsInTotal / 2 - 1)
    val group2 = mockDbToLoadSessionWithEvents(sessionEvent2, true, nEventsInTotal / 2 - 1)

    coEvery {
        eventLocalDataSource.loadOpenedSessions()
    } returns (group1 + group2).filterIsInstance<SessionCaptureEvent>().asFlow()

    coEvery {
        eventLocalDataSource.loadAllClosedSessionIds(any())
    } returns (group1 + group2).filterIsInstance<SessionCaptureEvent>().map { it.id }

    return (group1 + group2)
}

fun EventRepositoryImplTest.mockDbToLoadInvalidSessions(
    nEventsInTotal: Int,
    sessionEvent1: String = GUID1,
    sessionEvent2: String = GUID2
): List<Event> {
    val group1 = mockDbToLoadSessionWithEvents(sessionEvent1, true, nEventsInTotal / 2 - 1)
    val group2 = mockDbToLoadSessionWithEvents(sessionEvent2, true, nEventsInTotal / 2 - 1)

    coEvery {
        eventLocalDataSource.loadOpenedSessions()
    } returns (group1 + group2).filterIsInstance<SessionCaptureEvent>().asFlow()

    coEvery {
        eventLocalDataSource.loadAllFromSession(GUID2)
    } throws IllegalArgumentException()

    coEvery {
        eventLocalDataSource.loadAllClosedSessionIds(any())
    } returns (group1 + group2).filterIsInstance<SessionCaptureEvent>().map { it.id }

    return (group1 + group2)
}

fun EventRepositoryImplTest.mockDbToLoadTwoSessionsWithInvalidEvent(
    sessionId1: String = GUID1,
    sessionId2: String = GUID2,
): Map<String, List<String>> {
    val eventsForSession1 = listOf("event1", "event2")
    val eventsForSession2 = listOf("event3", "event4")

    coEvery {
        eventLocalDataSource.loadAllClosedSessionIds(any())
    } returns listOf(GUID1, GUID2)

    coEvery {
        eventLocalDataSource.loadAllFromSession(sessionId1)
    } throws JsonParseException(mock(), "")

    coEvery {
        eventLocalDataSource.loadAllFromSession(sessionId2)
    } throws JsonMappingException(mock(), "")

    coEvery {
        eventLocalDataSource.loadAllEventJsonFromSession(sessionId1)
    } returns eventsForSession1

    coEvery {
        eventLocalDataSource.loadAllEventJsonFromSession(sessionId2)
    } returns eventsForSession2

    return mapOf(sessionId1 to eventsForSession1, sessionId2 to eventsForSession2)
}

fun EventRepositoryImplTest.mockDbToLoadOpenSession(id: String) {
    val session = createSessionCaptureEvent(id).openSession()
    coEvery { eventLocalDataSource.loadAllFromSession(sessionId = id) } returns listOf(session)
    coEvery { eventLocalDataSource.loadAll() } returns flowOf(session)
}

fun EventRepositoryImplTest.verifyArtificialEventWasAdded(
    id: String,
    reason: ArtificialTerminationPayload.Reason
) {
    coVerify {
        eventLocalDataSource.insertOrUpdate(match {
            it.type == ARTIFICIAL_TERMINATION &&
                it.labels.sessionId == id &&
                (it as ArtificialTerminationEvent).payload.reason == reason
        })
    }
}

internal fun EventRepositoryImplTest.verifySessionHasNotGotUploaded(id: String, eventRemoteDataSource: EventRemoteDataSource) {
    coVerify(exactly = 0) { eventLocalDataSource.loadAllFromSession(sessionId = id) }
    coVerify {
        eventRemoteDataSource.post(any(), match { event ->
            event.none { it.id == id }
        })
    }
}

fun SessionCaptureEvent.openSession(): SessionCaptureEvent =
    this.copy(payload = this.payload.copy(sessionIsClosed = false))

fun AlertScreenEvent.removeLabels(): AlertScreenEvent =
    this.copy(id = GUID1, labels = EventLabels())
