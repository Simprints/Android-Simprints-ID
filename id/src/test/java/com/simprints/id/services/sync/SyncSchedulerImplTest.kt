package com.simprints.id.services.sync

import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.images.ImageUpSyncScheduler
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.eventsync.EventSyncManager
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SyncSchedulerImplTest {


    @MockK
    lateinit var eventSyncManager: EventSyncManager

    @MockK
    lateinit var imageUpSyncScheduler: ImageUpSyncScheduler

    @MockK
    lateinit var configManager: ConfigManager

    @MockK
    lateinit var authManager: AuthManager


    lateinit var syncScheduler: SyncSchedulerImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        syncScheduler = SyncSchedulerImpl(eventSyncManager, imageUpSyncScheduler, configManager, authManager)
    }

    @Test
    fun `scheduleBackgroundSyncs should call the correct methods`() {
        syncScheduler.scheduleBackgroundSyncs()

        verify(exactly = 1) { eventSyncManager.scheduleSync() }
        verify(exactly = 1) { imageUpSyncScheduler.scheduleImageUpSync() }
        verify(exactly = 1) { configManager.scheduleSyncConfiguration() }
        verify(exactly = 1) { authManager.scheduleSecurityStateCheck() }
    }

    @Test
    fun `cancelBackgroundSyncs should call the correct methods`() {
        syncScheduler.cancelBackgroundSyncs()

        verify(exactly = 1) { eventSyncManager.cancelScheduledSync() }
        verify(exactly = 1) { imageUpSyncScheduler.cancelImageUpSync() }
        verify(exactly = 1) { configManager.cancelScheduledSyncConfiguration() }
        verify(exactly = 1) { authManager.cancelSecurityStateCheck() }
    }
}
