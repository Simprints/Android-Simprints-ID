package com.simprints.id

import com.simprints.fingerprint.infra.scanner.data.worker.FirmwareFileUpdateScheduler
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.images.ImageUpSyncScheduler
import com.simprints.infra.sync.SyncOrchestrator
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ScheduleBackgroundSyncUseCaseTest {

    @MockK
    lateinit var eventSyncManager: EventSyncManager

    @MockK
    lateinit var imageUpSyncScheduler: ImageUpSyncScheduler

    @MockK
    lateinit var syncOrchestrator: SyncOrchestrator

    @MockK
    lateinit var authStore: AuthStore

    @MockK
    lateinit var firmwareFileUpdateScheduler: FirmwareFileUpdateScheduler

    private lateinit var useCase: ScheduleBackgroundSyncUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = ScheduleBackgroundSyncUseCase(
            eventSyncManager,
            imageUpSyncScheduler,
            syncOrchestrator,
            authStore,
            firmwareFileUpdateScheduler,
        )
    }

    @Test
    fun `If user is signed in - schedules all syncs when called`() = runTest {
        every { authStore.signedInProjectId } returns "projectId"

        useCase.invoke()

        verify {
            eventSyncManager.scheduleSync()
            firmwareFileUpdateScheduler.scheduleOrCancelWorkIfNecessary()
        }
        coVerify {
            syncOrchestrator.scheduleBackgroundWork()
            imageUpSyncScheduler.scheduleImageUpSync()
        }
    }

    @Test
    fun `If user is not signed in - does nothing`() = runTest {
        every { authStore.signedInProjectId } returns ""

        useCase.invoke()

        verify(exactly = 0) {
            eventSyncManager.scheduleSync()
            firmwareFileUpdateScheduler.scheduleOrCancelWorkIfNecessary()
        }
        coVerify(exactly = 0) {
            syncOrchestrator.scheduleBackgroundWork()
            imageUpSyncScheduler.scheduleImageUpSync()
        }
    }
}
