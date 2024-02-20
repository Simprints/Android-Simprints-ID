package com.simprints.infra.sync.config.usecase

import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.images.ImageUpSyncScheduler
import com.simprints.infra.sync.SyncOrchestrator
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class LogoutUseCaseTest {

    @MockK
    private lateinit var syncOrchestrator: SyncOrchestrator

    @MockK
    private lateinit var imageUpSyncScheduler: ImageUpSyncScheduler

    @MockK
    private lateinit var eventSyncManager: EventSyncManager

    @MockK
    private lateinit var authManager: AuthManager

    private lateinit var useCase: LogoutUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = LogoutUseCase(
            syncOrchestrator = syncOrchestrator,
            imageUpSyncScheduler = imageUpSyncScheduler,
            eventSyncManager = eventSyncManager,
            authManager = authManager,
        )
    }

    @Test
    fun `Fully logs out when called`() = runTest {
        useCase.invoke()

        verify {
            imageUpSyncScheduler.cancelImageUpSync()
            eventSyncManager.cancelScheduledSync()
        }
        coVerify {
            syncOrchestrator.cancelBackgroundWork()
            authManager.signOut()
            eventSyncManager.deleteSyncInfo()
        }
    }
}
