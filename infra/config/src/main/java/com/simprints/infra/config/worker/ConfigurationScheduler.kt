package com.simprints.infra.config.worker

internal interface ConfigurationScheduler {
    fun scheduleSync()
    fun cancelScheduledSync()
}
