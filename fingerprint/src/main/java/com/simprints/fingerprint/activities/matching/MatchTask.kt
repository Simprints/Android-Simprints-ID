package com.simprints.fingerprint.activities.matching

import com.simprints.fingerprint.data.domain.person.Person
import com.simprints.fingerprintmatcher.LibMatcher
import io.reactivex.Single

internal interface MatchTask {

    val matchStartTime: Long

    fun loadCandidates(): Single<List<Person>>

    fun handlesCandidatesLoaded(candidates: List<Person>)

    fun getMatcherType(): LibMatcher.MATCHER_TYPE

    fun onMatchProgressDo(progress: Int)

    fun handleMatchResult(candidates: List<Person>, scores: List<Float>)
}
