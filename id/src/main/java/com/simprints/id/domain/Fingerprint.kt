package com.simprints.id.domain

data class Fingerprint(
    val fingerId: Int,
    val template: ByteArray?,
    var qualityScore: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Fingerprint

        if (fingerId != other.fingerId) return false
        if (template != null) {
            if (other.template == null) return false
            if (!template.contentEquals(other.template)) return false
        } else if (other.template != null) return false
        if (qualityScore != other.qualityScore) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fingerId
        result = 31 * result + (template?.contentHashCode() ?: 0)
        result = 31 * result + qualityScore
        return result
    }
}
