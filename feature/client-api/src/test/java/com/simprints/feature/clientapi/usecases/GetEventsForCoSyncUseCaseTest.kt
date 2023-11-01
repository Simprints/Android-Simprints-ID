package com.simprints.feature.clientapi.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.feature.clientapi.models.CoSyncEvents
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.EnrolmentEventV2
import com.simprints.infra.events.event.domain.models.PersonCreationEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureBiometricsEvent
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GetEventsForCoSyncUseCaseTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var configManager: ConfigManager

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var tokenizationProcessor: TokenizationProcessor

    @MockK
    private lateinit var jsonHelper: JsonHelper

    private lateinit var useCase: GetEventsForCoSyncUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { eventRepository.observeEventsFromSession(any()) } returns flowOf(
            mockk<EnrolmentEventV2>(relaxed = true),
            mockk<PersonCreationEvent>(relaxed = true),
            mockk<FingerprintCaptureBiometricsEvent>(relaxed = true),
            mockk<FaceCaptureBiometricsEvent>(relaxed = true),
            mockk<EnrolmentEventV2>(relaxed = true),
        )

        useCase = GetEventsForCoSyncUseCase(configManager, eventRepository, jsonHelper, tokenizationProcessor)
    }

    @Test
    fun `returns null if coSync disabled`() = runTest {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { synchronization.up.coSync.kind } returns UpSynchronizationConfiguration.UpSynchronizationKind.NONE
        }

        val result = useCase(sessionId = "sessionId", project = mockk())

        assertThat(result).isNull()
    }

    @Test
    fun `returns all events if full coSync enabled`() = runTest {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { synchronization.up.coSync.kind } returns UpSynchronizationConfiguration.UpSynchronizationKind.ALL
        }
        every { jsonHelper.toJson(any<CoSyncEvents> ()) } returns "json"

        val result = useCase(sessionId = "sessionId", project = mockk())

        assertThat(result).isNotNull()
    }

    @Test
    fun `returns only analytics events if analytics coSync enabled`() = runTest {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { synchronization.up.coSync.kind } returns UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_ANALYTICS
        }
        every { jsonHelper.toJson(any<CoSyncEvents> ()) } returns "json"

        val result = useCase(sessionId = "sessionId", project = mockk())

        assertThat(result).isNotNull()
    }

    @Test
    fun `returns only biometric events if biometric coSync enabled`() = runTest {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { synchronization.up.coSync.kind } returns UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_BIOMETRICS
        }
        every { jsonHelper.toJson(any<CoSyncEvents> ()) } returns "json"

        val result = useCase(sessionId = "sessionId", project = mockk())

        assertThat(result).isNotNull()
    }
}
