package com.simprints.fingerprint.infra.basebiosdk.matching.domain

class Fingerprint(
    val fingerId: FingerIdentifier,
    val template: ByteArray,
    val format: String,
)
