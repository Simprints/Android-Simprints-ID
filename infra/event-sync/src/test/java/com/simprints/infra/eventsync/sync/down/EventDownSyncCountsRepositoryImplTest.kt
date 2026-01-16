package com.simprints.infra.eventsync.sync.down

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.Frequency
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.events.event.domain.EventCount
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID_2
import com.simprints.infra.eventsync.SampleSyncScopes
import com.simprints.infra.eventsync.event.remote.EventRemoteDataSource
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.models.DownSyncCounts
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class EventDownSyncCountsRepositoryImplTest {
    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var downSyncScopeRepository: EventDownSyncScopeRepository

    @MockK
    private lateinit var eventRemoteDataSource: EventRemoteDataSource

    @MockK
    private lateinit var generalConfiguration: GeneralConfiguration

    @MockK
    private lateinit var downSynchronizationConfiguration: DownSynchronizationConfiguration

    private lateinit var repository: EventDownSyncCountsRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { generalConfiguration.modalities } returns emptyList()
        every { downSynchronizationConfiguration.simprints } returns
            DownSynchronizationConfiguration.SimprintsDownSynchronizationConfiguration(
                partitionType = DownSynchronizationConfiguration.PartitionType.MODULE,
                maxNbOfModules = 0,
                moduleOptions = emptyList(),
                maxAge = DownSynchronizationConfiguration.DEFAULT_DOWN_SYNC_MAX_AGE,
                frequency = Frequency.PERIODICALLY,
            )
        coEvery { configRepository.getProjectConfiguration() } returns mockk {
            every { general } returns generalConfiguration
            every { synchronization } returns mockk {
                every { down } returns downSynchronizationConfiguration
            }
        }

        repository = EventDownSyncCountsRepositoryImpl(
            configRepository = configRepository,
            downSyncScopeRepository = downSyncScopeRepository,
            eventRemoteDataSource = eventRemoteDataSource,
        )
    }

    @Test
    fun `countEventsToDownload correctly counts sync events`() = runTest {
        coEvery {
            downSyncScopeRepository.getDownSyncScope(any(), any(), any())
        } returns SampleSyncScopes.modulesDownSyncScope

        coEvery { eventRemoteDataSource.count(any()) } returnsMany listOf(
            EventCount(8, false),
            EventCount(18, true),
        )
        coEvery { configRepository.getDeviceConfiguration() } returns DeviceConfiguration(
            language = "",
            selectedModules = listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2),
            lastInstructionId = "",
        )

        val result = repository.countEventsToDownload()

        assertThat(result).isEqualTo(DownSyncCounts(26, isLowerBound = true))
    }

    @Test
    fun `countEventsToDownload returns zero when simprints down sync config is missing`() = runTest {
        coEvery { configRepository.getProjectConfiguration() } returns mockk {
            every { general } returns generalConfiguration
            every { synchronization } returns mockk {
                every { down } returns mockk {
                    every { simprints } returns null
                }
            }
        }

        val result = repository.countEventsToDownload()

        assertThat(result).isEqualTo(DownSyncCounts(count = 0, isLowerBound = false))
        coVerify(exactly = 0) { configRepository.getDeviceConfiguration() }
        coVerify(exactly = 0) { downSyncScopeRepository.getDownSyncScope(any(), any(), any()) }
        coVerify(exactly = 0) { eventRemoteDataSource.count(any()) }
    }
}
