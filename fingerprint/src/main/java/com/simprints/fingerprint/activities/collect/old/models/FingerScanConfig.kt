package com.simprints.fingerprint.activities.collect.old.models

import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier

class FingerScanConfig(val config: Map<FingerIdentifier, ScanConfigFingerEntry>) {

    operator fun get(id: FingerIdentifier): FingerConfig =
        config[id]?.config
            ?: throw IllegalArgumentException("No FingerConfig associated to id")

    fun getPriority(id: FingerIdentifier): Int =
        config[id]?.priority
            ?: throw IllegalArgumentException("No priority associated to id")

    fun getOrder(id: FingerIdentifier): Int =
        config[id]?.order
            ?: throw IllegalArgumentException("No priority associated to id")

    companion object {
        val DEFAULT = FingerScanConfig(mapOf(
            FingerIdentifier.LEFT_THUMB to ScanConfigFingerEntry(FingerConfig.REQUIRED, 0, 0),
            FingerIdentifier.LEFT_INDEX_FINGER to ScanConfigFingerEntry(FingerConfig.REQUIRED, 1, 1),
            FingerIdentifier.LEFT_3RD_FINGER to ScanConfigFingerEntry(FingerConfig.OPTIONAL, 4, 2),
            FingerIdentifier.LEFT_4TH_FINGER to ScanConfigFingerEntry(FingerConfig.OPTIONAL, 6, 3),
            FingerIdentifier.LEFT_5TH_FINGER to ScanConfigFingerEntry(FingerConfig.OPTIONAL, 8, 4),
            FingerIdentifier.RIGHT_THUMB to ScanConfigFingerEntry(FingerConfig.OPTIONAL, 2, 5),
            FingerIdentifier.RIGHT_INDEX_FINGER to ScanConfigFingerEntry(FingerConfig.OPTIONAL, 3, 6),
            FingerIdentifier.RIGHT_3RD_FINGER to ScanConfigFingerEntry(FingerConfig.OPTIONAL, 5, 7),
            FingerIdentifier.RIGHT_4TH_FINGER to ScanConfigFingerEntry(FingerConfig.OPTIONAL, 7, 8),
            FingerIdentifier.RIGHT_5TH_FINGER to ScanConfigFingerEntry(FingerConfig.OPTIONAL, 9, 9)
        ))
    }
}

