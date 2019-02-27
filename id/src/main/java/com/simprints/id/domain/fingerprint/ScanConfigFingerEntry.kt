package com.simprints.id.domain.fingerprint

data class ScanConfigFingerEntry(
    val config: FingerConfig = FingerConfig.OPTIONAL,
    val priority: Int = 0,
    val order: Int = 0
)
