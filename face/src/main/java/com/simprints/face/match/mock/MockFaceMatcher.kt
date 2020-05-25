package com.simprints.face.match.mock

import com.simprints.face.match.FaceMatcher
import kotlin.random.Random

class MockFaceMatcher : FaceMatcher() {
    /**
     * Returns a comparison score from 0.0 - 100.0
     */
    override suspend fun getComparisonScore(probe: ByteArray, matchAgainst: ByteArray): Float {
        return (Random.nextFloat() * 100)
    }
}
