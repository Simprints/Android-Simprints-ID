package com.simprints.fingerprint.capture.state

internal data class ScanResult(
    val qualityScore: Int,
    val template: ByteArray,
    val templateFormat: String,
    val image: ByteArray?,
    private val qualityThreshold: Int,
) {
    fun isGoodScan() = qualityScore >= qualityThreshold

    override fun toString(): String {
        val qualityScoreStr = "$qualityScore (${if (isGoodScan()) "good scan" else "bad scan"})"
        val templateStr = "${template.size} byte template"
        val imageStr = if (image != null) "${image.size} byte image" else "no image"
        return "FingerScanResult(qualityScore=$qualityScoreStr, $templateStr, $imageStr)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ScanResult

        if (qualityScore != other.qualityScore) return false
        if (!template.contentEquals(other.template)) return false
        if (image != null) {
            if (other.image == null) return false
            if (!image.contentEquals(other.image)) return false
        } else if (other.image != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = qualityScore
        result = 31 * result + template.contentHashCode()
        result = 31 * result + (image?.contentHashCode() ?: 0)
        return result
    }
}
