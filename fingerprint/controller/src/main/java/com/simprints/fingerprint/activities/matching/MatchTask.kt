package com.simprints.fingerprint.activities.matching

import com.simprints.fingerprint.data.domain.matching.MatchResult

interface MatchTask {
    companion object {
        const val MATCHER_NAME = "SIM_AFIS"
    }

    val matchStartTime: Long

    fun onBeginLoadCandidates()

    fun onCandidatesLoaded(numberOfCandidates: Int)

    fun handleMatchResult(
        numberOfCandidates: Int,
        matchResults: List<MatchResult>,
        isCrossFingerMatchingEnabled: Boolean
    )
}
