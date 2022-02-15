package com.simprints.id.services.sync.events.master.internal

import androidx.work.WorkInfo

const val OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION = "OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION"
const val OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE = "OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE"
const val OUTPUT_ESTIMATED_MAINTENANCE_TIME = "OUTPUT_ESTIMATED_MAINTENANCE_TIME"

fun WorkInfo.didFailBecauseCloudIntegration(): Boolean =
    this.outputData.getBoolean(OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION, false)

fun WorkInfo.didFailBecauseBackendMaintenance(): Boolean =
    this.outputData.getBoolean(OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE, false)

fun WorkInfo.getEstimatedOutageTime(): Long =
    this.outputData.getLong(OUTPUT_ESTIMATED_MAINTENANCE_TIME, 0L)
