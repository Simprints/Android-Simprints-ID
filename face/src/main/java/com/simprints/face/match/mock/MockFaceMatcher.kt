package com.simprints.face.match.mock

import com.simprints.face.match.FaceMatcher
import kotlin.random.Random

class MockFaceMatcher : FaceMatcher() {
    override suspend fun getComparisonScore(probe: ByteArray, matchAgainst: ByteArray): Float {
        return (Random.nextFloat() * 100)
    }
}
