package com.simprints.face.match

import com.simprints.face.data.db.person.FaceIdentity
import com.simprints.face.data.db.person.FaceSample

abstract class FaceMatcher {
    /**
     * Returns a comparison score from 0.0 - 100.0
     */
    abstract suspend fun getComparisonScore(probe: ByteArray, matchAgainst: ByteArray): Float

    suspend fun getHighestComparisonScoreForCandidate(probes: List<FaceSample>, candidate: FaceIdentity): Float {
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
