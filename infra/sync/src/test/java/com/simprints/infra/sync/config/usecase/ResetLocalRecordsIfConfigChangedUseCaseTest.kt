package com.simprints.infra.sync.config.usecase

import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.Frequency
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.eventsync.ResetDownSyncInfoUseCase
import com.simprints.infra.sync.ScheduleCommand
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.config.testtools.projectConfiguration
import com.simprints.infra.sync.config.testtools.synchronizationConfiguration
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ResetLocalRecordsIfConfigChangedUseCaseTest {
    @MockK
    private lateinit var syncOrchestrator: SyncOrchestrator

    @MockK
    private lateinit var resetDownSyncInfo: ResetDownSyncInfoUseCase

    @MockK
    private lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    private lateinit var useCase: ResetLocalRecordsIfConfigChangedUseCase
    private val scheduleCommandSlot = slot<ScheduleCommand>()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every { syncOrchestrator.execute(capture(scheduleCommandSlot)) } returns noopJob()

        useCase = ResetLocalRecordsIfConfigChangedUseCase(
            enrolmentRecordRepository = enrolmentRecordRepository,
            resetDownSyncInfo = resetDownSyncInfo,
            syncOrchestrator = syncOrchestrator,
        )
    }

    @Test
    fun `should not reset local records when partition type has not changed`() = runTest {
        useCase(
            projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    down = synchronizationConfiguration.down.copy(
                        simprints = synchronizationConfiguration.down.simprints?.copy(
                            partitionType = DownSynchronizationConfiguration.PartitionType.MODULE,
                        ),
                    ),
                ),
            ),
            projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    down = synchronizationConfiguration.down.copy(
                        simprints = synchronizationConfiguration.down.simprints?.copy(
                            partitionType = DownSynchronizationConfiguration.PartitionType.MODULE,
                        ),
                    ),
                ),
            ),
        )

        verify(exactly = 0) { syncOrchestrator.execute(any<ScheduleCommand>()) }
        coVerify(exactly = 0) {
            resetDownSyncInfo()
            enrolmentRecordRepository.deleteAll()
        }
    }

    @Test
    fun `should reset local records when partition type changed`() = runTest {
        useCase(
            projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    down = synchronizationConfiguration.down.copy(
                        simprints = synchronizationConfiguration.down.simprints?.copy(
                            partitionType = DownSynchronizationConfiguration.PartitionType.PROJECT,
                        ),
                    ),
                ),
            ),
            projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    down = synchronizationConfiguration.down.copy(
                        simprints = synchronizationConfiguration.down.simprints?.copy(
                            partitionType = DownSynchronizationConfiguration.PartitionType.MODULE,
                        ),
                    ),
                ),
            ),
        )

        verify(exactly = 1) {
            syncOrchestrator.execute(match<ScheduleCommand.EventsCommand> { it.action == ScheduleCommand.Action.UNSCHEDULE })
            syncOrchestrator.execute(match<ScheduleCommand.EventsCommand> { it.action == ScheduleCommand.Action.RESCHEDULE })
        }

        runCurrent()
        coVerify {
            resetDownSyncInfo()
            enrolmentRecordRepository.deleteAll()
        }
    }

    @Test
    fun `should reset local records when sync source changed from Simprints to CommCare`() = runTest {
        useCase(
            projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    down = synchronizationConfiguration.down.copy(
                        simprints = synchronizationConfiguration.down.simprints?.copy(
                            partitionType = DownSynchronizationConfiguration.PartitionType.PROJECT,
                        ),
                        commCare = null,
                    ),
                ),
            ),
            projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    down = synchronizationConfiguration.down.copy(
                        simprints = null,
                        commCare = DownSynchronizationConfiguration.CommCareDownSynchronizationConfiguration,
                    ),
                ),
            ),
        )

        verify(exactly = 1) {
            syncOrchestrator.execute(match<ScheduleCommand.EventsCommand> { it.action == ScheduleCommand.Action.UNSCHEDULE })
            syncOrchestrator.execute(match<ScheduleCommand.EventsCommand> { it.action == ScheduleCommand.Action.RESCHEDULE })
        }
        coVerify {
            resetDownSyncInfo()
            enrolmentRecordRepository.deleteAll()
        }
    }

    @Test
    fun `should reset local records when sync source changed from CommCare to Simprints`() = runTest {
        useCase(
            projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    down = synchronizationConfiguration.down.copy(
                        simprints = null,
                        commCare = DownSynchronizationConfiguration.CommCareDownSynchronizationConfiguration,
                    ),
                ),
            ),
            projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    down = synchronizationConfiguration.down.copy(
                        simprints = synchronizationConfiguration.down.simprints?.copy(
                            partitionType = DownSynchronizationConfiguration.PartitionType.PROJECT,
                        ),
                        commCare = null,
                    ),
                ),
            ),
        )

        verify(exactly = 1) {
            syncOrchestrator.execute(match<ScheduleCommand.EventsCommand> { it.action == ScheduleCommand.Action.UNSCHEDULE })
            syncOrchestrator.execute(match<ScheduleCommand.EventsCommand> { it.action == ScheduleCommand.Action.RESCHEDULE })
        }
        coVerify {
            resetDownSyncInfo()
            enrolmentRecordRepository.deleteAll()
        }
    }

    @Test
    fun `should reset local records when sync partition changes`() = runTest {
        useCase(
            projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    down = synchronizationConfiguration.down.copy(
                        simprints = synchronizationConfiguration.down.simprints?.copy(
                            partitionType = DownSynchronizationConfiguration.PartitionType.PROJECT,
                        ),
                        commCare = null,
                    ),
                ),
            ),
            projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    down = synchronizationConfiguration.down.copy(
                        simprints = null,
                        commCare = null,
                    ),
                ),
            ),
        )

        verify(exactly = 1) {
            syncOrchestrator.execute(match<ScheduleCommand.EventsCommand> { it.action == ScheduleCommand.Action.UNSCHEDULE })
            syncOrchestrator.execute(match<ScheduleCommand.EventsCommand> { it.action == ScheduleCommand.Action.RESCHEDULE })
        }
        coVerify {
            resetDownSyncInfo()
            enrolmentRecordRepository.deleteAll()
        }
    }

    @Test
    fun `should not reset local records when sync frequency changes`() = runTest {
        useCase(
            projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    down = synchronizationConfiguration.down.copy(
                        simprints = synchronizationConfiguration.down.simprints?.copy(
                            frequency = Frequency.ONLY_PERIODICALLY_UP_SYNC,
                        ),
                    ),
                ),
            ),
            projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    down = synchronizationConfiguration.down.copy(
                        simprints = synchronizationConfiguration.down.simprints?.copy(
                            frequency = Frequency.PERIODICALLY,
                        ),
                    ),
                ),
            ),
        )

        verify(exactly = 0) { syncOrchestrator.execute(any<ScheduleCommand>()) }
        coVerify(exactly = 0) {
            resetDownSyncInfo()
            enrolmentRecordRepository.deleteAll()
        }
    }

    @Test
    fun `should not reset local records when sync modules changes`() = runTest {
        useCase(
            projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    down = synchronizationConfiguration.down.copy(
                        simprints = synchronizationConfiguration.down.simprints?.copy(
                            moduleOptions = listOf("One".asTokenizableEncrypted(), "Two".asTokenizableEncrypted()),
                        ),
                    ),
                ),
            ),
            projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    down = synchronizationConfiguration.down.copy(
                        simprints = synchronizationConfiguration.down.simprints?.copy(
                            moduleOptions = listOf("Three".asTokenizableEncrypted()),
                        ),
                    ),
                ),
            ),
        )

        verify(exactly = 0) { syncOrchestrator.execute(any<ScheduleCommand>()) }
        coVerify(exactly = 0) {
            resetDownSyncInfo()
            enrolmentRecordRepository.deleteAll()
        }
    }

    @Test
    fun `should not reset local records when sync max age changes`() = runTest {
        useCase(
            projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    down = synchronizationConfiguration.down.copy(
                        simprints = synchronizationConfiguration.down.simprints?.copy(
                            maxAge = "PT24H",
                        ),
                    ),
                ),
            ),
            projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    down = synchronizationConfiguration.down.copy(
                        simprints = synchronizationConfiguration.down.simprints?.copy(
                            maxAge = "PT12H",
                        ),
                    ),
                ),
            ),
        )

        verify(exactly = 0) { syncOrchestrator.execute(any<ScheduleCommand>()) }
        coVerify(exactly = 0) {
            resetDownSyncInfo()
            enrolmentRecordRepository.deleteAll()
        }
    }

    @Test
    fun `should not reset local records when sync module count changes`() = runTest {
        useCase(
            projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    down = synchronizationConfiguration.down.copy(
                        simprints = synchronizationConfiguration.down.simprints?.copy(
                            maxNbOfModules = 2,
                        ),
                    ),
                ),
            ),
            projectConfiguration.copy(
                synchronization = synchronizationConfiguration.copy(
                    down = synchronizationConfiguration.down.copy(
                        simprints = synchronizationConfiguration.down.simprints?.copy(
                            maxNbOfModules = 5,
                        ),
                    ),
                ),
            ),
        )

        verify(exactly = 0) { syncOrchestrator.execute(any<ScheduleCommand>()) }
        coVerify(exactly = 0) {
            resetDownSyncInfo()
            enrolmentRecordRepository.deleteAll()
        }
    }

    private fun noopJob(): Job = Job().apply { complete() }
}
