package com.simprints.id.services.scheduledSync.people.master.internal

import androidx.work.WorkInfo

const val OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION = "OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION"

fun WorkInfo.didFailBecauseCloudIntegration(): Boolean =
    this.outputData.getBoolean(OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION, false)
