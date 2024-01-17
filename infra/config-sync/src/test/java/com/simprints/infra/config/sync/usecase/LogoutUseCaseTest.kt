package com.simprints.infra.config.sync.usecase

import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.authlogic.worker.SecurityStateScheduler
import com.simprints.infra.config.sync.ProjectConfigurationScheduler
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.images.ImageUpSyncScheduler
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class LogoutUseCaseTest {

    @MockK
    private lateinit var configScheduler: ProjectConfigurationScheduler

    @MockK
    private lateinit var imageUpSyncScheduler: ImageUpSyncScheduler

    @MockK
    private lateinit var eventSyncManager: EventSyncManager

    @MockK
    private lateinit var securityStateScheduler: SecurityStateScheduler

    @MockK
    private lateinit var authManager: AuthManager

    private lateinit var useCase: LogoutUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = LogoutUseCase(
            configScheduler = configScheduler,
            securityStateScheduler = securityStateScheduler,
            imageUpSyncScheduler = imageUpSyncScheduler,
            eventSyncManager = eventSyncManager,
            authManager = authManager,
        )
    }

    @Test
    fun `Fully logs out when called`() = runTest {
        useCase.invoke()

        verify {
            securityStateScheduler.cancelSecurityStateCheck()
            imageUpSyncScheduler.cancelImageUpSync()
            configScheduler.cancelProjectSync()
            configScheduler.cancelDeviceSync()
            eventSyncManager.cancelScheduledSync()
        }
        coVerify {
            authManager.signOut()
            eventSyncManager.deleteSyncInfo()
        }
    }
}
