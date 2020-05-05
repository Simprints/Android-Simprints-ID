package com.simprints.fingerprint.activities.collect.state

import com.simprints.fingerprint.activities.collect.domain.ScanConfig

class FingerScanResult(
    val qualityScore: Int,
    val template: ByteArray,
    val image: ByteArray?
) {
    fun isGoodScan() = qualityScore >= ScanConfig.qualityThreshold
}
