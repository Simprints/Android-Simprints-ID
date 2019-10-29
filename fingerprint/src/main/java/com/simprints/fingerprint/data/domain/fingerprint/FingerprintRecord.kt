package com.simprints.fingerprint.data.domain.fingerprint

class FingerprintRecord(
    val personId: String,
    val fingerprints: List<Fingerprint>
)

fun FingerprintRecord.fromDomainToMatcherPerson() =
    com.simprints.fingerprintmatcher.Person(personId, fingerprints.map { it.fromDomainToMatcher() })
