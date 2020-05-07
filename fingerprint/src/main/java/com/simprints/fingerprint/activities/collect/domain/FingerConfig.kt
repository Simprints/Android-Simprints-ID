package com.simprints.fingerprint.activities.collect.domain

import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier

class FingerConfig(val config: Map<FingerIdentifier, FingerScanConfigEntry>) {

    fun getPriority(id: FingerIdentifier): Int = config.getValue(id).priority

    fun getOrder(id: FingerIdentifier): Int = config.getValue(id).order

    data class FingerScanConfigEntry(
        val priority: Int = 0,
        val order: Int = 0
    )

    companion object {
        val DEFAULT = FingerConfig(mapOf(
            FingerIdentifier.LEFT_THUMB to FingerScanConfigEntry(0, 0),
            FingerIdentifier.LEFT_INDEX_FINGER to FingerScanConfigEntry(1, 1),
            FingerIdentifier.LEFT_3RD_FINGER to FingerScanConfigEntry(4, 2),
            FingerIdentifier.LEFT_4TH_FINGER to FingerScanConfigEntry(6, 3),
            FingerIdentifier.LEFT_5TH_FINGER to FingerScanConfigEntry(8, 4),
            FingerIdentifier.RIGHT_THUMB to FingerScanConfigEntry(2, 5),
            FingerIdentifier.RIGHT_INDEX_FINGER to FingerScanConfigEntry(3, 6),
            FingerIdentifier.RIGHT_3RD_FINGER to FingerScanConfigEntry(5, 7),
            FingerIdentifier.RIGHT_4TH_FINGER to FingerScanConfigEntry(7, 8),
            FingerIdentifier.RIGHT_5TH_FINGER to FingerScanConfigEntry(9, 9)
        ))
    }
}
