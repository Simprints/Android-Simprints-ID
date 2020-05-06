package com.simprints.fingerprint.activities.collect.state

import com.simprints.fingerprint.activities.collect.domain.ScanConfig

data class FingerScanResult(
    val qualityScore: Int,
    val template: ByteArray,
    val image: ByteArray?
) {
    fun isGoodScan() = qualityScore >= ScanConfig.qualityThreshold

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FingerScanResult

        if (qualityScore != other.qualityScore) return false
        if (!template.contentEquals(other.template)) return false
        if (image != null) {
            if (other.image == null) return false
            if (!image.contentEquals(other.image)) return false
        } else if (other.image != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = qualityScore
        result = 31 * result + template.contentHashCode()
        result = 31 * result + (image?.contentHashCode() ?: 0)
        return result
    }
}
