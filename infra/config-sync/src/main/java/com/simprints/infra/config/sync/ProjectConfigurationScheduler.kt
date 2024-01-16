package com.simprints.infra.config.sync

interface ProjectConfigurationScheduler {
    fun scheduleSync()
    fun cancelScheduledSync()
}
