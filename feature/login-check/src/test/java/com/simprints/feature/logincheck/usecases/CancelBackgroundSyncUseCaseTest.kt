package com.simprints.feature.logincheck.usecases

import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.sync.SyncOrchestrator
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CancelBackgroundSyncUseCaseTest {

    @MockK
    lateinit var eventSyncManager: EventSyncManager

    @MockK
    lateinit var syncOrchestrator: SyncOrchestrator

    private lateinit var useCase: CancelBackgroundSyncUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = CancelBackgroundSyncUseCase(
            eventSyncManager,
            syncOrchestrator
        )
    }

    @Test
    fun `Cancels all syncs when called`() = runTest {
        useCase.invoke()

        verify {
            eventSyncManager.cancelScheduledSync()
        }
        coVerify {
            syncOrchestrator.cancelBackgroundWork()
        }
    }
}
