package com.simprints.infra.events

import android.os.Build
import android.os.Build.VERSION
import com.simprints.infra.config.store.models.GeneralConfiguration.Modality
import com.simprints.infra.events.event.domain.models.AlertScreenEvent
import com.simprints.infra.events.event.domain.models.ArtificialTerminationEvent
import com.simprints.infra.events.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload
import com.simprints.infra.events.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.event.domain.models.EventType.ARTIFICIAL_TERMINATION
import com.simprints.infra.events.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.infra.events.event.domain.models.session.DatabaseInfo
import com.simprints.infra.events.event.domain.models.session.Device
import com.simprints.infra.events.event.domain.models.session.SessionCaptureEvent
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.createSessionCaptureEvent
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.flow.flowOf

internal fun EventRepositoryImplTest.mockDbToHaveOneOpenSession(id: String = GUID1): SessionCaptureEvent {
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

internal fun EventRepositoryImplTest.mockDbToBeEmpty() {
    coEvery { eventLocalDataSource.count(type = SESSION_CAPTURE) } returns 0
    coEvery {
        eventLocalDataSource.loadOpenedSessions()
    } returns flowOf()
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

internal fun EventRepositoryImplTest.mockDbToLoadOpenSession(id: String) {
    val session = createSessionCaptureEvent(id).openSession()
    coEvery { eventLocalDataSource.loadAllFromSession(sessionId = id) } returns listOf(session)
    coEvery { eventLocalDataSource.loadAll() } returns flowOf(session)
}

internal fun EventRepositoryImplTest.verifyArtificialEventWasAdded(
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

fun SessionCaptureEvent.openSession(): SessionCaptureEvent =
    this.copy(payload = this.payload.copy(sessionIsClosed = false))

fun AlertScreenEvent.removeLabels(): AlertScreenEvent =
    this.copy(id = GUID1, labels = EventLabels())
