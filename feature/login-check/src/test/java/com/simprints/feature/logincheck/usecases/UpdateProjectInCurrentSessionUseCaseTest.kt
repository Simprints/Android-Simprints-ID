package com.simprints.feature.logincheck.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.IntentParsingEvent
import com.simprints.infra.events.event.domain.models.scope.DatabaseInfo
import com.simprints.infra.events.event.domain.models.scope.Device
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.events.event.domain.models.scope.EventScopePayload
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.events.session.SessionEventRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class UpdateProjectInCurrentSessionUseCaseTest {
    @MockK
    lateinit var eventRepository: SessionEventRepository

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
        coEvery { eventRepository.getCurrentSessionScope() } returns createBlankSessionScope(SIGNED_PROJECT_ID)
        coEvery { eventRepository.getEventsInCurrentSession() } returns emptyList()

        useCase()

        coVerify(exactly = 0) { eventRepository.addOrUpdateEvent(any()) }
    }

    @Test
    fun `Update session project ID when same as signed in project ID`() = runTest {
        coEvery { eventRepository.getCurrentSessionScope() } returns createBlankSessionScope(OTHER_PROJECT_ID)
        coEvery { eventRepository.getEventsInCurrentSession() } returns emptyList()

        useCase()

        coVerify {
            eventRepository.saveSessionScope(
                withArg {
                    assertThat(it.projectId).isEqualTo(SIGNED_PROJECT_ID)
                },
            )
        }
    }

    @Test
    fun `Update session project ID in all session events`() = runTest {
        coEvery { eventRepository.getCurrentSessionScope() } returns createBlankSessionScope(SIGNED_PROJECT_ID)
        coEvery { eventRepository.getEventsInCurrentSession() } returns listOf(createBlankSessionEvent(OTHER_PROJECT_ID))

        useCase()

        coVerify(exactly = 1) {
            eventRepository.addOrUpdateEvent(any())
        }
    }

    @Test
    fun `Update language in current session event when project ID updates`() = runTest {
        val language = "lang"
        coEvery { configManager.getDeviceConfiguration() } returns mockk {
            every { this@mockk.language } returns language
        }
        coEvery { eventRepository.getCurrentSessionScope() } returns createBlankSessionScope(OTHER_PROJECT_ID)
        coEvery { eventRepository.getEventsInCurrentSession() } returns emptyList()

        useCase()

        coVerify {
            eventRepository.saveSessionScope(
                withArg {
                    assertThat(it.payload.language).isEqualTo(language)
                },
            )
        }
    }

    private fun createBlankSessionScope(projectId: String) = EventScope(
        id = "eventId",
        projectId = projectId,
        type = EventScopeType.SESSION,
        createdAt = Timestamp(0L),
        endedAt = null,
        payload = EventScopePayload(
            endCause = null,
            modalities = emptyList(),
            sidVersion = "appVersionName",
            libSimprintsVersion = "libVersionName",
            language = "language",
            projectConfigurationUpdatedAt = "projectConfigurationUpdatedAt",
            device = Device("deviceId", "deviceModel", "deviceManufacturer"),
            databaseInfo = DatabaseInfo(0, 0),
        ),
    )

    private fun createBlankSessionEvent(projectId: String): Event = IntentParsingEvent(
        id = "eventId",
        payload = IntentParsingEvent.IntentParsingPayload(
            createdAt = Timestamp(0L),
            eventVersion = 0,
            integration = IntentParsingEvent.IntentParsingPayload.IntegrationInfo.ODK,
        ),
        type = EventType.INTENT_PARSING,
        projectId = projectId,
    )

    companion object {
        private const val SIGNED_PROJECT_ID = "projectId"
        private const val OTHER_PROJECT_ID = "otherProjectId"
    }
}
