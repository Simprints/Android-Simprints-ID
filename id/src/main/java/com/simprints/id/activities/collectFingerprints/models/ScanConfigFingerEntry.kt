package com.simprints.id.activities.collectFingerprints.models

data class ScanConfigFingerEntry(
    val config: FingerConfig = FingerConfig.OPTIONAL,
    val priority: Int = 0,
    val order: Int = 0
)
