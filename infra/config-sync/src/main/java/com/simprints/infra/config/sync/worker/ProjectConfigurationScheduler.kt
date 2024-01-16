package com.simprints.infra.config.sync.worker

internal interface ProjectConfigurationScheduler {
    fun scheduleSync()
    fun cancelScheduledSync()
}
