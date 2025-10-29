package com.simprints.infra.sync.config.usecase

import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.Frequency
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.config.testtools.projectConfiguration
import com.simprints.infra.sync.config.testtools.synchronizationConfiguration
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ResetLocalRecordsIfConfigChangedUseCaseTest {
    @MockK
    private lateinit var syncOrchestrator: SyncOrchestrator

    @MockK
    private lateinit var eventSyncManager: EventSyncManager

    @MockK
    private lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    private lateinit var useCase: ResetLocalRecordsIfConfigChangedUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = ResetLocalRecordsIfConfigChangedUseCase(
            syncOrchestrator = syncOrchestrator,
            eventSyncManager = eventSyncManager,
            enrolmentRecordRepository = enrolmentRecordRepository,
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

        coVerify(exactly = 0) {
            syncOrchestrator.cancelEventSync()
            syncOrchestrator.rescheduleEventSync()
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

        coVerify {
            syncOrchestrator.cancelEventSync()
            syncOrchestrator.rescheduleEventSync()
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

        coVerify {
            syncOrchestrator.cancelEventSync()
            syncOrchestrator.rescheduleEventSync()
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

        coVerify {
            syncOrchestrator.cancelEventSync()
            syncOrchestrator.rescheduleEventSync()
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

        coVerify(exactly = 0) {
            syncOrchestrator.cancelEventSync()
            syncOrchestrator.rescheduleEventSync()
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

        coVerify(exactly = 0) {
            syncOrchestrator.cancelEventSync()
            syncOrchestrator.rescheduleEventSync()
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

        coVerify(exactly = 0) {
            syncOrchestrator.cancelEventSync()
            syncOrchestrator.rescheduleEventSync()
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

        coVerify(exactly = 0) {
            syncOrchestrator.cancelEventSync()
            syncOrchestrator.rescheduleEventSync()
            eventSyncManager.resetDownSyncInfo()
            enrolmentRecordRepository.deleteAll()
        }
    }
}
