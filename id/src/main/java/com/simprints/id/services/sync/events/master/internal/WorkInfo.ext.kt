package com.simprints.id.services.sync.events.master.internal

import androidx.work.WorkInfo

const val OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION = "OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION"
const val OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE = "OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE"

fun WorkInfo.didFailBecauseCloudIntegration(): Boolean =
    this.outputData.getBoolean(OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION, false)

fun WorkInfo.didFailBecauseBackendMaintenance(): Boolean =
    this.outputData.getBoolean(OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE, false)
