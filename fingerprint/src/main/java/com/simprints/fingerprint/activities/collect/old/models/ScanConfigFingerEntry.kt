package com.simprints.fingerprint.activities.collect.old.models

data class ScanConfigFingerEntry(
    val config: FingerConfig = FingerConfig.OPTIONAL,
    val priority: Int = 0,
    val order: Int = 0
)
