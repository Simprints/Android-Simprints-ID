package com.simprints.fingerprint.infra.basebiosdk.matching.domain

class Fingerprint(
    val fingerId: FingerIdentifier,
    val template: FloatArray,
    val format: String,
)
