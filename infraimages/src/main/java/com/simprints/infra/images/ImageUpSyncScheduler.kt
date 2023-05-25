package com.simprints.infra.images

interface ImageUpSyncScheduler {

    fun scheduleImageUpSync()
    fun cancelImageUpSync()

}
