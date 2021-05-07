@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.simprints.id.data.db.event

import android.os.Build
import android.os.Build.VERSION
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.GUID1
import com.simprints.id.commontesttools.DefaultTestConstants.GUID2
import com.simprints.id.commontesttools.events.createAlertScreenEvent
import com.simprints.id.commontesttools.events.createEnrolmentRecordCreationEvent
import com.simprints.id.commontesttools.events.createSessionCaptureEvent
import com.simprints.id.data.db.event.domain.models.AlertScreenEvent
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventType.ARTIFICIAL_TERMINATION
import com.simprints.id.data.db.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.id.data.db.event.domain.models.session.DatabaseInfo
import com.simprints.id.data.db.event.domain.models.session.Device
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.id.domain.modality.Modes
import com.simprints.id.domain.modality.Modes.FINGERPRINT
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf

fun EventRepositoryImplTest.mockDbToHaveOneOpenSession(id: String = GUID1): SessionCaptureEvent {
    val oldOpenSession: SessionCaptureEvent = createSessionCaptureEvent(id).openSession()

    coEvery { eventLocalDataSource.count(SESSION_CAPTURE) } returns 1

    // Mock query for session by id
    coEvery { eventLocalDataSource.loadAllFromSession(sessionId = id) } returns flowOf(
        oldOpenSession
    )

    // Mock query for open sessions
    coEvery {
        eventLocalDataSource.loadAllSessions(false)
    } returns flowOf(oldOpenSession)

    return oldOpenSession
}

fun EventRepositoryImplTest.mockDbToBeEmpty() {
    coEvery { eventLocalDataSource.count(type = SESSION_CAPTURE) } returns 0
    coEvery {
        eventLocalDataSource.loadAllSessions(any())
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
        events.add(createAlertScreenEvent().copy(labels = EventLabels(sessionId = GUID1)))
    }

    coEvery {
        eventLocalDataSource.loadAllFromSession(sessionId = sessionId)
    } returns events.asFlow()

    return events
}

fun assertANewSessionCaptureWasAdded(event: Event): Boolean =
    event is SessionCaptureEvent &&
        event.payload.projectId == DEFAULT_PROJECT_ID &&
        event.payload.createdAt == EventRepositoryImplTest.NOW &&
        event.payload.modalities == listOf(Modes.FACE, FINGERPRINT) &&
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
        eventLocalDataSource.loadAllSessions(true)
    } returns (group1 + group2).filterIsInstance<SessionCaptureEvent>().asFlow()

    return (group1 + group2)
}

fun EventRepositoryImplTest.mockDbToLoadOldOpenSession(id: String) {
    val session = createSessionCaptureEvent(id, timeHelper.now())
    coEvery { eventLocalDataSource.loadAllFromSession(sessionId = id) } returns flowOf(session)
    coEvery { eventLocalDataSource.loadAll() } returns flowOf(session)
}

fun EventRepositoryImplTest.mockDbToLoadOpenSession(id: String) {
    val session = createSessionCaptureEvent(id).openSession()
    coEvery { eventLocalDataSource.loadAllFromSession(sessionId = id) } returns flowOf(session)
    coEvery { eventLocalDataSource.loadAll() } returns flowOf(session)
}

suspend fun EventRepositoryImplTest.mockDbToLoadPersonRecordEvents(nPersonRecordEvents: Int): List<Event> {
    val events = mutableListOf<EnrolmentRecordCreationEvent>()
    (0 until nPersonRecordEvents).forEach { _ ->
        events += createEnrolmentRecordCreationEvent()
    }

    coEvery {
        eventLocalDataSource.loadAllFromProject(projectId = DEFAULT_PROJECT_ID)
    } returns events.asFlow()

    return events.toList()
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

fun EventRepositoryImplTest.verifySessionHasNotGotUploaded(id: String) {
    coVerify(exactly = 0) { eventLocalDataSource.loadAllFromSession(sessionId = id) }
    coVerify {
        eventRemoteDataSource.post(any(), match {
            it.none { it.id == id }
        })
    }
}

fun SessionCaptureEvent.openSession(): SessionCaptureEvent =
    this.copy(payload = this.payload.copy(sessionIsClosed = false))

fun AlertScreenEvent.removeLabels(): AlertScreenEvent =
    this.copy(id = GUID1, labels = EventLabels())
