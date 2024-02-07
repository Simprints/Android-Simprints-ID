package com.simprints.infra.config.sync

interface ProjectConfigurationScheduler {
    fun scheduleProjectSync()
    fun cancelProjectSync()

    fun startDeviceSync()
    fun scheduleDeviceSync()
    fun cancelDeviceSync()
}
