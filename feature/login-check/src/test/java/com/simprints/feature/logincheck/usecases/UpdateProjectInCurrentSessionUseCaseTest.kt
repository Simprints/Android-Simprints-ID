package com.simprints.feature.logincheck.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.IntentParsingEvent
import com.simprints.infra.events.event.domain.models.session.DatabaseInfo
import com.simprints.infra.events.event.domain.models.session.Device
import com.simprints.infra.events.event.domain.models.session.SessionScope
import com.simprints.infra.events.event.domain.models.session.SessionScopePayload
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
    lateinit var configRepository: ConfigRepository

    lateinit var useCase: UpdateProjectInCurrentSessionUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { authStore.signedInProjectId } returns SIGNED_PROJECT_ID
        coEvery { configRepository.getProjectConfiguration() } returns mockk {
            every { general.modalities } returns emptyList()
        }

        useCase = UpdateProjectInCurrentSessionUseCase(eventRepository, authStore, configRepository)
    }

    @Test
    fun `Does not update session project ID when same as signed in project ID`() = runTest {
        coEvery { eventRepository.getCurrentSessionScope() } returns createBlankSessionScope(SIGNED_PROJECT_ID)
        coEvery { eventRepository.observeEventsFromSession(any()) } returns emptyFlow()

        useCase()

        coVerify(exactly = 0) { eventRepository.addOrUpdateEvent(any()) }
    }

    @Test
    fun `Update session project ID when same as signed in project ID`() = runTest {
        coEvery { eventRepository.getCurrentSessionScope() } returns createBlankSessionScope(OTHER_PROJECT_ID)
        coEvery { eventRepository.observeEventsFromSession(any()) } returns emptyFlow()

        useCase()

        coVerify {
            eventRepository.saveSessionScope(withArg {
                assertThat(it.projectId).isEqualTo(SIGNED_PROJECT_ID)
            })
        }
    }

    @Test
    fun `Update session project ID in all session events`() = runTest {
        coEvery { eventRepository.getCurrentSessionScope() } returns createBlankSessionScope(SIGNED_PROJECT_ID)
        coEvery { eventRepository.observeEventsFromSession(any()) } returns flowOf(createBlankSessionEvent(OTHER_PROJECT_ID))

        useCase()

        coVerify(exactly = 1) {
            eventRepository.addOrUpdateEvent(withArg {
                assertThat(it.projectId).isEqualTo(SIGNED_PROJECT_ID)
            })
        }
    }

    @Test
    fun `Update language in current session event when project ID updates`() = runTest {
        val language = "lang"
        coEvery { configRepository.getDeviceConfiguration() } returns mockk {
            every { this@mockk.language } returns language
        }
        coEvery { eventRepository.getCurrentSessionScope() } returns createBlankSessionScope(OTHER_PROJECT_ID)
        coEvery { eventRepository.observeEventsFromSession(any()) } returns emptyFlow()

        useCase()

        coVerify {
            eventRepository.saveSessionScope(withArg {
                assertThat(it.payload.language).isEqualTo(language)
            })
        }
    }

    private fun createBlankSessionScope(projectId: String) = SessionScope(
        id = "eventId",
        projectId = projectId,
        createdAt = Timestamp(0L),
        endedAt = null,
        payload = SessionScopePayload(
            endCause = null,
            modalities = emptyList(),
            sidVersion = "appVersionName",
            libSimprintsVersion = "libVersionName",
            language = "language",
            projectConfigurationUpdatedAt = "projectConfigurationUpdatedAt",
            device = Device("deviceId", "deviceModel", "deviceManufacturer"),
            databaseInfo = DatabaseInfo(0, 0),
        )
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
