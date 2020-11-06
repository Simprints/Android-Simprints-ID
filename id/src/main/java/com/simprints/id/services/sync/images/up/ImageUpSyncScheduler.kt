package com.simprints.id.services.sync.images.up

interface ImageUpSyncScheduler {

    fun scheduleImageUpSync()
    fun cancelImageUpSync()

}
