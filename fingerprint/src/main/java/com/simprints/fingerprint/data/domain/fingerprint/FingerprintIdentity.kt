package com.simprints.fingerprint.data.domain.fingerprint

class FingerprintIdentity(
    val personId: String,
    val fingerprints: List<Fingerprint>
)

fun FingerprintIdentity.fromDomainToMatcher() =
    com.simprints.fingerprintmatcher.Person(personId, fingerprints.map { it.fromDomainToMatcher() })
