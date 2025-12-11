package com.simprints.face.infra.basebiosdk.matching

import com.simprints.core.domain.reference.BiometricReferenceCapture
import com.simprints.core.domain.sample.Identity

abstract class FaceMatcher(
    open val probeReference: BiometricReferenceCapture,
) : AutoCloseable {
    /**
     * Get highest comparison score for matching candidate template against samples
     *
     * @param candidate
     * @return the highest comparison score
     */
    abstract suspend fun getHighestComparisonScoreForCandidate(candidate: Identity): Float
}
