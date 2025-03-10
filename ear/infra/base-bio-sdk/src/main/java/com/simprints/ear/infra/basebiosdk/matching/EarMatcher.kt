package com.simprints.ear.infra.basebiosdk.matching

abstract class EarMatcher {
    /** The matching SDK name */
    abstract val matcherName: String
    abstract val supportedTemplateFormat: String

    /** Returns a comparison score of two templates from 0.0 - 100.0 */
    abstract suspend fun getComparisonScore(
        probe: ByteArray,
        matchAgainst: ByteArray,
    ): Float

    /**
     * Get highest comparison score for matching candidate template against all probes
     *
     * @param probes
     * @param candidate
     * @return the highest comparison score
     */
    suspend fun getHighestComparisonScoreForCandidate(
        probes: List<EarSample>,
        candidate: EarIdentity,
    ): Float {
        var highestScore = 0f
        probes.forEach { probe ->
            candidate.faces.forEach { face ->
                val score = getComparisonScore(probe.template, face.template)
                if (score > highestScore) highestScore = score
            }
        }
        return highestScore
    }
}
