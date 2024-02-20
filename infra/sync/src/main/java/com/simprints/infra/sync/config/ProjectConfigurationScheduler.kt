package com.simprints.infra.sync.config

interface ProjectConfigurationScheduler {
    fun scheduleProjectSync()
    fun cancelProjectSync()

    fun startDeviceSync()
    fun scheduleDeviceSync()
    fun cancelDeviceSync()
}
