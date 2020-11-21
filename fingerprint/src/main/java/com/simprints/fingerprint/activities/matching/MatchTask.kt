package com.simprints.fingerprint.activities.matching

import com.simprints.fingerprint.data.domain.matching.MatchResult

interface MatchTask {

    val matchStartTime: Long

    fun onBeginLoadCandidates()

    fun onCandidatesLoaded(numberOfCandidates: Int)

    fun handleMatchResult(numberOfCandidates: Int, matchResults: List<MatchResult>)
}
