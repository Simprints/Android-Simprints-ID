package com.simprints.fingerprint.activities.matching

import com.simprints.fingerprint.data.domain.fingerprint.FingerprintIdentity
import com.simprints.fingerprintmatcher.LibMatcher
import io.reactivex.Single

interface MatchTask {

    val matchStartTime: Long

    fun loadCandidates(): Single<List<FingerprintIdentity>>

    fun handlesCandidatesLoaded(candidates: List<FingerprintIdentity>)

    fun getMatcherType(): LibMatcher.MATCHER_TYPE

    fun onMatchProgressDo(progress: Int)

    fun handleMatchResult(candidates: List<FingerprintIdentity>, scores: List<Float>)
}
