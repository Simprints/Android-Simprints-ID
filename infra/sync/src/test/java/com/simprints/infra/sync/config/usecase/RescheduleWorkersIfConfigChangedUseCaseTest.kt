package com.simprints.infra.sync.config.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.DEVICE_STATUS_REPEAT_INTERVAL_HOURS
import com.simprints.infra.sync.ScheduleCommand
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.config.testtools.projectConfiguration
import com.simprints.infra.sync.config.testtools.simprintsUpSyncConfigurationConfiguration
import com.simprints.infra.sync.config.testtools.synchronizationConfiguration
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RescheduleWorkersIfConfigChangedUseCaseTest {
    @MockK
    private lateinit var syncOrchestrator: SyncOrchestrator

    private lateinit var useCase: RescheduleWorkersIfConfigChangedUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every { syncOrchestrator.execute(any<ScheduleCommand>()) } returns Job().apply { complete() }

        useCase = RescheduleWorkersIfConfigChangedUseCase(syncOrchestrator)
    }

    @Test
    fun `should not reschedule image upload when unmetered connection flag not changes`() = runTest {
        useCase(
            projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    up = synchronizationConfiguration.up.copy(
                        simprints = simprintsUpSyncConfigurationConfiguration.copy(
                            imagesRequireUnmeteredConnection = true,
                        ),
                    ),
                ),
            ),
            projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    up = synchronizationConfiguration.up.copy(
                        simprints = simprintsUpSyncConfigurationConfiguration.copy(
                            imagesRequireUnmeteredConnection = true,
                        ),
                    ),
                ),
            ),
        )

        verify(exactly = 0) { syncOrchestrator.execute(any<ScheduleCommand>()) }
    }

    @Test
    fun `should reschedule image upload when unmetered connection flag changes`() = runTest {
        val syncCommandJob = Job()
        every { syncOrchestrator.execute(any<ScheduleCommand>()) } returns syncCommandJob

        val useCaseJob = async {
            useCase(
                projectConfiguration.copy(
                    synchronization = synchronizationConfiguration.copy(
                        up = synchronizationConfiguration.up.copy(
                            simprints = simprintsUpSyncConfigurationConfiguration.copy(
                                imagesRequireUnmeteredConnection = false,
                            ),
                        ),
                    ),
                ),
                projectConfiguration.copy(
                    synchronization = synchronizationConfiguration.copy(
                        up = synchronizationConfiguration.up.copy(
                            simprints = simprintsUpSyncConfigurationConfiguration.copy(
                                imagesRequireUnmeteredConnection = true,
                            ),
                        ),
                    ),
                ),
            )
        }

        runCurrent()
        assertThat(useCaseJob.isCompleted).isFalse()

        syncCommandJob.complete()
        runCurrent()
        useCaseJob.await()

        verify { syncOrchestrator.execute(ScheduleCommand.Images.reschedule()) }
        assertThat(useCaseJob.isCompleted).isTrue()
    }

    @Test
    fun `should not reschedule device status report when interval does not change`() = runTest {
        useCase(
            projectConfiguration.copy(custom = null),
            projectConfiguration.copy(custom = mapOf(DEVICE_STATUS_REPEAT_INTERVAL_HOURS to JsonPrimitive(8))),
        )

        verify(exactly = 0) { syncOrchestrator.execute(ScheduleCommand.DeviceStatus.reschedule()) }
    }

    @Test
    fun `should reschedule device status report when interval changes`() = runTest {
        useCase(
            projectConfiguration.copy(custom = null),
            projectConfiguration.copy(custom = mapOf(DEVICE_STATUS_REPEAT_INTERVAL_HOURS to JsonPrimitive(24))),
        )

        verify(exactly = 1) { syncOrchestrator.execute(ScheduleCommand.DeviceStatus.reschedule()) }
        verify(exactly = 0) { syncOrchestrator.execute(ScheduleCommand.Images.reschedule()) }
    }

    @Test
    fun `should reschedule device status report when reporting is disabled`() = runTest {
        useCase(
            projectConfiguration.copy(custom = mapOf(DEVICE_STATUS_REPEAT_INTERVAL_HOURS to JsonPrimitive(8))),
            projectConfiguration.copy(custom = mapOf(DEVICE_STATUS_REPEAT_INTERVAL_HOURS to JsonPrimitive(0))),
        )

        verify(exactly = 1) { syncOrchestrator.execute(ScheduleCommand.DeviceStatus.reschedule()) }
    }
}
