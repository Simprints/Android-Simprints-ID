package com.simprints.infra.config.sync.worker

internal interface ConfigurationScheduler {
    fun scheduleSync()
    fun cancelScheduledSync()
}
