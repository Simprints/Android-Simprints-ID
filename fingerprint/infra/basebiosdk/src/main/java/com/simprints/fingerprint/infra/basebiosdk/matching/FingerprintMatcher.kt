package com.simprints.fingerprint.infra.basebiosdk.matching

import com.simprints.fingerprint.infra.basebiosdk.FingerprintTemplate

interface FingerprintMatcher {
    fun match(candidate: FingerprintTemplate, prob: FingerprintTemplate): Float

    fun match(template: FingerprintTemplate, templates: List<FingerprintTemplate>, threshold: Float): List<Float>
}
