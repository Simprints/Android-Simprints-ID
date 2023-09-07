package com.simprints.feature.logincheck.usecases

import com.simprints.infra.config.ConfigManager
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.images.ImageUpSyncScheduler
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class CancelBackgroundSyncUseCaseTest {

    @MockK
    lateinit var eventSyncManager: EventSyncManager

    @MockK
    lateinit var imageUpSyncScheduler: ImageUpSyncScheduler

    @MockK
    lateinit var configManager: ConfigManager

    private lateinit var useCase: CancelBackgroundSyncUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = CancelBackgroundSyncUseCase(
            eventSyncManager,
            imageUpSyncScheduler,
            configManager
        )
    }

    @Test
    fun `Cancels all syncs when called`() {
        useCase.invoke()

        verify {
            eventSyncManager.cancelScheduledSync()
            imageUpSyncScheduler.cancelImageUpSync()
            configManager.cancelScheduledSyncConfiguration()
        }
    }
}
