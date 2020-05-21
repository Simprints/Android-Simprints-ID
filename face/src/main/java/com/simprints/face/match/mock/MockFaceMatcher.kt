package com.simprints.face.match.mock

import com.simprints.face.match.FaceMatcher

class MockFaceMatcher: FaceMatcher() {
    override suspend fun getComparisonScore(probe: ByteArray, matchAgainst: ByteArray): Float {
        return 1f
    }
}
