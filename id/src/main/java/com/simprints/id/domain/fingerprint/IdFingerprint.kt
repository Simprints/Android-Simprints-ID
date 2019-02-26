package com.simprints.id.domain.fingerprint

import com.simprints.id.FingerIdentifier
import timber.log.Timber

data class IdFingerprint(
    val fingerId: Int,
    val template: ByteArray?,
    var qualityScore: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IdFingerprint

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

// TODO: move this adapter out of domain code. The domain layer should not be aware of outer layers
fun IdFingerprint.toLibFingerprint(): Fingerprint =
    Fingerprint(
        FingerIdentifier.values()[fingerId],
        template ?: throw IllegalArgumentException("Unexpected null fingerprint template")
    )

fun IdFingerprint.toLibFingerprintOrNull(): Fingerprint? =
    try {
        toLibFingerprint()
    } catch (arg: IllegalArgumentException) {
        Timber.tag("FINGERPRINT").d("FAILED")
        null
    }
