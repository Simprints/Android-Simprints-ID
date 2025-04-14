package com.simprints.document.infra.basedocumentsdk.matching

abstract class DocumentMatcher(
    open val probeSamples: List<DocumentSample>
) : AutoCloseable {
    /**
     * Get highest comparison score for matching candidate template against samples
     *
     * @param candidate
     * @return the highest comparison score
     */
    abstract suspend fun getHighestComparisonScoreForCandidate(candidate: DocumentIdentity): Float
}
