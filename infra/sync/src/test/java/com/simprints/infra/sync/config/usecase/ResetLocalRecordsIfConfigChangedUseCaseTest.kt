package com.simprints.infra.sync.config.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.Frequency
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.sync.ExecutableSyncCommand
import com.simprints.infra.sync.SyncAction
import com.simprints.infra.sync.SyncCommand
import com.simprints.infra.sync.SyncCommandPayload
import com.simprints.infra.sync.SyncResponse
import com.simprints.infra.sync.SyncTarget
import com.simprints.infra.sync.config.testtools.projectConfiguration
import com.simprints.infra.sync.config.testtools.synchronizationConfiguration
import com.simprints.infra.sync.usecase.SyncUseCase
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ResetLocalRecordsIfConfigChangedUseCaseTest {
    @MockK
    private lateinit var sync: SyncUseCase

    @MockK
    private lateinit var eventSyncManager: EventSyncManager

    @MockK
    private lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    private lateinit var useCase: ResetLocalRecordsIfConfigChangedUseCase
    private val syncCommandSlot = slot<SyncCommand>()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every { sync(capture(syncCommandSlot)) } returns noopSyncResponse()

        useCase = ResetLocalRecordsIfConfigChangedUseCase(
            eventSyncManager = eventSyncManager,
            enrolmentRecordRepository = enrolmentRecordRepository,
            sync = sync,
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

        verify(exactly = 0) { sync(any()) }
        coVerify(exactly = 0) {
            eventSyncManager.resetDownSyncInfo()
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

        verify { sync(any()) }
        val command = syncCommandSlot.captured as ExecutableSyncCommand
        assertThat(command.target)
            .isEqualTo(SyncTarget.SCHEDULE_EVENTS)
        assertThat(command.action)
            .isEqualTo(SyncAction.STOP_AND_START)
        assertThat((command.payload as SyncCommandPayload.WithDelay).withDelay)
            .isFalse()

        command.blockToRunWhileStopped?.invoke()
        runCurrent()
        coVerify {
            eventSyncManager.resetDownSyncInfo()
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

        verify { sync(any()) }
        val command = syncCommandSlot.captured as ExecutableSyncCommand
        assertThat(command.target)
            .isEqualTo(SyncTarget.SCHEDULE_EVENTS)
        assertThat(command.action)
            .isEqualTo(SyncAction.STOP_AND_START)

        command.blockToRunWhileStopped?.invoke()
        runCurrent()
        coVerify {
            eventSyncManager.resetDownSyncInfo()
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

        verify { sync(any()) }
        val command = syncCommandSlot.captured as ExecutableSyncCommand
        assertThat(command.target)
            .isEqualTo(SyncTarget.SCHEDULE_EVENTS)
        assertThat(command.action)
            .isEqualTo(SyncAction.STOP_AND_START)

        command.blockToRunWhileStopped?.invoke()
        runCurrent()
        coVerify {
            eventSyncManager.resetDownSyncInfo()
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

        verify { sync(any()) }
        val command = syncCommandSlot.captured as ExecutableSyncCommand
        assertThat(command.target)
            .isEqualTo(SyncTarget.SCHEDULE_EVENTS)
        assertThat(command.action)
            .isEqualTo(SyncAction.STOP_AND_START)

        command.blockToRunWhileStopped?.invoke()
        runCurrent()
        coVerify {
            eventSyncManager.resetDownSyncInfo()
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

        verify(exactly = 0) { sync(any()) }
        coVerify(exactly = 0) {
            eventSyncManager.resetDownSyncInfo()
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

        verify(exactly = 0) { sync(any()) }
        coVerify(exactly = 0) {
            eventSyncManager.resetDownSyncInfo()
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

        verify(exactly = 0) { sync(any()) }
        coVerify(exactly = 0) {
            eventSyncManager.resetDownSyncInfo()
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

        verify(exactly = 0) { sync(any()) }
        coVerify(exactly = 0) {
            eventSyncManager.resetDownSyncInfo()
            enrolmentRecordRepository.deleteAll()
        }
    }

    private fun noopSyncResponse() = SyncResponse(
        syncCommandJob = Job().apply { complete() },
        syncStatusFlow = MutableStateFlow(mockk(relaxed = true)),
    )
}
