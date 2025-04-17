package com.simprints.face.infra.basebiosdk.matching

abstract class FaceMatcher(
    open val probeSamples: List<FaceSample>
) : AutoCloseable {
    /**
     * Get highest comparison score for matching candidate template against samples
     *
     * @param candidate
     * @return the highest comparison score
     */
    abstract suspend fun getHighestComparisonScoreForCandidate(candidate: FaceIdentity): Float
}
