package com.simprints.id.services.sync

import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.services.sync.images.up.ImageUpSyncScheduler
import com.simprints.infra.config.ConfigManager
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class SyncSchedulerImplTest {

    private val eventSyncManager = mockk<EventSyncManager>()
    private val imageUpSyncScheduler = mockk<ImageUpSyncScheduler>()
    private val configManager = mockk<ConfigManager>()
    private val syncScheduler =
        SyncSchedulerImpl(eventSyncManager, imageUpSyncScheduler, configManager)

    @Test
    fun `scheduleBackgroundSyncs should call the correct methods`() {
        syncScheduler.scheduleBackgroundSyncs()

        verify(exactly = 1) { eventSyncManager.scheduleSync() }
        verify(exactly = 1) { imageUpSyncScheduler.scheduleImageUpSync() }
        verify(exactly = 1) { configManager.scheduleSyncConfiguration() }
    }

    @Test
    fun `cancelBackgroundSyncs should call the correct methods`() {
        syncScheduler.cancelBackgroundSyncs()

        verify(exactly = 1) { eventSyncManager.cancelScheduledSync() }
        verify(exactly = 1) { imageUpSyncScheduler.cancelImageUpSync() }
        verify(exactly = 1) { configManager.cancelScheduledSyncConfiguration() }
    }
}
