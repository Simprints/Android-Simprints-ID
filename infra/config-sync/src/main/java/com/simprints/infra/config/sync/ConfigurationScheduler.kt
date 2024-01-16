package com.simprints.infra.config.sync

interface ConfigurationScheduler {
    fun scheduleSync()
    fun cancelScheduledSync()
}
