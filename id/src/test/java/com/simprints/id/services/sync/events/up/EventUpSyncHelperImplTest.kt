package com.simprints.id.services.sync.events.up

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.events_sync.up.EventUpSyncScopeRepository
import com.simprints.eventsystem.events_sync.up.domain.EventUpSyncOperation.UpSyncState.*
import com.simprints.eventsystem.sampledata.SampleDefaults
import com.simprints.eventsystem.sampledata.createPersonCreationEvent
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.config.domain.models.SynchronizationConfiguration
import com.simprints.infra.config.domain.models.UpSynchronizationConfiguration
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class EventUpSyncHelperImplTest {

    private val operation = SampleDefaults.projectUpSyncScope.operation
    private lateinit var uploadEventsChannel: Channel<Event>

    private lateinit var eventUpSyncHelper: EventUpSyncHelper

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var eventUpSyncScopeRepository: EventUpSyncScopeRepository

    @MockK
    private lateinit var timeHelper: TimeHelper

    private val synchronizationConfiguration = mockk<SynchronizationConfiguration>()
    private val projectConfiguration = mockk<ProjectConfiguration> {
        every { synchronization } returns synchronizationConfiguration
    }
    private val configManager = mockk<ConfigManager>(relaxed = true) {
        coEvery { getProjectConfiguration() } returns projectConfiguration
    }

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        eventUpSyncHelper = EventUpSyncHelperImpl(
            eventRepository,
            eventUpSyncScopeRepository,
            timeHelper,
            configManager
        )

        runBlocking {
            mockProgressEmission(emptyFlow())
        }
    }

    @Test
    fun countForUpSync_shouldInvokeTheEventRepo() = runTest {
        eventUpSyncHelper.countForUpSync(operation)

        coVerify { eventRepository.localCount(operation.projectId) }
    }

    @Test
    fun upSync_shouldConsumeRepoEventChannel() = runTest {
        eventUpSyncHelper.upSync(this, operation).toList()

        coVerify {
            eventRepository.uploadEvents(
                operation.projectId,
                any(), any(), any()
            )
        }
    }

    @Test
    fun `up sync should consume upload the events with the correct canSyncAllDataToSimprints parameter`() =
        runTest {
            coEvery { synchronizationConfiguration.up } returns UpSynchronizationConfiguration(
                simprints = UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration(
                    UpSynchronizationConfiguration.UpSynchronizationKind.ALL,
                ),
                coSync = mockk()
            )

            eventUpSyncHelper.upSync(this, operation).toList()

            coVerify {
                eventRepository.uploadEvents(
                    operation.projectId,
                    canSyncAllDataToSimprints = true,
                    canSyncBiometricDataToSimprints = false,
                    canSyncAnalyticsDataToSimprints = false
                )
            }
        }

    @Test
    fun `up sync should consume upload the events with the correct canSyncBiometricDataToSimprints parameter`() =
        runTest {
            coEvery { synchronizationConfiguration.up } returns UpSynchronizationConfiguration(
                simprints = UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration(
                    UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_BIOMETRICS,
                ),
                coSync = mockk()
            )

            eventUpSyncHelper.upSync(this, operation).toList()

            coVerify {
                eventRepository.uploadEvents(
                    operation.projectId,
                    canSyncAllDataToSimprints = false,
                    canSyncBiometricDataToSimprints = true,
                    canSyncAnalyticsDataToSimprints = false
                )
            }
        }

    @Test
    fun `up sync should consume upload the events with the correct canSyncAnalyticsDataToSimprints parameter`() =
        runTest {
            coEvery { synchronizationConfiguration.up } returns UpSynchronizationConfiguration(
                simprints = UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration(
                    UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_ANALYTICS,
                ),
                coSync = mockk()
            )

            eventUpSyncHelper.upSync(this, operation).toList()

            coVerify {
                eventRepository.uploadEvents(
                    operation.projectId,
                    canSyncAllDataToSimprints = false,
                    canSyncBiometricDataToSimprints = false,
                    canSyncAnalyticsDataToSimprints = true
                )
            }
        }

    @Test
    fun upSync_shouldProgressEventsInBatches() = runTest {
        val eventsToUpload = mutableListOf<Event>()
        repeat(3) { eventsToUpload += createPersonCreationEvent() }
        val sequenceOfProgress = flowOf(1, 1, 1)
        mockProgressEmission(sequenceOfProgress)

        val channel = eventUpSyncHelper.upSync(this, operation)

        val progress = channel.toList()
        sequenceOfProgress.onEach {
            assertThat(progress[it].progress).isEqualTo(it)
            assertThat(progress[it].operation.lastState).isEqualTo(RUNNING)
        }

        assertThat(progress.last().operation.lastState).isEqualTo(COMPLETE)
        coVerify(exactly = 4) { eventUpSyncScopeRepository.insertOrUpdate(any()) }
    }

    @Test
    fun upSync_shouldEmitAFailureIfUploadFails() = runTest {
        coEvery {
            eventRepository.uploadEvents(
                any(),
                any(),
                any(),
                any()
            )
        } throws Throwable("IO Exception")

        val channel = eventUpSyncHelper.upSync(this, operation)

        val progress = channel.toList()
        assertThat(progress.first().operation.lastState).isEqualTo(FAILED)
        coVerify(exactly = 1) { eventUpSyncScopeRepository.insertOrUpdate(any()) }
    }

    private fun mockProgressEmission(sequence: Flow<Int>) {
        uploadEventsChannel = Channel(capacity = Channel.UNLIMITED)
        coEvery { eventRepository.uploadEvents(any(), any(), any(), any()) } returns sequence
    }
}
