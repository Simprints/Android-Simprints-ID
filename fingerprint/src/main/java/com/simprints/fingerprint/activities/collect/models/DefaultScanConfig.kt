package com.simprints.fingerprint.activities.collect.models

import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier

class DefaultScanConfig {

    private val defaultFingerConfigs: Map<FingerIdentifier, ScanConfigFingerEntry> =
        mapOf(FingerIdentifier.LEFT_THUMB to ScanConfigFingerEntry(FingerConfig.REQUIRED, 0, 0),
            FingerIdentifier.LEFT_INDEX_FINGER to ScanConfigFingerEntry(FingerConfig.REQUIRED, 1, 1),
            FingerIdentifier.LEFT_3RD_FINGER to ScanConfigFingerEntry(FingerConfig.OPTIONAL, 4, 2),
            FingerIdentifier.LEFT_4TH_FINGER to ScanConfigFingerEntry(FingerConfig.OPTIONAL, 6, 3),
            FingerIdentifier.LEFT_5TH_FINGER to ScanConfigFingerEntry(FingerConfig.OPTIONAL, 8, 4),
            FingerIdentifier.RIGHT_THUMB to ScanConfigFingerEntry(FingerConfig.OPTIONAL, 2, 5),
            FingerIdentifier.RIGHT_INDEX_FINGER to ScanConfigFingerEntry(FingerConfig.OPTIONAL, 3, 6),
            FingerIdentifier.RIGHT_3RD_FINGER to ScanConfigFingerEntry(FingerConfig.OPTIONAL, 5, 7),
            FingerIdentifier.RIGHT_4TH_FINGER to ScanConfigFingerEntry(FingerConfig.OPTIONAL, 7, 8),
            FingerIdentifier.RIGHT_5TH_FINGER to ScanConfigFingerEntry(FingerConfig.OPTIONAL, 9, 9))

    operator fun get(id: FingerIdentifier): FingerConfig =
        defaultFingerConfigs[id]?.config
            ?: throw IllegalArgumentException("No FingerConfig associated to id")

    fun getPriority(id: FingerIdentifier): Int =
        defaultFingerConfigs[id]?.priority
            ?: throw IllegalArgumentException("No priority associated to id")

    fun getOrder(id: FingerIdentifier): Int =
        defaultFingerConfigs[id]?.order
            ?: throw IllegalArgumentException("No priority associated to id")
}

