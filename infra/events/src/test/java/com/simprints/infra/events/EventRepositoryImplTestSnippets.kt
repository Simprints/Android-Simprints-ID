package com.simprints.infra.events

import android.os.Build
import android.os.Build.VERSION
import com.simprints.infra.config.store.models.GeneralConfiguration.Modality
import com.simprints.infra.events.event.domain.models.session.DatabaseInfo
import com.simprints.infra.events.event.domain.models.session.Device
import com.simprints.infra.events.event.domain.models.session.SessionScope
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.createAlertScreenEvent
import com.simprints.infra.events.sampledata.createSessionScope
import io.mockk.coEvery
import kotlinx.coroutines.flow.flowOf

internal fun EventRepositoryImplTest.mockDbToHaveOneOpenSession(id: String = GUID1): SessionScope {
    val oldOpenSession: SessionScope = createSessionScope(id, isClosed = false)
    coEvery { eventLocalDataSource.countSessions() } returns 1

    // Mock query for open sessions
    coEvery { eventLocalDataSource.loadOpenedSessions() } returns listOf(oldOpenSession)

    return oldOpenSession
}

internal fun EventRepositoryImplTest.mockDbToBeEmpty() {
    coEvery { eventLocalDataSource.countSessions() } returns 0
    coEvery { eventLocalDataSource.loadOpenedSessions() } returns listOf()
}

fun assertANewSessionCaptureWasAdded(scope: SessionScope): Boolean =
    scope.projectId == DEFAULT_PROJECT_ID &&
        scope.createdAt == EventRepositoryImplTest.NOW &&
        scope.endedAt == null &&
        scope.payload.modalities == listOf(Modality.FINGERPRINT, Modality.FACE) &&
        scope.payload.sidVersion == EventRepositoryImplTest.APP_VERSION_NAME &&
        scope.payload.language == EventRepositoryImplTest.LANGUAGE &&
        scope.payload.device == Device(
            VERSION.SDK_INT.toString(),
            Build.MANUFACTURER + "_" + Build.MODEL,
            EventRepositoryImplTest.DEVICE_ID
        ) &&
        scope.payload.databaseInfo == DatabaseInfo(0)


fun assertThatSessionScopeClosed(scope: SessionScope): Boolean = scope.endedAt != null

internal fun EventRepositoryImplTest.mockDbToHaveEvents(id: String) {
    val event = createAlertScreenEvent()
    coEvery { eventLocalDataSource.loadEventsInSession(sessionId = id) } returns listOf(event)
    coEvery { eventLocalDataSource.loadAllEvents() } returns flowOf(event)
}
