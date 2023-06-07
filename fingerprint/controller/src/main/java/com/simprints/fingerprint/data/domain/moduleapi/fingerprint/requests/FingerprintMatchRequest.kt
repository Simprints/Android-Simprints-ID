package com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests

import com.simprints.fingerprint.data.domain.fingerprint.Fingerprint
import kotlinx.parcelize.Parcelize
import java.io.Serializable

/**
 * This class represents a Fingerprint request for matching candidates against a fingerprint
 *
 * @param probeFingerprintSamples the list of captured fingerprints of the subject
 * @param queryForCandidates the query parameters used for searching matching candidates
 * @see SubjectQuery
 */
@Parcelize
data class FingerprintMatchRequest(
    val probeFingerprintSamples: List<Fingerprint>,
    val queryForCandidates: Serializable
) : FingerprintRequest
