package com.simprints.fingerprint.infra.basebiosdk.matching.domain

import com.simprints.core.domain.sample.SampleIdentifier

class Fingerprint(
    val fingerId: SampleIdentifier,
    val template: ByteArray,
    val format: String,
)
