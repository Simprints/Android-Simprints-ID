package com.simprints.fingerprint.activities.matching

import com.simprints.fingerprint.data.domain.fingerprint.FingerprintRecord
import com.simprints.fingerprintmatcher.LibMatcher
import io.reactivex.Single

interface MatchTask {

    val matchStartTime: Long

    fun loadCandidates(): Single<List<FingerprintRecord>>

    fun handlesCandidatesLoaded(candidates: List<FingerprintRecord>)

    fun getMatcherType(): LibMatcher.MATCHER_TYPE

    fun onMatchProgressDo(progress: Int)

    fun handleMatchResult(candidates: List<FingerprintRecord>, scores: List<Float>)
}
