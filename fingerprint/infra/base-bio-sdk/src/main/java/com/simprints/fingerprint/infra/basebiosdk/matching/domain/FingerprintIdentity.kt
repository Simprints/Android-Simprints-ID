package com.simprints.fingerprint.infra.basebiosdk.matching.domain

class FingerprintIdentity(
    val subjectId: String,
    val fingerprints: List<Fingerprint>,
)
