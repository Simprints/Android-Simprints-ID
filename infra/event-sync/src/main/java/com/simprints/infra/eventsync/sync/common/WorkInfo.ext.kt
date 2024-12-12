package com.simprints.infra.eventsync.sync.common

import androidx.work.WorkInfo

internal const val OUTPUT_FAILED_BECAUSE_RELOGIN_REQUIRED = "OUTPUT_FAILED_BECAUSE_RELOGIN_REQUIRED"
internal const val OUTPUT_FAILED_BECAUSE_TOO_MANY_REQUESTS = "OUTPUT_FAILED_BECAUSE_TOO_MANY_REQUESTS"
internal const val OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION = "OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION"
internal const val OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE = "OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE"
internal const val OUTPUT_ESTIMATED_MAINTENANCE_TIME = "OUTPUT_ESTIMATED_MAINTENANCE_TIME"

internal fun WorkInfo.didFailBecauseReloginRequired(): Boolean = this.outputData.getBoolean(OUTPUT_FAILED_BECAUSE_RELOGIN_REQUIRED, false)

internal fun WorkInfo.didFailBecauseCloudIntegration(): Boolean = this.outputData.getBoolean(OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION, false)

internal fun WorkInfo.didFailBecauseBackendMaintenance(): Boolean =
    this.outputData.getBoolean(OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE, false)

internal fun WorkInfo.didFailBecauseTooManyRequests(): Boolean = this.outputData.getBoolean(OUTPUT_FAILED_BECAUSE_TOO_MANY_REQUESTS, false)

internal fun WorkInfo.getEstimatedOutageTime(): Long = this.outputData.getLong(OUTPUT_ESTIMATED_MAINTENANCE_TIME, 0L)
