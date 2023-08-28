package com.simprints.feature.clientapi.session

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.argWhere
import com.simprints.core.tools.json.JsonHelper
import com.simprints.feature.clientapi.models.CoSyncEvents
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.UpSynchronizationConfiguration
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

class GetEventJsonForSessionUseCaseTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var configManager: ConfigManager

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var jsonHelper: JsonHelper

    private lateinit var useCase: GetEventJsonForSessionUseCase

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

        useCase = GetEventJsonForSessionUseCase(configManager, eventRepository, jsonHelper)
    }

    @Test
    fun `returns null if coSync disabled`() = runTest {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { synchronization.up.coSync.kind } returns UpSynchronizationConfiguration.UpSynchronizationKind.NONE
        }

        val result = useCase("sessionId")

        assertThat(result).isNull()
    }

    @Test
    fun `returns all events if full coSync enabled`() = runTest {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { synchronization.up.coSync.kind } returns UpSynchronizationConfiguration.UpSynchronizationKind.ALL
        }
        every { jsonHelper.toJson(argWhere<CoSyncEvents> { it.events.size == 5 }) } returns "json"

        val result = useCase("sessionId")

        assertThat(result).isNotNull()
    }

    @Test
    fun `returns only analytics events if anbalytics coSync enabled`() = runTest {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { synchronization.up.coSync.kind } returns UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_ANALYTICS
        }
        every { jsonHelper.toJson(argWhere<CoSyncEvents> { it.events.size == 3 }) } returns "json"

        val result = useCase("sessionId")

        assertThat(result).isNotNull()
    }

    @Test
    fun `returns only biometric events if biometric coSync enabled`() = runTest {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { synchronization.up.coSync.kind } returns UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_BIOMETRICS
        }
        every { jsonHelper.toJson(argWhere<CoSyncEvents> { it.events.size == 2 }) } returns "json"

        val result = useCase("sessionId")

        assertThat(result).isNotNull()
    }
}
