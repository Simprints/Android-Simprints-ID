package com.simprints.fingerprintmatcher.algorithms.simafis

import com.simprints.fingerprintmatcher.FingerIdentifier
import com.simprints.fingerprintmatcher.FingerIdentifier.*
import com.simprints.fingerprintmatcher.Fingerprint
import com.simprints.fingerprintmatcher.FingerprintRecord
import com.simprints.fingerprintmatcher.MatchResult
import com.simprints.fingerprintmatcher.algorithms.simafis.models.SimAfisFingerIdentifier
import com.simprints.fingerprintmatcher.algorithms.simafis.models.SimAfisFingerprint
import com.simprints.fingerprintmatcher.algorithms.simafis.models.SimAfisPerson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

internal class SimAfisMatcher {

    suspend fun match(
        probe: FingerprintRecord,
        candidates: Flow<FingerprintRecord>
    ): Flow<MatchResult> {

        val simAfisCandidates = candidates.map { it.toSimAfisPerson() }.toList()

        println("Matching ${simAfisCandidates.size} candidates using all ${JNILibAfis.getNbCores()} cores")

        val results = JNILibAfis.identify(
            probe.toSimAfisPerson(),
            simAfisCandidates,
            JNILibAfis.getNbCores()
        ) ?: floatArrayOf()

        return results.zip(simAfisCandidates).map { (score, candidate) ->
            MatchResult(candidate.guid, score.toInt())
        }.asFlow()
    }

    private fun FingerprintRecord.toSimAfisPerson(): SimAfisPerson =
        SimAfisPerson(id, fingerprints.map { it.toSimAfisFingerprint() })

    private fun Fingerprint.toSimAfisFingerprint(): SimAfisFingerprint =
        SimAfisFingerprint(fingerId.toSimAfisFingerIdentifier(), template)

    private fun FingerIdentifier.toSimAfisFingerIdentifier(): SimAfisFingerIdentifier =
        when (this) {
            RIGHT_5TH_FINGER -> SimAfisFingerIdentifier.RIGHT_5TH_FINGER
            RIGHT_4TH_FINGER -> SimAfisFingerIdentifier.RIGHT_4TH_FINGER
            RIGHT_3RD_FINGER -> SimAfisFingerIdentifier.RIGHT_3RD_FINGER
            RIGHT_INDEX_FINGER -> SimAfisFingerIdentifier.RIGHT_INDEX_FINGER
            RIGHT_THUMB -> SimAfisFingerIdentifier.RIGHT_THUMB
            LEFT_THUMB -> SimAfisFingerIdentifier.LEFT_THUMB
            LEFT_INDEX_FINGER -> SimAfisFingerIdentifier.LEFT_INDEX_FINGER
            LEFT_3RD_FINGER -> SimAfisFingerIdentifier.LEFT_3RD_FINGER
            LEFT_4TH_FINGER -> SimAfisFingerIdentifier.LEFT_4TH_FINGER
            LEFT_5TH_FINGER -> SimAfisFingerIdentifier.LEFT_5TH_FINGER
        }
}
