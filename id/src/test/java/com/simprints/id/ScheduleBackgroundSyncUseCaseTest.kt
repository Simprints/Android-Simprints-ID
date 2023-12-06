package com.simprints.id

import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.images.ImageUpSyncScheduler
import io.mockk.MockKAnnotations
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
    lateinit var configManager: ConfigManager

    @MockK
    lateinit var authManager: AuthManager

    @MockK
    lateinit var authStore: AuthStore

    private lateinit var useCase: ScheduleBackgroundSyncUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = ScheduleBackgroundSyncUseCase(
            eventSyncManager,
            imageUpSyncScheduler,
            configManager,
            authManager,
            authStore,
        )
    }

    @Test
    fun `If user is signed in - schedules all syncs when called`() = runTest {
        every { authStore.signedInProjectId } returns "projectId"

        useCase.invoke()

        verify {
            eventSyncManager.scheduleSync()
            imageUpSyncScheduler.scheduleImageUpSync()
            configManager.scheduleSyncConfiguration()
            authManager.scheduleSecurityStateCheck()
        }
    }

    @Test
    fun `If user is not signed in - does nothing`() = runTest {
        every { authStore.signedInProjectId } returns ""

        useCase.invoke()

        verify(exactly = 0) {
            eventSyncManager.scheduleSync()
            imageUpSyncScheduler.scheduleImageUpSync()
            configManager.scheduleSyncConfiguration()
            authManager.scheduleSecurityStateCheck()
        }
    }
}
