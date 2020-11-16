package com.simprints.fingerprint.data.domain.fingerprint

import com.simprints.fingerprintmatcher.algorithms.simafis.models.SimAfisPerson

class FingerprintIdentity(
    val personId: String,
    val fingerprints: List<Fingerprint>
)

fun FingerprintIdentity.fromDomainToMatcher() =
    SimAfisPerson(personId, fingerprints.map { it.fromDomainToMatcher() })
