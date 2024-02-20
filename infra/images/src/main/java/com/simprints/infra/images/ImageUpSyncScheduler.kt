package com.simprints.infra.images

interface ImageUpSyncScheduler {

    /**
     * Schedules the background worker if there is none.
     */
    suspend fun scheduleImageUpSync()

    /**
     * Fully reschedule the background worker.
     * Should be used in when the configuration that affects scheduling has changed.
     */
    suspend fun rescheduleImageUpSync()
    fun cancelImageUpSync()

}
