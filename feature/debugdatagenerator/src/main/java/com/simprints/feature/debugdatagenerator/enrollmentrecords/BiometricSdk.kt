package com.simprints.feature.debugdatagenerator.enrollmentrecords

import com.simprints.core.domain.fingerprint.IFingerIdentifier

sealed class BiometricSdk(
    val format: String,
) {
    data class SimMatcher(
        val fingers: Set<IFingerIdentifier>,
    ) : BiometricSdk("ISO_19794_2")

    data class NEC(
        val fingers: Set<IFingerIdentifier>,
    ) : BiometricSdk("NEC_1")

    object Roc1 : BiometricSdk("RANK_ONE_1_23")

    object Roc3 : BiometricSdk("RANK_ONE_3_1")
}
