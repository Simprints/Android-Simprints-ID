package com.simprints.feature.clientapi.session

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.session.DatabaseInfo
import com.simprints.infra.events.event.domain.models.session.Device
import com.simprints.infra.events.event.domain.models.session.SessionCaptureEvent
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class UpdateProjectInCurrentSessionUseCaseTest {

    @MockK
    lateinit var eventRepository: EventRepository

    @MockK
    lateinit var authStore: AuthStore

    @MockK
    lateinit var configManager: ConfigManager

    lateinit var useCase: UpdateProjectInCurrentSessionUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { authStore.signedInProjectId } returns SIGNED_PROJECT_ID
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { general.modalities } returns emptyList()
        }

        useCase = UpdateProjectInCurrentSessionUseCase(eventRepository, authStore, configManager)
    }

    @Test
    fun `Does not update session project ID when same as signed in project ID`() = runTest {
        coEvery { eventRepository.getCurrentCaptureSessionEvent() } returns createBlankSessionEvent(SIGNED_PROJECT_ID)
        coEvery { eventRepository.observeEventsFromSession(any()) } returns emptyFlow()

        useCase()

        coVerify(exactly = 0) { eventRepository.addOrUpdateEvent(any()) }
    }

    @Test
    fun `Update session project ID when same as signed in project ID`() = runTest {
        coEvery { eventRepository.getCurrentCaptureSessionEvent() } returns createBlankSessionEvent(OTHER_PROJECT_ID)
        coEvery { eventRepository.observeEventsFromSession(any()) } returns emptyFlow()

        useCase()

        coVerify {
            eventRepository.addOrUpdateEvent(withArg {
                assertThat((it.payload as SessionCaptureEvent.SessionCapturePayload).projectId).isEqualTo(SIGNED_PROJECT_ID)
            })
        }
    }

    @Test
    fun `Update session project ID in all session events`() = runTest {
        coEvery { eventRepository.getCurrentCaptureSessionEvent() } returns createBlankSessionEvent(SIGNED_PROJECT_ID)
        coEvery { eventRepository.observeEventsFromSession(any()) } returns flowOf(createBlankSessionEvent(OTHER_PROJECT_ID))

        useCase()

        coVerify(exactly = 1) {
            eventRepository.addOrUpdateEvent(withArg { assertThat(it.labels.projectId).isEqualTo(SIGNED_PROJECT_ID) })
        }
    }

    private fun createBlankSessionEvent(projectId: String) = SessionCaptureEvent(
        id = "eventId",
        projectId = projectId,
        createdAt = 0,
        modalities = emptyList(),
        appVersionName = "appVersionName",
        libVersionName = "libVersionName",
        language = "language",
        device = Device("deviceId", "deviceModel", "deviceManufacturer"),
        databaseInfo = DatabaseInfo(0, 0),
    )

    companion object {
        private const val SIGNED_PROJECT_ID = "projectId"
        private const val OTHER_PROJECT_ID = "otherProjectId"
    }
}
