package com.simprints.infra.images

interface ImageUpSyncScheduler {

    /**
     * Schedules the background worker if there is none.
     */
    suspend fun scheduleImageUpSync()


    suspend fun rescheduleImageUpSync()
    fun cancelImageUpSync()

}
